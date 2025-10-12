package com.example.projec_demo;

import android.util.Log;

import com.pspdfkit.annotations.Annotation;
import com.pspdfkit.annotations.AnnotationType;
import com.pspdfkit.annotations.HighlightAnnotation;
import com.pspdfkit.document.PdfDocument;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;

public class PdfFileHandler {
    public static String createJsonAnnotationFileName(PdfDocument document) {
        String pdfFileName = document.getTitle();
        String documentId = String.valueOf(pdfFileName.hashCode());
        // Xóa ký tự không hợp lệ trong file name (nếu có)
        pdfFileName = pdfFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = pdfFileName + "_annotations_file_" + documentId + ".json";
        return fileName;
    }
    public static void exportAnnotationsWithText(PdfDocument document, File outputFile) {
        try {
            String pdfFileName = document.getTitle(); // hoặc document.getTitle()
            String documentId = document.getUid() != null ? document.getUid().toString() : pdfFileName;
            //Nếu file cũ đã tồn tại → XÓA để tránh bị nối thêm dữ liệu cũ
            if (outputFile.exists()) {
                boolean deleted = outputFile.delete();
                if (deleted) {
                    Log.i("JSON_EXPORT", "Old annotation file deleted before writing new data.");
                } else {
                    Log.w("JSON_EXPORT", "Failed to delete old annotation file — will overwrite instead.");
                }
            }
            JSONArray annotationsArray = new JSONArray();
            //Lấy toàn bộ highlight annotations trong document (toàn bộ các trang)
            List<Annotation> allAnnotations = document
                    .getAnnotationProvider()
                    .getAllAnnotationsOfType(EnumSet.of(AnnotationType.HIGHLIGHT));
            //Duyệt qua tất cả các highlight annotation
            for (Annotation annotation : allAnnotations) {
                if (!(annotation instanceof HighlightAnnotation)) continue;
                HighlightAnnotation highlight = (HighlightAnnotation) annotation;
                int pageIndex = highlight.getPageIndex();
                //Trích text nằm trong vùng highlight (vẫn cần text layer)
                String extractedText = document.getPageText(pageIndex, highlight.getBoundingBox());
                JSONObject highlightObj = new JSONObject();
                highlightObj.put("documentId", documentId);
                highlightObj.put("pdfFileName", pdfFileName);
                highlightObj.put("pageIndex", pageIndex);
                highlightObj.put("id", highlight.getUuid().toString());
                highlightObj.put("color", highlight.getColor());
                highlightObj.put("text", extractedText == null ? "" : extractedText.trim()); //text thật
                highlightObj.put("metadata", new JSONObject(highlight.toInstantJson().toString())); //metadata gốc
                annotationsArray.put(highlightObj);
            }
            //Gói toàn bộ vào object chính
            JSONObject finalJson = new JSONObject();
            finalJson.put("annotations", annotationsArray);
            //Ghi ra file JSON
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(finalJson.toString(2).getBytes());
            }
            System.out.println("Exported annotations with text to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void importAnnotationsFromJson(PdfDocument document, File jsonFile) {
        try {
            //Đọc toàn bộ file JSON thành chuỗi
            FileInputStream fis = new FileInputStream(jsonFile);
            byte[] data = new byte[(int) jsonFile.length()];
            fis.read(data);
            fis.close();
            String jsonString = new String(data, StandardCharsets.UTF_8);
            //Parse chuỗi JSON gốc
            JSONObject jsonRoot = new JSONObject(jsonString);
            JSONArray annotationsArray = jsonRoot.getJSONArray("annotations");
            //Duyệt qua từng annotation trong file
            for (int i = 0; i < annotationsArray.length(); i++) {
                JSONObject annotationObj = annotationsArray.getJSONObject(i);
                //Lấy phần metadata (instant json)
                JSONObject metadata = annotationObj.getJSONObject("metadata");
                //Chuyển sang chuỗi JSON trước khi truyền
                String metadataString = metadata.toString();
                //Tạo annotation từ chuỗi Instant JSON
                Annotation annotation = document
                        .getAnnotationProvider()
                        .createAnnotationFromInstantJson(metadataString);
            }
            //Lưu thay đổi
//            document.saveIfModified();
            System.out.println("Imported annotations from JSON: " + annotationsArray.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
