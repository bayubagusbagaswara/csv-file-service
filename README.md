# Notes

```java
public class HtmlToPdfUtil {

    public static void generatePdf(String html, File outputFile) throws Exception {

        // Mode toleran untuk HTML jelek
        System.setProperty("com.openhtmltopdf.lenient", "true");

        try (OutputStream os = new FileOutputStream(outputFile)) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useHTML5Parser();

            // baseURI agar <img src="file:///..."> bekerja
            String baseUri = outputFile.getParentFile().toURI().toString();

            builder.withHtmlContent(html, baseUri);
            builder.toStream(os);

            // Metadata PDF
            builder.addMetadata("Producer", "Statement Generator v2.0");
            builder.addMetadata("Creator", "OpenHTMLToPDF HTML5 Engine");

            builder.run();
        }
    }
}
```

```java
public static void setStatementAccountOutputFile(Session session, Date startDate, Date endDate,
                                                 Map<String, CustomerAccountStatement> stmtMap, String filterValue) {

    String code = "";
    String cif = "";
    String fullname = "";
    String email = "";
    String aidCust = "";
    String secCode = "";
    String pfCode = "";

    try {

        for (String aid : stmtMap.keySet()) {

            Portfolio pfolio = PortfolioService.get().getById(aid);
            pfCode = pfolio.getCode().replaceAll("\n", "_");

            if (pfCode.contains("AL") && pfCode.contains("921143")) {
                pfCode = "921143AL";
            }

            code = aid;
            cif = pfolio.getCifNumber();
            fullname = pfolio.getAid().getFullName();
            email = pfolio.getAid().getEmail();
            aidCust = pfolio.getAid().getAid();
            secCode = pfolio.getSecurity().getCode();
            MasterPortfolio mp = pfolio.getAid();

            // Password: birthdate ddMMyy
            String passCode = "";
            if (pfolio.getAid().getBirthDate() != null) {
                LocalDate date = GetTanggal.convertDateToLocalDateUsingInstant(pfolio.getAid().getBirthDate());
                passCode = DateTimeFormatter.ofPattern("ddMMyy", Locale.ENGLISH).format(date);
            }

            CustomerAccountStatement stmt = stmtMap.get(aid);

            // Pilih template
            int adsCount = MasterAdsService.get().listByCriterions(
                    Restrictions.eq(MasterAds.APPROVAL_STATUS, ApprovalStatus.Approved)
            ).size();

            File statementAccountTemplate;
            if (adsCount == 0) {
                statementAccountTemplate = new File(templateFolderPath, "TemplateE-statement.txt");
            } else {
                if (mp.getSellingAgent().getCode().equalsIgnoreCase("BDI")) {
                    statementAccountTemplate = new File(templateFolderPath, "TemplateE-statement_2.txt");
                } else {
                    statementAccountTemplate = new File(templateFolderPath, "TemplateE-statement.txt");
                }
            }

            if (!statementAccountTemplate.exists()) {
                throw new RuntimeException("Template Report not found");
            }

            // Cek seluruh portfolio Syariah
            boolean allSyariah = stmt.getPortfolioEntries().stream().allMatch(PortfolioEntry::isSyariah);
            String syariahSuffix = allSyariah ? "_SYARIAH" : "";

            // Load template HTML dari TXT
            String html = Files.readString(Paths.get(statementAccountTemplate.getAbsolutePath()));

            // Isi placeholder HTML
            html = fillCustomerFormData(html, stmt, pfolio.getAid(), session);

            // Output PDF name
            String outputName =
                    "Laporan_Bulanan_Obligasi_" + checkName(stmt.getCustomerName()) + "_" + cif +
                            syariahSuffix + "_" + dfName.format(endDate) + "_" + aidCust + "_" +
                            secCode + "_" + pfCode + ".pdf";

            File outputFile = new File(outputFolder, outputName);

            // REPLACE: iText -> OpenHTMLToPDF
            HtmlToPdfUtil.generatePdf(html, outputFile);

            // Save Report Generator
            List<ReportGenerator> ex = ReportGeneratorService.get().listByCriterions(
                    Restrictions.eq(ReportGenerator.CIF, cif.trim()),
                    Restrictions.eq(ReportGenerator.PERIOD, dfName.format(endDate))
            );

            ReportGenerator eG = new ReportGenerator();
            String combineFileName;

            if (!ex.isEmpty()) {
                combineFileName = ex.get(0).getCombineFileName();
                eG.setCombined(true);
            } else {
                combineFileName = "Laporan_Bulanan_Obligasi_" +
                        checkName(stmt.getCustomerName()) + "_" +
                        cif + syariahSuffix + "_" + dfName.format(endDate) + ".pdf";
                eG.setCombined(false);
            }

            eG.setCif(cif);
            eG.setCustomerName(fullname);
            eG.setEmail(email);
            eG.setSID(pfolio.getAid().getSID());
            eG.setReportType(ReportType.AccountStatement);
            eG.setPassCode(passCode);
            eG.setStatus(0);
            eG.setFilePath(outputFolder.getPath());
            eG.setSecurityCOde(secCode);
            eG.setCombineFileName(combineFileName);
            eG.setPortfolioCode(pfolio.getCode());
            eG.setFileName(outputName);
            eG.setStatementPeriod(dfName.format(endDate));
            eG.setAid(aidCust);

            ReportGeneratorService.get().store(eG);

            // Insert combine record
            insertRbCombine(stmt);
        }

    } catch (Exception e) {

        ReportGeneratorFailed failed = new ReportGeneratorFailed();
        failed.setAid(aidCust);
        failed.setSecurityCode(secCode);
        failed.setPortfolioCode(code);
        failed.setCif(cif);
        failed.setCustomerName(fullname);
        failed.setEmail(email);
        failed.setErrorDescription(e.getMessage());
        failed.setStatementPeriod(dfName.format(endDate));

        ReportGeneratorFailedService.get().store(failed);

        e.printStackTrace();
    }
}

```


