package com.example.projec_demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pspdfkit.configuration.activity.PdfActivityConfiguration;

import com.pspdfkit.configuration.page.PageLayoutMode;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.document.PdfDocumentLoader;
import com.pspdfkit.document.formatters.DocumentJsonFormatter;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.ui.PdfActivityIntentBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");      // chỉ chọn file PDF
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn file PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                File pdfFile = copyPdfFromAssets("quiz.pdf");
                pdfUri = Uri.fromFile(pdfFile);

                PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(this)
                        .scrollMode(PageScrollMode.CONTINUOUS)
                        .scrollDirection(PageScrollDirection.VERTICAL)
                        .layoutMode(PageLayoutMode.SINGLE)
                        .build();
                Intent intent = PdfActivityIntentBuilder.fromUri(this, pdfUri)
                        .configuration(config)
                        .activityClass(MyPdfActivity.class)
                        .build();
                startActivity(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File copyPdfFromAssets(String assetFileName) throws IOException {
        File outFile = new File(getCacheDir(), assetFileName);
        try (InputStream in = getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return outFile;
    }
}