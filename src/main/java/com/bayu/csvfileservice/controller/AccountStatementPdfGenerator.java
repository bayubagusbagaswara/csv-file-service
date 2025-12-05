package com.bayu.csvfileservice.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Contoh: baca template HTML, sanitasi menjadi XHTML (menggunakan Jsoup),
 * salin resource (logo) ke temp dir, lalu render PDF dengan OpenHTMLtoPDF.
 *
 * Sesuaikan readResourceAsString/copyResourceToDir kalau Anda menyimpan template/logo di lokasi berbeda.
 */
public class AccountStatementPdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(AccountStatementPdfGenerator.class);

    public static void main(String[] args) {
        try {
            // Jika arg[0] diberikan, gunakan path file template eksternal; jika tidak, pakai resource dari classpath
            String templateHtml;
            if (args != null && args.length > 0) {
                Path tplPath = Paths.get(args[0]);
                log.info("Reading template from file: {}", tplPath.toAbsolutePath());
                templateHtml = new String(Files.readAllBytes(tplPath), StandardCharsets.UTF_8);
            } else {
                log.info("Reading template from classpath: /templates/template.html");
                templateHtml = readResourceAsString("/templates/template.html");
            }

            // Ganti placeholder sederhana (contoh)
            String filledHtml = templateHtml
                    .replace("[CustomerName]", "Budi Santoso")
                    .replace("[CIF]", "12345678")
                    .replace("[Address]", "Jl. Contoh No.1")
                    .replace("[Branch]", "380 - BADUNG KEROBOKAN")
                    .replace("[StartDate]", "01/12/2022")
                    .replace("[EndDate]", "31/12/2022")
                    .replace("[ProductCode]", "ADMF05CCN3")
                    .replace("[LastHolding]", "2,000,000,000.00")
                    .replace("[PortfolioCode]", "186342")
                    .replace("[MTM]", "0.0")
                    .replace("[MTMValue]", "0.00")
                    .replace("[StatementDetails]", "<tr><td>01/12/2022</td><td>Previous Holding</td><td align=\"right\">2,000,000,000.00</td><td align=\"right\">0.00</td><td align=\"right\">0.00</td><td align=\"right\">0.00</td><td align=\"right\">2,000,000,000.00</td></tr>");

            // Sanitasi / convert ke XHTML well-formed menggunakan Jsoup
            String xhtml = sanitizeHtmlToXhtml(filledHtml);

            // Siapkan temp folder untuk resource agar baseUri = file://... dapat diakses
            Path tempDir = Files.createTempDirectory("pdf-resources-" + UUID.randomUUID());
            tempDir.toFile().deleteOnExit();

            // Jika Anda punya logo di resources, salin ke tempDir. Jika template sudah pakai file:// absolute, ini optional.
//            copyResourceToDir("/templates/logo.png", tempDir);

            // baseUri harus menunjuk ke folder yang berisi resource (logo, css, gambar lainnya)
            String baseUri = tempDir.toUri().toString();
            log.info("Using baseUri: {}", baseUri);

            // output file
            Path output = Paths.get("output-sample.pdf");

            // Render ke PDF
            renderPdf(xhtml, baseUri, output);

            log.info("PDF written to: {}", output.toAbsolutePath());

        } catch (Exception e) {
            log.error("PDF generation failed", e);
        }
    }

    /**
     * Sanitasi HTML -> XHTML menggunakan Jsoup.
     * Menghasilkan XML-well-formed yang bisa diparse OpenHTMLtoPDF.
     */
    private static String sanitizeHtmlToXhtml(String html) {
        Document doc = Jsoup.parse(html);

        // optional: tambahkan namespace XHTML agar lebih aman
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset(StandardCharsets.UTF_8)
                .prettyPrint(false);

        // Pastikan self-closing untuk void elements (img, br, hr) di XHTML output
        String xhtml = doc.outerHtml();

        // Debug: log first lines if needed
        log.debug("Sanitized XHTML (first 500 chars):\n{}", xhtml.length() > 500 ? xhtml.substring(0, 500) + "..." : xhtml);

        return xhtml;
    }

    /**
     * Render XHTML content ke outputFile menggunakan OpenHTMLtoPDF.
     * baseUri harus berupa folder (ending dengan '/'), mis: file:///D:/temp/
     */
    private static void renderPdf(String xhtmlContent, String baseUri, Path outputFile) throws IOException {
        if (xhtmlContent == null || xhtmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("xhtmlContent is empty");
        }
        log.info("Rendering PDF to {}", outputFile.toAbsolutePath());

        try (OutputStream os = new FileOutputStream(outputFile.toFile())) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtmlContent, baseUri); // baseUri penting untuk gambar relatif
            builder.withProducer("MyCompany - AccountStatements"); // optional
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            // rethrow as IOException to preserve stack and allow caller handle
            throw new IOException("Failed to render PDF", e);
        }
    }

    private static String readResourceAsString(String resourcePath) throws IOException {
        try (InputStream is = AccountStatementPdfGenerator.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static void copyResourceToDir(String resourcePath, Path dir) throws IOException {
        try (InputStream is = AccountStatementPdfGenerator.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.info("Resource not found (ok if absent): {}", resourcePath);
                return;
            }
            Path dest = dir.resolve(Paths.get(resourcePath).getFileName().toString());
            Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied resource {} -> {}", resourcePath, dest.toAbsolutePath());
        }
    }
}