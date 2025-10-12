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
        String fileName = PdfFileHandler.createJsonAnnotationFileName(document);
        File jsonFile = new File(getExternalFilesDir(null), fileName);
        if (jsonFile.exists()) {
            PdfFileHandler.importAnnotationsFromJson(document, jsonFile);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (document == null) return;
        try {
//            document.saveIfModified();
            String fileName = PdfFileHandler.createJsonAnnotationFileName(document);
            File jsonFile = new File(getExternalFilesDir(null), fileName);
            PdfFileHandler.exportAnnotationsWithText(document, jsonFile);
            Log.i("JSON_EXPORT", "Annotations and text have been saved at: " + jsonFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("JSON_EXPORT", "Error while exporting annotation JSON", e);
        }
    }
}