```java
public static void setStatementAccountOutputFile(Session session, Date startDate, Date endDate,
                                                 Map<String, CustomerAccountStatement> stmtMap, String filterValue) {

    String code = "";
    String cif = "";
    String fullname = "";
    String email = "";
    String aidCust = "";
    String secCode = "";
    String pfCode = "";

    try {
        for (String aid : stmtMap.keySet()) {

            Portfolio pfolio = PortfolioService.get().getById(aid);

            pfCode = pfolio.getCode().replaceAll("\n", "_");
            if (pfCode.contains("AL") && pfCode.contains("921143")) {
                pfCode = "921143AL";
            }

            code = aid;
            cif = pfolio.getCifNumber();
            fullname = pfolio.getAid().getFullName();
            email = pfolio.getAid().getEmail();
            aidCust = pfolio.getAid().getAid();
            secCode = pfolio.getSecurity().getCode();
            MasterPortfolio mp = pfolio.getAid();

            String passCode = "";
            if (pfolio.getAid().getBirthDate() != null) {
                LocalDate date = GetTanggal.convertDateToLocalDateUsingInstant(pfolio.getAid().getBirthDate());
                String formatDate = DateTimeFormatter.ofPattern("ddMMyy", Locale.ENGLISH).format(date);
                passCode = formatDate;
            }

            CustomerAccountStatement stmt = stmtMap.get(aid);

            int count = 0;
            List<MasterAds> masterAds = MasterAdsService.get()
                    .listByCriterions(Restrictions.eq(MasterAds.APPROVAL_STATUS, ApprovalStatus.Approved));

            if (masterAds.size() > 0) {
                count = masterAds.size();
            }

            File statementAccountTemplate = null;

            if (count == 0) {
                statementAccountTemplate = new File(templateFolderPath, "TemplateE-statement.txt");
            } else if (count == 1) {
                if (mp.getSellingAgent().getCode().equalsIgnoreCase("BDI")) {
                    statementAccountTemplate = new File(templateFolderPath, "TemplateE-statement_2.txt");
                } else {
                    statementAccountTemplate = new File(templateFolderPath, "TemplateE-statement.txt");
                }
            }

            String syariah = "";

            boolean allSyariah = true;
            for (PortfolioEntry pfEntry : stmt.getPortfolioEntries()) {
                if (!pfEntry.isSyariah()) {
                    allSyariah = false;
                    break;
                }
            }
            if (allSyariah) {
                syariah = "_SYARIAH";
            }

            if (!statementAccountTemplate.exists()) {
                throw new RuntimeException("Template Report not found");
            }

            String fileHtml = new String(Files.readAllBytes(Paths.get(statementAccountTemplate.getAbsolutePath())));
            fileHtml = fillCustomerFormData(fileHtml, stmt, pfolio.getAid(), session);

            // FIX HTML tanpa library jsoup
            String fixedHtml = HtmlFixer.fix(fileHtml);

            // Output PDF path
            String outputName = "Laporan_Bulanan_Obligasi_" + checkName(stmt.getCustomerName()) + "_" + cif
                    + syariah + "_" + dfName.format(endDate) + "_" + aidCust + "_"
                    + pfolio.getSecurity().getCode() + "_" + pfCode + ".pdf";

            File outputFile = new File(outputFolder, outputName);

            System.out.println("output " + outputName);

            // ====== GANTIKAN HtmlConverter DENGAN OpenHTMLToPDF ======
            HtmlToPdfUtil.generatePdf(fixedHtml, outputFile);


            // === sisa logic report ke DB tidak saya ubah ===
            List<ReportGenerator> eGList = ReportGeneratorService.get()
                    .listByCriterions(Restrictions.eq(ReportGenerator.CIF, cif.trim()),
                            Restrictions.eq(ReportGenerator.PERIOD, dfName.format(endDate)));

            ReportGenerator eG = new ReportGenerator();
            
            insertRbCombine(stmt);
        }

    } catch (Exception e) {

        ReportGeneratorFailed eG = new ReportGeneratorFailed();
        eG.setAid(aidCust);
        eG.setSecurityCode(secCode);
        eG.setPortfolioCode(code);
        eG.setCif(cif);
        eG.setCustomerName(fullname);
        eG.setEmail(email);
        eG.setErrorDescription(e.getLocalizedMessage());
        eG.setStatementPeriod(dfName.format(endDate));

        ReportGeneratorFailedService.get().store(eG);

        e.printStackTrace();
    }
}

```