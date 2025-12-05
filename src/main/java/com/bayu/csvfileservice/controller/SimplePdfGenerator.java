package com.bayu.csvfileservice.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal test program:
 * - arg[0] = path to your original HTML/TXT template file (required)
 * - arg[1] = optional output PDF path (default: ./test-output.pdf)
 *
 * This keeps your original HTML content intact (no structural changes),
 * only sanitizes it to well-formed XHTML and normalizes image src that use Windows paths.
 */
public class SimplePdfGenerator {

    public static void main(String[] args) {
        Path templatePath;
        Path outputPath;

        // Jika arg[0] diberikan, gunakan itu; jika tidak, coba fallback "./TemplateE-statement.txt"
        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            templatePath = Paths.get(args[0]);
        } else {
            templatePath = Paths.get("TemplateE-statement.txt");
            System.out.println("Info: tidak ada argumen. Mencoba fallback ke ./TemplateE-statement.txt");
        }

        outputPath = (args != null && args.length > 1 && args[1] != null && !args[1].trim().isEmpty())
                ? Paths.get(args[1])
                : Paths.get("test-output.pdf");

        if (!Files.exists(templatePath)) {
            System.err.println("Template file tidak ditemukan: " + templatePath.toAbsolutePath());
            System.err.println("Silakan jalankan dengan: mvn exec:java -Dexec.args=\"C:\\\\path\\\\to\\\\TemplateE-statement.txt C:\\\\path\\\\to\\\\output.pdf\"");
            // Tidak memanggil System.exit -- hanya kembali agar mudah debug di IDE / unit test
            return;
        }

        try {
            // baca template
            String originalHtml = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);

            // normalisasi path gambar Windows (D:\...) -> file:///D:/...
            String preprocessed = normalizeWindowsImagePaths(originalHtml);

            // sanitize -> XHTML (Jsoup)
            String xhtml = sanitizeHtmlToXhtml(preprocessed);

            // baseUri = folder template agar gambar relatif dapat ditemukan
            String baseUri = templatePath.getParent() != null ? templatePath.getParent().toUri().toString() : null;

            renderPdf(xhtml, baseUri, outputPath);

            System.out.println("PDF berhasil dibuat: " + outputPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("PDF generation failed:");
            e.printStackTrace();
        }
    }

    private static String normalizeWindowsImagePaths(String html) {
        if (html == null) return "";

        String result = html;

        // pattern untuk src="C:\path\to\file.png"
        Pattern pDouble = Pattern.compile("(?i)src\\s*=\\s*\"([A-Za-z]:\\\\[^\"']*)\"");
        Matcher m = pDouble.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String path = m.group(1).replace('\\', '/');
            if (!path.startsWith("/")) path = "/" + path; // jadi /D:/...
            String replacement = "src=\"file://" + path + "\"";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        result = sb.toString();

        // pattern untuk src='C:\path\to\file.png'
        Pattern pSingle = Pattern.compile("(?i)src\\s*=\\s*'([A-Za-z]:\\\\[^\"']*)'");
        m = pSingle.matcher(result);
        sb = new StringBuffer();
        while (m.find()) {
            String path = m.group(1).replace('\\', '/');
            if (!path.startsWith("/")) path = "/" + path;
            String replacement = "src='file://" + path + "'";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        result = sb.toString();

        return result;
    }

    private static String sanitizeHtmlToXhtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset(StandardCharsets.UTF_8)
                .prettyPrint(false);
        String xhtml = doc.outerHtml();
        if (xhtml.startsWith("\uFEFF")) {
            xhtml = xhtml.substring(1);
        }
        return xhtml;
    }

    private static void renderPdf(String xhtmlContent, String baseUri, Path outputFile) throws IOException {
        if (xhtmlContent == null || xhtmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty HTML content");
        }

        // pastikan direktori parent ada
        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (OutputStream os = new FileOutputStream(outputFile.toFile())) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtmlContent, baseUri);
            builder.withProducer("Bank Danamon - Test Generator");
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            try { Files.deleteIfExists(outputFile); } catch (Exception ignore) {}
            throw new IOException("Failed to render PDF", e);
        }
    }
}