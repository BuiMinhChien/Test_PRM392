package com.example.projec_demo;

import android.util.Log;
import android.view.Menu;

import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.ui.PdfActivity;

import java.io.File;

public class MyPdfActivity extends PdfActivity {
    private PdfDocument document;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean handled = super.onPrepareOptionsMenu(menu);
        return handled;
    }

    @Override
    public void onDocumentLoadFailed(Throwable exception) {
    }

    @Override
    public void onDocumentLoaded(PdfDocument document) {
        super.onDocumentLoaded(document);
        this.document = document;
        Log.i("MyPdfActivity", "Document loaded: " + document);
        File jsonFile = new File(getExternalFilesDir(null), "annotations_with_text.json");
        if (jsonFile.exists()) {
            AnnotationHandler.importAnnotationsFromJson(document, jsonFile);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (document == null) return;
        try {
            document.saveIfModified();
            File jsonFile = new File(getExternalFilesDir(null), "annotations_with_text.json");
            AnnotationHandler.exportAnnotationsWithText(document, jsonFile);
            Log.i("JSON_EXPORT", "Đã lưu annotation + text tại: " + jsonFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("JSON_EXPORT", "Lỗi khi export annotation JSON", e);
        }
    }
}
