package com.bayu.csvfileservice.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class PdfUtil {

    public static void generate(String html, String outputFile) throws Exception {

        // Pastikan HTML dibaca dalam UTF-8
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        String safeHtml = new String(htmlBytes, StandardCharsets.UTF_8);

        PdfRendererBuilder builder = new PdfRendererBuilder();

        // HTML5 + LAX MODE (tidak strict XHTML)
        builder.useFastMode();        // memaksa parser HTML5 yang longgar
        builder.withHtmlContent(safeHtml, null);

        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            builder.toStream(os);
            builder.run();
        }
    }

}
