package com.bayu.csvfileservice;

import com.bayu.csvfileservice.util.PdfUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class CsvFileServiceApplicationTests {

	public static void testGenerateStatementPdf() {

		try {
			PdfUtil pdfUtil = new PdfUtil();

			// ================================
			// 1. Dummy template
			// ================================
			String html = """
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        body { font-family: Arial, sans-serif; padding: 20px; }
                        h1 { color: #003366; }
                        p { font-size: 14px; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th, td { border: 1px solid #999; padding: 8px; }
                    </style>
                </head>
                <body>
                    <h1>Laporan Bulanan Obligasi</h1>
                    <p>Nama Nasabah: <b>Dummy User</b></p>
                    <p>CIF: <b>1234567890</b></p>

                    <table>
                        <tr><th>Produk</th><th>Nominal</th><th>Status</th></tr>
                        <tr><td>Obligasi A</td><td>Rp 10.000.000</td><td>Aktif</td></tr>
                        <tr><td>Obligasi B</td><td>Rp 5.000.000</td><td>Aktif</td></tr>
                    </table>

                    <p style="margin-top:30px;">Generated for testing OpenHTMLToPDF.</p>
                </body>
                </html>
                """;


			// ================================
			// 2. Nama output PDF
			// ================================
			File outputFolder = new File("output-test");
			if (!outputFolder.exists()) outputFolder.mkdirs();

			File outputFile = new File(outputFolder, "Test_Statement.pdf");

			// ================================
			// 3. Generate PDF
			// ================================
			byte[] pdfBytes = pdfUtil.generatePdfFromHtml(html);
			pdfUtil.savePdfToFile(pdfBytes, outputFile.getAbsolutePath());

			System.out.println("PDF berhasil dibuat: " + outputFile.getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Gagal generate PDF: " + e.getMessage());
		}
	}

}
