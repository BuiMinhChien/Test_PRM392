//package com.example.projec_demo;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.pdf.PdfDocument;
//import android.graphics.pdf.PdfRenderer;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.ParcelFileDescriptor;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.github.barteksc.pdfviewer.PDFView;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.text.Text;
//import com.google.mlkit.vision.text.TextRecognition;
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class PdfActivity extends AppCompatActivity {
//    private PDFView pdfView;
//    private OcrHighlightView highlightOverlay;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_pdf);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        pdfView = findViewById(R.id.pdfView);
//        highlightOverlay = findViewById(R.id.highlightOverlay);
//        highlightOverlay.bringToFront();
//
//        String uriString = getIntent().getStringExtra("pdf_uri");
//        if (uriString != null) {
//            Uri pdfUri = Uri.parse(uriString);
////            pdfView.fromUri(pdfUri)
////                    .enableSwipe(true)
////                    .swipeHorizontal(false)
////                    .load();
//            pdfView.fromUri(pdfUri)
//                    .enableSwipe(true)
//                    .swipeHorizontal(false)
//                    .onLoad(nbPages -> runOcrOnFirstPage(pdfUri)) // Gọi OCR khi PDF load xong
//                    .load();
//        } else {
//            Toast.makeText(this, "Không có file PDF nào được chọn", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void runOcrOnFirstPage(Uri pdfUri) {
//        try {
//            // 1️⃣ Render trang đầu thành Bitmap
//            Bitmap bitmap = renderFirstPageToBitmap(pdfUri);
//
//            // 2️⃣ Chạy OCR bằng ML Kit
//            InputImage image = InputImage.fromBitmap(bitmap, 0);
//            var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//
//            recognizer.process(image)
//                    .addOnSuccessListener(result -> {
//                        // 3️⃣ Duyệt các block chữ
//                        List<Rect> highlightRects = new ArrayList<>();
//
//                        for (Text.TextBlock block : result.getTextBlocks()) {
//                            String text = block.getText();
//                            Rect rect = block.getBoundingBox();
//
//                            // Ví dụ: highlight chữ đầu tiên hoặc đoạn chứa "Introduction"
//                            if (text.contains("Introduction") || highlightRects.isEmpty()) {
//                                if (rect != null) highlightRects.add(rect);
//                            }
//                        }
//
//                        // 4️⃣ Cập nhật overlay highlight
//                        highlightOverlay.setHighlightRects(highlightRects);
//
//                    })
//                    .addOnFailureListener(e ->
//                            Toast.makeText(this, "OCR thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Hàm render trang đầu PDF thành bitmap để OCR.
//     */
//    private Bitmap renderFirstPageToBitmap(Uri pdfUri) throws IOException {
//        File file = new File(pdfUri.getPath());
//        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//        PdfRenderer renderer = new PdfRenderer(fileDescriptor);
//        PdfRenderer.Page page = renderer.openPage(0);
//
//        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
//        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//
//        page.close();
//        renderer.close();
//        return bitmap;
//    }
//}

package com.example.projec_demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PdfActivity extends AppCompatActivity {
    private TextRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        String uriString = getIntent().getStringExtra("pdf_uri");
        if (uriString == null) {
            Toast.makeText(this, "Không nhận được file PDF nào!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Uri pdfUri = Uri.parse(uriString);
        processPdfFile(pdfUri);
    }

    private void processPdfFile(Uri pdfUri) {
        new Thread(() -> {
            StringBuilder allText = new StringBuilder();

            try (ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(pdfUri, "r");
                 PdfRenderer renderer = new PdfRenderer(fd)) {

                int pageCount = renderer.getPageCount();

                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);

                    // ⚙️ Tăng độ phân giải render để OCR đọc được
                    int scale = 10; // 4x hoặc 5x nếu chữ nhỏ
                    int width = page.getWidth() * scale;
                    int height = page.getHeight() * scale;

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    android.graphics.Matrix matrix = new android.graphics.Matrix();
                    matrix.postScale(scale, scale);

                    page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();

                    // 🧠 OCR bằng ML Kit
                    InputImage image = InputImage.fromBitmap(bitmap, 0);
                    Text result = com.google.android.gms.tasks.Tasks.await(
                            recognizer.process(image),
                            30, java.util.concurrent.TimeUnit.SECONDS
                    );

                    if (result != null && !result.getText().trim().isEmpty()) {
                        android.util.Log.d("OCR_RESULT", "Page " + (i+1) + ":\n" + result.getText());
                        allText.append("=== PAGE ").append(i + 1).append(" ===\n");
                        allText.append(result.getText()).append("\n\n");
                    } else {
                        android.util.Log.w("OCR_RESULT", "Trang " + (i+1) + " không có text OCR được.");
                        allText.append("=== PAGE ").append(i + 1).append(" ===\n\n");
                    }
                }

                // 💾 Lưu ra thư mục Download
                File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File outFile = new File(outputDir, "ocr_output.txt");
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(allText.toString().getBytes());
                }

                runOnUiThread(() ->
                        Toast.makeText(this,
                                "✅ OCR xong! Lưu tại: " + outFile.getAbsolutePath(),
                                Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "❌ Lỗi OCR: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }


}
