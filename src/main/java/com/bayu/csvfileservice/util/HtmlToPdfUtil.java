package com.bayu.csvfileservice.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class HtmlToPdfUtil {

    public static void generatePdf(String html, File outputFile) throws Exception {

        // Mode toleran untuk HTML rusak atau tidak lengkap
        System.setProperty("com.openhtmltopdf.lenient", "true");

        try (OutputStream os = new FileOutputStream(outputFile)) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // baseURI agar path relatif / img file:/// berfungsi
            String baseUri = outputFile.getParentFile().toURI().toString();

            builder.withHtmlContent(html, baseUri);
            builder.toStream(os);

            // Tidak ada metadata (karena versi library berbeda-beda)
            // Builder dijalankan langsung
            builder.run();
        }
    }
}
