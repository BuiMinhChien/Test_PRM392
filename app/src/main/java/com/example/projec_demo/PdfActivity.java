package com.example.projec_demo;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.barteksc.pdfviewer.PDFView;

public class PdfActivity extends AppCompatActivity {
    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pdf);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pdfView = findViewById(R.id.pdfView);

        String uriString = getIntent().getStringExtra("pdf_uri");
        if (uriString != null) {
            Uri pdfUri = Uri.parse(uriString);
            pdfView.fromUri(pdfUri)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .load();
        } else {
            Toast.makeText(this, "Không có file PDF nào được chọn", Toast.LENGTH_SHORT).show();
        }
    }
}
