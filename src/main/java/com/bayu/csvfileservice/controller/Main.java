package com.bayu.csvfileservice.controller;

import com.bayu.csvfileservice.util.PdfUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            // Path template HTML/TXT
            String templatePath = "C:\\E-STATEMENT\\template.txt";

            // Baca isi file
            String html = Files.readString(Paths.get(templatePath));

            // Output PDF
            String outputPath = "C:\\E-STATEMENT\\output.pdf";

            // Generate PDF
            PdfUtil.generate(html, outputPath);

            System.out.println("PDF berhasil dibuat di: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
