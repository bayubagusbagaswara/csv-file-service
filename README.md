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


# List Task

1. Upload Data (Bulk) – User upload data Management Fee secara bulk lalu sistem memvalidasinya.
2. Masuk ke Data Change – Data valid disimpan ke Data Change dengan status PENDING_APPROVAL.
3. Approval Data Raw – User approve atau reject data raw sebelum diproses lebih lanjut.
4. Proses Mapping – Sistem melakukan mapping data dari ManagementFeeRaw dan MasterBank.
5. Approval Data Mapping – User approve atau reject hasil mapping data.
6. Kirim ke Middleware – User mengirim data yang sudah siap ke Middleware.
7. Terima Response Middleware – Sistem menerima dan menyimpan response dari Middleware.
8. Handling Response Status – Sistem menangani response berdasarkan status sukses, saldo tidak cukup, atau failed.


# ViewManagementFeeBeforeDelete
misal saya ingin saat menjadi string dari json, nama fieldnya akan berubah.
```java
@Data
@Builder
public class ViewManagementFeeBeforeDelete {

    private String mutualFundName;
    private String investmentManager;
    private String fundCode;
    private String debitAccount;
    private String amount;
    private String creditAccount;
    private String beneficiaryName;
    private String bankName;
    private String paymentInstructions;
    private String paymentType;
    private String period;
    private String description;
    private String bankCode;
}
```
misal contoh dari json menjadi string yang disimpan di jsonBefore atau jsonAfter: "{"namaReksadana": "CapitalMoneyMarket", "managerInvestasi": "PT Capital","fundCode": "A001","rekeningDebet": "12344577"}
maksudnya nama field berbeda dengan nama field di object
```json
{
  "namaReksadana": "CapitalMoneyMarket",
  "managerInvestasi": "PT Capital",
  "fundCode": "A001",
  "rekeningDebet": "12344577"
}
```

# ManagementFeeDto
```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagementFeeDto {

    // ================= BEFORE & AFTER =================
    @JsonView(AuditView.Before.class)
    private String mutualFundName;

    @JsonView(AuditView.Before.class)
    private String investmentManager;

    @JsonView(AuditView.Before.class)
    private String fundCode;

    @JsonView(AuditView.Before.class)
    private String debitAccount;

    @JsonView(AuditView.Before.class)
    private String amount;

    @JsonView(AuditView.Before.class)
    private String creditAccount;

    @JsonView(AuditView.Before.class)
    private String beneficiaryName;

    @JsonView(AuditView.Before.class)
    private String bankName;

    @JsonView(AuditView.Before.class)
    private String paymentInstructions;

    @JsonView(AuditView.Before.class)
    private String paymentType;

    @JsonView(AuditView.Before.class)
    private String period;

    @JsonView(AuditView.Before.class)
    private String description;

    @JsonView(AuditView.Before.class)
    private String bankCode;

    // ================= ONLY AFTER =================
    @JsonView(AuditView.After.class)
    private String internalNotes;
}
```

# DataChangeHelper

```java
@Mapper(componentModel = "spring")
public abstract class DataChangeHelperMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    // ================= CORE =================

    private String toJson(Object obj, Class<?> view) {
        if (obj == null) return null;

        try {
            return objectMapper
                    .writerWithView(view)
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonSerializeException(
                    "Failed to serialize payload: " + obj.getClass().getSimpleName(),
                    e
            );
        }
    }

    // ================= USE CASE =================

    public <T> DataChangeDto forAdd(DataChangeDto baseDto, T after) {
        return baseDto.toBuilder()
                .jsonDataBefore(null)
                .jsonDataAfter(toJson(after, AuditView.After.class))
                .build();
    }

    public <T> DataChangeDto forEdit(DataChangeDto baseDto, T before, T after) {
        return baseDto.toBuilder()
                .jsonDataBefore(toJson(before, AuditView.Before.class))
                .jsonDataAfter(toJson(after, AuditView.After.class))
                .build();
    }

    public <T> DataChangeDto forDelete(DataChangeDto baseDto, T before) {
        return baseDto.toBuilder()
                .jsonDataBefore(toJson(before, AuditView.Before.class))
                .jsonDataAfter(null)
                .build();
    }
}
```

# Delete By Id

```java
@Override
public ProcessResult deleteById(DeleteIdRequest request, DataChangeDto dataChangeDto) {

    ProcessResult processResult = new ProcessResult();

    try {

        Long id = request.getId();

        // 1. get entity
        ManagementFeeRaw entity = managementFeeRawRepository.findById(id)
                .orElseThrow(() ->
                        new DataNotFoundException("ManagementFeeRaw not found with id: " + id)
                );

        // 2. map ke DTO
        ManagementFeeDto dto = managementFeeMapper.toDto(entity);

        // 3. build audit (DELETE)
        DataChangeDto dtoAudit = dataChangeHelperMapper.forDelete(
                dataChangeDto,
                dto
        );

        // 4. map ke entity DataChange
        DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);

        // 5. create delete action
        dataChangeService.createChangeActionDelete(
                dataChange,
                ManagementFeeRaw.class
        );

        processResult.addSuccess();

    } catch (Exception e) {

        log.error("Error deleteById id {}", request.getId(), e);

        processResult.addError(
                ErrorDetail.of(
                        "id",
                        String.valueOf(request.getId()),
                        List.of(e.getMessage())
                )
        );
    }

    return processResult;
}
```

# Delete Approve
```java
@Override
public ProcessResult deleteApprove(ApproveDataChangeRequest request, String clientIp) {

    ProcessResult processResult = new ProcessResult();
    LocalDateTime now = LocalDateTime.now();

    try {

        Long dataChangeId = request.getDataChangeId();

        // 1. get DataChange + VALIDASI PENDING
        DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

        Long entityId = dataChange.getEntityId() != null
                ? Long.valueOf(dataChange.getEntityId())
                : null;

        if (entityId == null) {
            throw new IllegalStateException("EntityId is null for delete operation");
        }

        Optional<ManagementFeeRaw> optional =
                managementFeeRawRepository.findById(entityId);

        // ================= SUCCESS =================
        if (optional.isPresent()) {

            ManagementFeeRaw entity = optional.get();

            // set approval
            setApprovalFieldsToDataChange(
                    dataChange,
                    request,
                    clientIp,
                    entity.getId(),
                    now
            );

            // delete
            managementFeeRawRepository.delete(entity);

            // audit update
            dataChange.setJsonDataAfter(null);
            dataChange.setDescription(
                    "Success delete management fee with id: " + entity.getId()
            );

            dataChangeService.setApprovalStatusIsApproved(dataChange);

            processResult.addSuccess();

        } else {

            // ================= NOT FOUND =================
            setApprovalFieldsToDataChange(
                    dataChange,
                    request,
                    clientIp,
                    null,
                    now
            );

            dataChangeService.setApprovalStatusIsRejected(
                    dataChange,
                    List.of("Management Fee not found")
            );

            processResult.addError(
                    ErrorDetail.of(
                            "dataChangeId",
                            String.valueOf(dataChangeId),
                            List.of("Management Fee not found")
                    )
            );
        }

    } catch (Exception e) {

        log.error("Error deleteApprove dataChangeId {}", request.getDataChangeId(), e);

        processResult.addError(
                ErrorDetail.of(
                        "dataChangeId",
                        String.valueOf(request.getDataChangeId()),
                        List.of("Failed to approve delete")
                )
        );
    }

    return processResult;
}
```

# Process Single Request

```java
private ErrorDetail processSingleRequest(
        ManagementFeeRequest request,
        DataChangeDto dataChangeDto
) {

    List<String> errors = new ArrayList<>();

    // ================= VALIDATION =================
    Set<ConstraintViolation<ManagementFeeRequest>> violations =
            validationData.validateObject(request);

    if (!violations.isEmpty()) {

        errors.addAll(
                violations.stream()
                        .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                        .toList()
        );

        log.warn("Validation failed for fundCode {}: {}", request.getFundCode(), errors);

        return buildError(request, errors);
    }

    // ================= BUSINESS PROCESS =================
    try {

        // 🔥 langsung ke DTO
        ManagementFeeDto dtoData =
                managementFeeMapper.fromRequestToDto(request);

        // 🔥 pakai JsonView (After)
        DataChangeDto dto =
                dataChangeHelperMapper.forAdd(dataChangeDto, dtoData);

        DataChange entity = dataChangeMapper.toEntity(dto);

        dataChangeService.createChangeActionAdd(entity, ManagementFeeRaw.class);

    } catch (Exception e) {

        log.error("Unexpected error for fundCode {}", request.getFundCode(), e);

        errors.add("Failed to process data");

        return buildError(request, errors);
    }

    return null; // success
}
```

# Method Mapping

```java
fromRequestToDto();
```

# Base Mapper

```java
public interface BaseMapper<REQ, DTO, ENTITY> {

    DTO toDto(REQ request);

    ENTITY toEntity(DTO dto);

    DTO toDto(ENTITY entity);
}
```

# Mapper Implementation

```java
@Mapper(componentModel = "spring")
public interface ManagementFeeMapper extends BaseMapper<
        ManagementFeeRequest,
        ManagementFeeDto,
        ManagementFeeRaw> {

    // kalau perlu custom mapping, tambahkan di sini
}
```

# AuditView

```java
public class AuditView {

    public static class Before {}

    public static class After extends Before {}
}
```

# ManagementFeeDto

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagementFeeDto {

    // ================= BEFORE & AFTER =================
    @JsonView(AuditView.Before.class)
    private String mutualFundName;

    @JsonView(AuditView.Before.class)
    private String investmentManager;

    @JsonView(AuditView.Before.class)
    private String fundCode;

    @JsonView(AuditView.Before.class)
    private String debitAccount;

    @JsonView(AuditView.Before.class)
    private String amount;

    @JsonView(AuditView.Before.class)
    private String creditAccount;

    @JsonView(AuditView.Before.class)
    private String beneficiaryName;

    @JsonView(AuditView.Before.class)
    private String bankName;

    @JsonView(AuditView.Before.class)
    private String paymentInstructions;

    @JsonView(AuditView.Before.class)
    private String paymentType;

    @JsonView(AuditView.Before.class)
    private String period;

    @JsonView(AuditView.Before.class)
    private String description;

    @JsonView(AuditView.Before.class)
    private String bankCode;

    // ================= ONLY AFTER =================
    @JsonView(AuditView.After.class)
    private String internalNotes;
}
```

# DataChangeHelper

```java
@Mapper(componentModel = "spring")
public abstract class DataChangeHelperMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    private String toJson(Object obj, Class<?> view) {
        if (obj == null) return null;

        try {
            return objectMapper
                    .writerWithView(view)
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonSerializeException(
                    "Failed to serialize payload: " + obj.getClass().getSimpleName(),
                    e
            );
        }
    }

    public <T> DataChangeDto forAdd(DataChangeDto baseDto, T after) {
        return baseDto.toBuilder()
                .jsonDataBefore(null)
                .jsonDataAfter(toJson(after, AuditView.After.class))
                .build();
    }

    public <T> DataChangeDto forEdit(DataChangeDto baseDto, T before, T after) {
        return baseDto.toBuilder()
                .jsonDataBefore(toJson(before, AuditView.Before.class))
                .jsonDataAfter(toJson(after, AuditView.After.class))
                .build();
    }

    public <T> DataChangeDto forDelete(DataChangeDto baseDto, T before) {
        return baseDto.toBuilder()
                .jsonDataBefore(toJson(before, AuditView.Before.class))
                .jsonDataAfter(null)
                .build();
    }
}
```

# BaseAuditService

```java
@RequiredArgsConstructor
@Slf4j
public abstract class BaseAuditService<REQ, DTO, ENTITY> {

    protected final ValidationData validationData;
    protected final DataChangeHelperMapper auditMapper;
    protected final DataChangeMapper dataChangeMapper;
    protected final DataChangeService dataChangeService;

    protected abstract BaseMapper<REQ, DTO, ENTITY> getMapper();

    protected abstract Class<?> getEntityClass();

    protected abstract String getIdentifier(REQ request);

    // ================= CREATE =================
    protected ErrorDetail processCreate(REQ request, DataChangeDto baseDto) {

        List<String> errors = new ArrayList<>();

        Set<ConstraintViolation<REQ>> violations =
                validationData.validateObject(request);

        if (!violations.isEmpty()) {

            errors.addAll(
                    violations.stream()
                            .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                            .toList()
            );

            log.warn("Validation failed {}: {}", getIdentifier(request), errors);

            return ErrorDetail.of("request", getIdentifier(request), errors);
        }

        try {

            DTO dto = getMapper().toDto(request);

            DataChangeDto auditDto =
                    auditMapper.forAdd(baseDto, dto);

            DataChange entity =
                    dataChangeMapper.toEntity(auditDto);

            dataChangeService.createChangeActionAdd(
                    entity,
                    getEntityClass()
            );

        } catch (Exception e) {

            log.error("Error processing {}", getIdentifier(request), e);

            errors.add("Failed to process data");

            return ErrorDetail.of("request", getIdentifier(request), errors);
        }

        return null;
    }
}
```

# ManagementFeeRawServiceImpl

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeRawServiceImpl
        extends BaseAuditService<
        ManagementFeeRequest,
        ManagementFeeDto,
        ManagementFeeRaw>
        implements ManagementFeeRawService {

    private final ManagementFeeMapper mapper;

    @Override
    protected BaseMapper<ManagementFeeRequest, ManagementFeeDto, ManagementFeeRaw> getMapper() {
        return mapper;
    }

    @Override
    protected Class<?> getEntityClass() {
        return ManagementFeeRaw.class;
    }

    @Override
    protected String getIdentifier(ManagementFeeRequest request) {
        return request.getFundCode();
    }

    // ================= BULK =================
    @Override
    public ProcessResult createBulk(
            ManagementFeeBulkRequest request,
            DataChangeDto dataChangeDto
    ) {

        ProcessResult result = new ProcessResult();

        for (ManagementFeeRequest item : request.getItems()) {

            ErrorDetail error = processCreate(item, dataChangeDto);

            if (error != null) {
                result.addError(error);
            } else {
                result.addSuccess();
            }
        }

        return result;
    }
}
```

# Edit Management Fee

```java
@Override
    @Transactional
    public ProcessResult editById(EditRequest request, DataChangeDto dataChangeDto) {
        ProcessResult processResult = new ProcessResult();
        try {
            // 1. Get existing entity
            ManagementFeeRaw existingEntity = managementFeeRawRepository.findById(request.getId())
                    .orElseThrow(() -> new DataNotFoundException("ManagementFeeRaw not found with id: " + request.getId()));

            // 2. Map to DTOs
            ManagementFeeDto beforeDto = managementFeeMapper.fromEntityToDto(existingEntity);
            ManagementFeeDto afterDto = managementFeeMapper.fromRequestToDto(request);
            
            // 3. Set ID untuk afterDto (karena dari request tidak ada ID)
            afterDto.setId(existingEntity.getId());

            // 4. For EDIT: jsonBefore DAN jsonAfter sama-sama mengandung id, month, year
            DataChangeDto dtoAudit = dataChangeHelperMapper.forEdit(dataChangeDto, beforeDto, afterDto);

            // 5. Save to data change
            DataChange dataChange = dataChangeMapper.toEntity(dtoAudit);
            dataChange.setEntityId(String.valueOf(request.getId()));

            dataChangeService.createChangeActionEdit(dataChange, ManagementFeeRaw.class);
            
            processResult.addSuccess();
            
        } catch (Exception e) {
            log.error("Error editById id {}", request.getId(), e);
            processResult.addError(
                    ErrorDetail.of("id", String.valueOf(request.getId()), List.of(e.getMessage()))
            );
        }
        return processResult;
    }

    @Override
    @Transactional
    public ProcessResult editApprove(ApproveDataChangeRequest request, String clientIp) {
        ProcessResult processResult = new ProcessResult();
        LocalDateTime now = LocalDateTime.now();
        try {
            Long dataChangeId = request.getDataChangeId();

            // 1. Get DataChange + VALIDASI PENDING
            DataChange dataChange = dataChangeService.getPendingById(dataChangeId);

            Long entityId = dataChange.getEntityId() != null
                    ? Long.valueOf(dataChange.getEntityId())
                    : null;

            Optional<ManagementFeeRaw> optional = entityId != null
                    ? managementFeeRawRepository.findById(entityId)
                    : Optional.empty();

            if (optional.isPresent()) {
                ManagementFeeRaw existingEntity = optional.get();
                
                // Parse JSON after untuk mendapatkan data baru
                ManagementFeeDto afterPayload = jsonHelper.fromJson(dataChange.getJsonDataAfter(), ManagementFeeDto.class);

                // Update entity dengan data baru
                updateEntityFromDto(existingEntity, afterPayload);
                
                // Set approval fields
                setApprovalFields(existingEntity, dataChange, request, clientIp, now);
                managementFeeRawRepository.save(existingEntity);

                // Set approval ke dataChange
                setApprovalFieldsToDataChange(dataChange, request, clientIp, existingEntity.getId(), now);

                // Update jsonAfter dengan data lengkap setelah update
                ManagementFeeDto completeDto = managementFeeMapper.toDto(existingEntity);
                dataChange.setJsonDataAfter(jsonHelper.toJson(completeDto));

                dataChange.setDescription("Success approve edit of management fee with id: " + existingEntity.getId());
                dataChangeService.setApprovalStatusIsApproved(dataChange);
                processResult.addSuccess();
                
            } else {
                // Entity not found
                setApprovalFieldsToDataChange(dataChange, request, clientIp, null, now);
                dataChangeService.setApprovalStatusIsRejected(
                        dataChange,
                        List.of("Management Fee not found")
                );
                processResult.addError(
                        ErrorDetail.of(
                                DATA_CHANGE_ID_FIELD,
                                String.valueOf(dataChangeId),
                                List.of("Management Fee not found")
                        )
                );
            }
            
        } catch (Exception e) {
            log.error("Error editApprove dataChangeId {}", request.getDataChangeId(), e);
            processResult.addError(
                    ErrorDetail.of(
                            "dataChangeId",
                            String.valueOf(request.getDataChangeId()),
                            List.of("Failed to approve edit: " + e.getMessage())
                    )
            );
        }
        return processResult;
    }

```


# Contoh Transfer BiFast

```java

// 1. buat InquiryAccountRequest
InquiryAccountRequest inquiryAccountRequest = createInquiryAccountRequest(managementFeeMap);

// set referenceId untuk inquiry, sistem sebelumnya ada 2 field, yakni inquiryReferenceId dan referenceId
managementFeeMap.setInquiryReferenceId("uuid");

// create NCBS Request dan save

// send ke middleware
InquiryAccountResponse inquiryAccountResponse = middlewareService.inquiryAccount(inquiryAccountRequest, managementFeeMap.getReferenceId);

// tangkap response inquiry dan simpan NCBS Response

// cek response code service inquiry

// jika response code gagal, maka tidak akan lanjut langsung transaksi failed

// jika response code sukses, maka lanjut ke proses selanjutnya

// proses selanjutnya adalah membuat CreditTransferBiFastRequest
CreditTransferBiFastRequest creditTransferBiFastRequest = createCreditTransferBiFast(inquiryResponse.getData, managementFeeMap);

// create NCBS Request dan save

// send ke middleware
CreditTransferResponse creditTransferResponse = middlewareService.creditTransfer(referenceId, creditTransferBiFastRequest);

// biasanya response creditTransfer ada field payUserRefNo. Kita tangkap datanya lalu kita simpan di ncbsResponse dengan field sendiri

// tangkap response dan save NCBS Response

// handle juga jika saat credit transfer terjadi saldo kurang


```

# Flow SKN RTGS

```java
// create request untuk skn rtgs

// create NCBS request

// hit ke middleware service skn rtgs, responsenya SknRtgsResponse

// create NCBS Response

// handle response

```

# Flow Overbooking
```java
// create Overbooking request

// create NCBS Request

// hit middlewareService overbooking. responsenya overbooking response

// create NCBS Response

// handle response
```

# Transfer Executor

```java
public interface TransferExecutor {

    boolean supports(TransferMethod method);

    NcbsResponse execute(ManagementFeeMap item);
}
```

# BASE ABSTRACT — BaseTransferExecutor

```java
@RequiredArgsConstructor
@Slf4j
public abstract class BaseTransferExecutor implements TransferExecutor {

    protected final NcbsRequestRepository requestRepository;
    protected final NcbsResponseRepository responseRepository;
    protected final ObjectMapper objectMapper;

    protected NcbsRequest buildNcbsRequest(
            ManagementFeeMap item,
            MiddlewareServiceType service,
            String referenceId
    ) {
        return NcbsRequest.builder()
                .referenceId(referenceId)
                .entityId(item.getId())
                .createdDate(LocalDateTime.now())
                .transferScope(item.getTransferScope())
                .transferMethod(item.getTransferMethod())
                .service(service)
                .build();
    }

    protected NcbsResponse buildNcbsResponse(
            NcbsRequest request,
            Object response
    ) {
        try {
            return NcbsResponse.builder()
                    .referenceId(request.getReferenceId())
                    .entityId(request.getEntityId())
                    .createdDate(LocalDateTime.now())
                    .service(request.getService())
                    .transferMethod(request.getTransferMethod())
                    .transferScope(request.getTransferScope())
                    .jsonResponse(objectMapper.writeValueAsString(response))
                    .responseCode(extractResponseCode(response))
                    .responseMessage(extractResponseMessage(response))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build NcbsResponse", e);
        }
    }

    protected String extractResponseCode(Object response) {
        if (response instanceof OverbookingResponse r) return r.getResponseCode();
        if (response instanceof SknRtgsTransferResponse r) return r.getResponseCode();
        if (response instanceof CreditTransferResponse r) return r.getResponseCode();
        if (response instanceof InquiryAccountResponse r) return r.getResponseCode();
        return null;
    }

    protected String extractResponseMessage(Object response) {
        if (response instanceof OverbookingResponse r) return r.getResponseMessage();
        if (response instanceof SknRtgsTransferResponse r) return r.getResponseMessage();
        if (response instanceof CreditTransferResponse r) return r.getResponseMessage();
        if (response instanceof InquiryAccountResponse r) return r.getResponseMessage();
        return null;
    }
}
```

# Overbooking Executor

```java
@Component
@RequiredArgsConstructor
public class OverbookingTransferExecutor extends BaseTransferExecutor {

    private final MiddlewareService middlewareService;

    @Override
    public boolean supports(TransferMethod method) {
        return method == TransferMethod.OVERBOOKING;
    }

    @Override
    public NcbsResponse execute(ManagementFeeMap item) {

        String referenceId = UUID.randomUUID().toString();

        OverbookingRequest request = OverbookingRequest.builder()
                .debitAccount(item.getDebitAccount())
                .creditAccount(item.getCreditAccount())
                .amount(item.getAmount())
                .remark(item.getDescription())
                .build();

        NcbsRequest ncbsRequest = buildNcbsRequest(
                item,
                MiddlewareServiceType.OVERBOOKING_CASA,
                referenceId
        );

        requestRepository.save(ncbsRequest);

        OverbookingResponse response =
                middlewareService.overbooking(referenceId, request);

        if (response == null) {
            throw new IllegalStateException("Overbooking response is null");
        }

        NcbsResponse ncbsResponse =
                buildNcbsResponse(ncbsRequest, response);

        ncbsResponse.setProviderSystem(response.getTransactionId());

        responseRepository.save(ncbsResponse);

        return ncbsResponse;
    }
}
```

# SKN / RTGS EXECUTOR

```java
@Component
@RequiredArgsConstructor
public class SknRtgsTransferExecutor extends BaseTransferExecutor {

    private final MiddlewareService middlewareService;

    @Override
    public boolean supports(TransferMethod method) {
        return method == TransferMethod.SKN || method == TransferMethod.RTGS;
    }

    @Override
    public NcbsResponse execute(ManagementFeeMap item) {

        String referenceId = UUID.randomUUID().toString();

        SknRtgsTransferRequest request = SknRtgsTransferRequest.builder()
                .debitAccount(item.getDebitAccount())
                .beneficiaryAccount(item.getCreditAccount())
                .beneficiaryName(item.getBeneficiaryName())
                .bankCode(item.getBankCode())
                .amount(item.getAmount())
                .remark(item.getDescription())
                .build();

        NcbsRequest ncbsRequest = buildNcbsRequest(
                item,
                MiddlewareServiceType.TRANSFER_SKN_RTGS,
                referenceId
        );

        requestRepository.save(ncbsRequest);

        SknRtgsTransferResponse response =
                middlewareService.transferSknRtgs(referenceId, request);

        if (response == null) {
            throw new IllegalStateException("SKN/RTGS response is null");
        }

        NcbsResponse ncbsResponse =
                buildNcbsResponse(ncbsRequest, response);

        ncbsResponse.setProviderSystem(response.getTransactionId());

        responseRepository.save(ncbsResponse);

        return ncbsResponse;
    }
}
```

# BI-FAST EXECUTOR

```java
@Component
@RequiredArgsConstructor
public class BiFastTransferExecutor extends BaseTransferExecutor {

    private final MiddlewareService middlewareService;
    private final ResponseCodeService responseCodeService;

    @Override
    public boolean supports(TransferMethod method) {
        return method == TransferMethod.BIFAST;
    }

    @Override
    public NcbsResponse execute(ManagementFeeMap item) {

        // ================= INQUIRY =================
        String inquiryRefId = UUID.randomUUID().toString();

        InquiryAccountRequest inquiryRequest = InquiryAccountRequest.builder()
                .accountNumber(item.getCreditAccount())
                .bankCode(item.getBankCode())
                .amount(item.getAmount())
                .build();

        NcbsRequest inquiryNcbsRequest = buildNcbsRequest(
                item,
                MiddlewareServiceType.INQUIRY_ACCOUNT,
                inquiryRefId
        );

        requestRepository.save(inquiryNcbsRequest);

        InquiryAccountResponse inquiryResponse =
                middlewareService.inquiryAccount(inquiryRequest, inquiryRefId);

        if (inquiryResponse == null) {
            throw new IllegalStateException("Inquiry response is null");
        }

        NcbsResponse inquiryNcbsResponse =
                buildNcbsResponse(inquiryNcbsRequest, inquiryResponse);

        responseRepository.save(inquiryNcbsResponse);

        if (!responseCodeService.isSuccess(inquiryResponse.getResponseCode())) {
            return inquiryNcbsResponse;
        }

        // ================= TRANSFER =================
        String transferRefId = UUID.randomUUID().toString();

        CreditTransferBiFastRequest transferRequest =
                CreditTransferBiFastRequest.builder()
                        .beneficiaryName(inquiryResponse.getData().getAccountName())
                        .beneficiaryAccount(item.getCreditAccount())
                        .bankCode(item.getBankCode())
                        .amount(item.getAmount())
                        .remark(item.getDescription())
                        .build();

        NcbsRequest transferNcbsRequest = buildNcbsRequest(
                item,
                MiddlewareServiceType.TRANSFER_SKN_RTGS,
                transferRefId
        );

        requestRepository.save(transferNcbsRequest);

        CreditTransferResponse transferResponse =
                middlewareService.creditTransfer(transferRefId, transferRequest);

        if (transferResponse == null) {
            throw new IllegalStateException("BiFast transfer response is null");
        }

        NcbsResponse transferNcbsResponse =
                buildNcbsResponse(transferNcbsRequest, transferResponse);

        transferNcbsResponse.setProviderSystem(
                transferResponse.getPayUserRefNo()
        );

        responseRepository.save(transferNcbsResponse);

        return transferNcbsResponse;
    }
}
```

# SERVICE — ManagementFeeMapService (FINAL)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagementFeeMapService {

    private final ManagementFeeMapRepository mapRepository;
    private final ResponseCodeService responseCodeService;
    private final List<TransferExecutor> executors;

    public ProcessResult sendTransactions(List<Long> ids) {

        ProcessResult result = new ProcessResult();

        List<ManagementFeeMap> list = mapRepository.findAllById(ids);

        for (ManagementFeeMap item : list) {

            try {

                validateSend(item);

                TransferExecutor executor = executors.stream()
                        .filter(e -> e.supports(item.getTransferMethod()))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException("No executor found"));

                NcbsResponse response = executor.execute(item);

                // ================= UPDATE =================

                item.setStatus(MappingStatus.SENT);
                item.setReferenceId(response.getReferenceId());
                item.setLastSentDate(LocalDateTime.now());
                item.setRetryCount(
                        item.getRetryCount() == null ? 1 : item.getRetryCount() + 1
                );

                updateStatus(item, response);

                mapRepository.save(item);

                result.addSuccess();

            } catch (Exception e) {

                log.error("Send failed id {}", item.getId(), e);

                item.setStatus(MappingStatus.FAILED);
                mapRepository.save(item);

                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of(e.getMessage())
                        )
                );
            }
        }

        return result;
    }

    private void validateSend(ManagementFeeMap item) {

        if (!List.of(MappingStatus.READY, MappingStatus.RETRY)
                .contains(item.getStatus())) {

            throw new IllegalStateException("Invalid status for send");
        }
    }

    private void updateStatus(ManagementFeeMap item, NcbsResponse response) {

        if (responseCodeService.isSuccess(response.getResponseCode())) {

            item.setStatus(MappingStatus.SUCCESS);

        } else if (responseCodeService.isInsufficientBalance(response.getResponseCode())) {

            item.setStatus(MappingStatus.RETRY);

        } else {

            item.setStatus(MappingStatus.FAILED);
        }
    }
}
```

# Selasa 14 April 2026

## Final Architecture

```bash
Controller
   ↓
Service (ManagementFee / TaxBroker)
   ↓
TransferOrchestratorService   ✅ GENERIC
   ↓
TransferExecutor              ✅ GENERIC INTERFACE
   ↓
Executor spesifik:
   ├── OverbookingExecutor
   ├── SknRtgsExecutor
   ├── BiFastExecutor
   ↓
MiddlewareService             ❗ SPESIFIK
   ↓
NcbsRequest / NcbsResponse
```
## 1. Interface Transferable
semua entity yang bisa dikirim ke middleware harus implement ini
```java
public interface Transferable {

    Long getId();

    String getDebitAccount();

    String getCreditAccount();

    String getBeneficiaryName();

    String getBankCode();

    BigDecimal getAmount();

    String getDescription();

    TransferMethod getTransferMethod();

    TransferScope getTransferScope();
}
```

## Entity ManagementFeeMap
```java
@Entity
@Table(name = "management_fee_map")
@Data
public class ManagementFeeMap implements Transferable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String debitAccount;
    private String creditAccount;
    private String beneficiaryName;
    private String bankCode;
    private BigDecimal amount;
    private String description;

    @Enumerated(EnumType.STRING)
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    private MappingStatus status;

    private String referenceId;
    private Integer retryCount;
    private LocalDateTime lastSentDate;
}
```

## Entity TraxBrokerFee

```java
@Entity
@Table(name = "tax_broker_fee")
@Data
public class TaxBrokerFee implements Transferable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String debitAccount;
    private String creditAccount;
    private String beneficiaryName;
    private String bankCode;
    private BigDecimal amount;
    private String description;

    @Enumerated(EnumType.STRING)
    private TransferMethod transferMethod;

    @Enumerated(EnumType.STRING)
    private TransferScope transferScope;

    @Enumerated(EnumType.STRING)
    private MappingStatus status;

    private String referenceId;
    private Integer retryCount;
}
```

## Interface TransferExecutor
```java
public interface TransferExecutor {

    boolean supports(TransferMethod method);

    NcbsResponse execute(Transferable item);
}
```

## Base Executor (Common Logic)

```java
@RequiredArgsConstructor
public abstract class BaseTransferExecutor implements TransferExecutor {

    protected final NcbsRequestRepository requestRepository;
    protected final NcbsResponseRepository responseRepository;
    protected final ObjectMapper objectMapper;

    protected NcbsRequest createNcbsRequest(
            Transferable item,
            MiddlewareServiceType service,
            String referenceId
    ) {
        return NcbsRequest.builder()
                .referenceId(referenceId)
                .entityId(item.getId())
                .createdDate(LocalDateTime.now())
                .transferMethod(item.getTransferMethod())
                .transferScope(item.getTransferScope())
                .service(service)
                .build();
    }

    protected NcbsResponse createNcbsResponse(
            NcbsRequest request,
            Object response
    ) {
        try {
            return NcbsResponse.builder()
                    .referenceId(request.getReferenceId())
                    .entityId(request.getEntityId())
                    .createdDate(LocalDateTime.now())
                    .jsonResponse(objectMapper.writeValueAsString(response))
                    .responseCode(extractCode(response))
                    .responseMessage(extractMessage(response))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractCode(Object res) {
        if (res instanceof OverbookingResponse r) return r.getResponseCode();
        if (res instanceof SknRtgsTransferResponse r) return r.getResponseCode();
        if (res instanceof CreditTransferResponse r) return r.getResponseCode();
        if (res instanceof InquiryAccountResponse r) return r.getResponseCode();
        return null;
    }

    private String extractMessage(Object res) {
        if (res instanceof OverbookingResponse r) return r.getResponseMessage();
        if (res instanceof SknRtgsTransferResponse r) return r.getResponseMessage();
        if (res instanceof CreditTransferResponse r) return r.getResponseMessage();
        if (res instanceof InquiryAccountResponse r) return r.getResponseMessage();
        return null;
    }
}
```

## 5. Executor Specific

### Overbooking
```java
@Component
@RequiredArgsConstructor
public class OverbookingExecutor extends BaseTransferExecutor {

    private final MiddlewareService middleware;

    @Override
    public boolean supports(TransferMethod method) {
        return method == TransferMethod.OVERBOOKING;
    }

    @Override
    public NcbsResponse execute(Transferable item) {

        String refId = UUID.randomUUID().toString();

        OverbookingRequest request = OverbookingRequest.builder()
                .debitAccount(item.getDebitAccount())
                .creditAccount(item.getCreditAccount())
                .amount(item.getAmount())
                .remark(item.getDescription())
                .build();

        NcbsRequest ncbsRequest =
                createNcbsRequest(item, MiddlewareServiceType.OVERBOOKING_CASA, refId);

        requestRepository.save(ncbsRequest);

        OverbookingResponse response =
                middleware.overbooking(refId, request);

        NcbsResponse ncbsResponse =
                createNcbsResponse(ncbsRequest, response);

        responseRepository.save(ncbsResponse);

        return ncbsResponse;
    }
}
```

### SKN RTGS
```java
@Component
@RequiredArgsConstructor
public class SknRtgsExecutor extends BaseTransferExecutor {

    private final MiddlewareService middleware;

    @Override
    public boolean supports(TransferMethod method) {
        return method == TransferMethod.SKN || method == TransferMethod.RTGS;
    }

    @Override
    public NcbsResponse execute(Transferable item) {

        String refId = UUID.randomUUID().toString();

        SknRtgsTransferRequest request = SknRtgsTransferRequest.builder()
                .debitAccount(item.getDebitAccount())
                .beneficiaryAccount(item.getCreditAccount())
                .beneficiaryName(item.getBeneficiaryName())
                .bankCode(item.getBankCode())
                .amount(item.getAmount())
                .remark(item.getDescription())
                .build();

        NcbsRequest ncbsRequest =
                createNcbsRequest(item, MiddlewareServiceType.TRANSFER_SKN_RTGS, refId);

        requestRepository.save(ncbsRequest);

        SknRtgsTransferResponse response =
                middleware.transferSknRtgs(refId, request);

        NcbsResponse ncbsResponse =
                createNcbsResponse(ncbsRequest, response);

        responseRepository.save(ncbsResponse);

        return ncbsResponse;
    }
}
```

### BI-FAST 2 STEP

```java
@Component
@RequiredArgsConstructor
public class BiFastExecutor extends BaseTransferExecutor {

    private final MiddlewareService middleware;
    private final ResponseCodeService responseCodeService;

    @Override
    public boolean supports(TransferMethod method) {
        return method == TransferMethod.BIFAST;
    }

    @Override
    public NcbsResponse execute(Transferable item) {

        // ===== INQUIRY =====
        String inquiryRef = UUID.randomUUID().toString();

        InquiryAccountRequest inquiryReq = InquiryAccountRequest.builder()
                .accountNumber(item.getCreditAccount())
                .bankCode(item.getBankCode())
                .amount(item.getAmount())
                .build();

        NcbsRequest inquiryNcbs =
                createNcbsRequest(item, MiddlewareServiceType.INQUIRY_ACCOUNT, inquiryRef);

        requestRepository.save(inquiryNcbs);

        InquiryAccountResponse inquiryRes =
                middleware.inquiryAccount(inquiryReq, inquiryRef);

        NcbsResponse inquiryResponse =
                createNcbsResponse(inquiryNcbs, inquiryRes);

        responseRepository.save(inquiryResponse);

        if (!responseCodeService.isSuccess(inquiryRes.getResponseCode())) {
            return inquiryResponse;
        }

        // ===== TRANSFER =====
        String transferRef = UUID.randomUUID().toString();

        CreditTransferBiFastRequest transferReq =
                CreditTransferBiFastRequest.builder()
                        .beneficiaryName(inquiryRes.getData().getAccountName())
                        .beneficiaryAccount(item.getCreditAccount())
                        .bankCode(item.getBankCode())
                        .amount(item.getAmount())
                        .remark(item.getDescription())
                        .build();

        NcbsRequest transferNcbs =
                createNcbsRequest(item, MiddlewareServiceType.TRANSFER_SKN_RTGS, transferRef);

        requestRepository.save(transferNcbs);

        CreditTransferResponse transferRes =
                middleware.creditTransfer(transferRef, transferReq);

        NcbsResponse transferResponse =
                createNcbsResponse(transferNcbs, transferRes);

        responseRepository.save(transferResponse);

        return transferResponse;
    }
}
```

## 6. ORCHESTRATOR (GENERIC CORE)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferOrchestratorService {

    private final List<TransferExecutor> executors;
    private final ResponseCodeService responseCodeService;

    public <T extends Transferable> ProcessResult process(List<T> items) {

        ProcessResult result = new ProcessResult();

        for (T item : items) {

            try {

                TransferExecutor executor = executors.stream()
                        .filter(e -> e.supports(item.getTransferMethod()))
                        .findFirst()
                        .orElseThrow();

                NcbsResponse response = executor.execute(item);

                result.addSuccess();

            } catch (Exception e) {

                log.error("Transfer failed id {}", item.getId(), e);

                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                List.of(e.getMessage())
                        )
                );
            }
        }

        return result;
    }
}
```

## 7. SERVICE (PER DOMAIN)

### Management Fee
```java
@Service
@RequiredArgsConstructor
public class ManagementFeeMapService {

    private final ManagementFeeMapRepository repo;
    private final TransferOrchestratorService orchestrator;

    public ProcessResult send(List<Long> ids) {
        return orchestrator.process(repo.findAllById(ids));
    }
}
```

### TaxBrokerFee

```java
@Service
@RequiredArgsConstructor
public class TaxBrokerFeeService {

    private final TaxBrokerFeeRepository repo;
    private final TransferOrchestratorService orchestrator;

    public ProcessResult send(List<Long> ids) {
        return orchestrator.process(repo.findAllById(ids));
    }
}
```

### Transferable final

```java
public interface Transferable {

    Long getId();

    String getDebitAccount();

    String getCreditAccount();

    BigDecimal getAmount();

    TransferMethod getTransferMethod();

    TransferScope getTransferScope();
}
```

# Rabu 15 April 2026

## Struktur Folder
```bash
com.yourapp
│
├── domain
│   └── entity
│       ├── ManagementFeeMap.java
│       ├── TaxBrokerFee.java
│       ├── NcbsRequest.java
│       └── NcbsResponse.java
│
├── application
│   ├── dto
│   │   ├── Transferable.java
│   │   └── TransferableAdapter.java
│   │
│   ├── mapper
│   │   └── TransferableMapper.java
│   │
│   ├── executor
│   │   ├── TransferExecutor.java
│   │   ├── BaseTransferExecutor.java
│   │   └── OverbookingExecutor.java
│   │
│   └── orchestrator
│       └── TransferOrchestratorService.java
│
├── infrastructure
│   ├── repository
│   └── service
│       └── MiddlewareService.java
│
└── service
    ├── ManagementFeeMapService.java
    └── TaxBrokerFeeService.java
```

## 1. Transferable

```java
public interface Transferable {

    Long getId();

    String getDebitAccount();

    String getCreditAccount();

    BigDecimal getAmount();

    String getDescription();

    TransferMethod getTransferMethod();

    TransferScope getTransferScope();
}
```
## 2. Transferable Adapter

```java
public class TransferableAdapter implements Transferable {

    private Long id;
    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String description;
    private TransferMethod transferMethod;
    private TransferScope transferScope;

    public TransferableAdapter() {}

    public TransferableAdapter(Long id,
                               String debitAccount,
                               String creditAccount,
                               BigDecimal amount,
                               String description,
                               TransferMethod transferMethod,
                               TransferScope transferScope) {
        this.id = id;
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
        this.amount = amount;
        this.description = description;
        this.transferMethod = transferMethod;
        this.transferScope = transferScope;
    }

    public Long getId() { return id; }
    public String getDebitAccount() { return debitAccount; }
    public String getCreditAccount() { return creditAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public TransferMethod getTransferMethod() { return transferMethod; }
    public TransferScope getTransferScope() { return transferScope; }

    public void setId(Long id) { this.id = id; }
    public void setDebitAccount(String debitAccount) { this.debitAccount = debitAccount; }
    public void setCreditAccount(String creditAccount) { this.creditAccount = creditAccount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setTransferMethod(TransferMethod transferMethod) { this.transferMethod = transferMethod; }
    public void setTransferScope(TransferScope transferScope) { this.transferScope = transferScope; }
}
```

## 3. TransferableMapper

```java
@Component
public class TransferableMapper {

    public Transferable fromManagementFee(ManagementFeeMap e) {

        TransferableAdapter dto = new TransferableAdapter();

        dto.setId(e.getId());
        dto.setDebitAccount(e.getDebitAccount());
        dto.setCreditAccount(e.getCreditAccount());
        dto.setAmount(e.getAmount());
        dto.setDescription(e.getDescription());
        dto.setTransferMethod(e.getTransferMethod());
        dto.setTransferScope(e.getTransferScope());

        return dto;
    }

    public Transferable fromTaxBroker(TaxBrokerFee e) {

        TransferableAdapter dto = new TransferableAdapter();

        dto.setId(e.getId());
        dto.setDebitAccount(e.getCashAccount());
        dto.setCreditAccount(e.getDestinationAccount());
        dto.setAmount(e.getTaxAmount());
        dto.setDescription("Tax Broker Fee");
        dto.setTransferMethod(e.getTransferMethod());
        dto.setTransferScope(e.getTransferScope());

        return dto;
    }
}
```

## 4. TransferExecutor

```java
public interface TransferExecutor {

    boolean supports(TransferMethod method);

    NcbsResponse execute(Transferable item);
}
```

## 5. BaseTransferExecutor
```java
public abstract class BaseTransferExecutor implements TransferExecutor {

    protected final NcbsRequestRepository requestRepository;
    protected final NcbsResponseRepository responseRepository;
    protected final ObjectMapper objectMapper;

    protected BaseTransferExecutor(NcbsRequestRepository requestRepository,
                                   NcbsResponseRepository responseRepository,
                                   ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.responseRepository = responseRepository;
        this.objectMapper = objectMapper;
    }

    protected NcbsRequest createRequest(Transferable item,
                                        MiddlewareServiceType service,
                                        String refId) {

        NcbsRequest req = new NcbsRequest();
        req.setReferenceId(refId);
        req.setEntityId(item.getId());
        req.setCreatedDate(LocalDateTime.now());
        req.setTransferMethod(item.getTransferMethod());
        req.setTransferScope(item.getTransferScope());
        req.setService(service);

        return req;
    }

    protected NcbsResponse createResponse(NcbsRequest req, Object res) {

        NcbsResponse response = new NcbsResponse();

        try {
            response.setReferenceId(req.getReferenceId());
            response.setEntityId(req.getEntityId());
            response.setCreatedDate(LocalDateTime.now());
            response.setJsonResponse(objectMapper.writeValueAsString(res));
            response.setResponseCode(extractCode(res));
            response.setResponseMessage(extractMessage(res));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private String extractCode(Object res) {

        if (res instanceof OverbookingResponse) {
            return ((OverbookingResponse) res).getResponseCode();
        }

        if (res instanceof SknRtgsTransferResponse) {
            return ((SknRtgsTransferResponse) res).getResponseCode();
        }

        return null;
    }

    private String extractMessage(Object res) {

        if (res instanceof OverbookingResponse) {
            return ((OverbookingResponse) res).getResponseMessage();
        }

        if (res instanceof SknRtgsTransferResponse) {
            return ((SknRtgsTransferResponse) res).getResponseMessage();
        }

        return null;
    }
}
```

## 6. Overbooking Executor

```java
@Component
public class OverbookingExecutor extends BaseTransferExecutor {

    private final MiddlewareService middleware;

    public OverbookingExecutor(NcbsRequestRepository requestRepository,
                               NcbsResponseRepository responseRepository,
                               ObjectMapper objectMapper,
                               MiddlewareService middleware) {
        super(requestRepository, responseRepository, objectMapper);
        this.middleware = middleware;
    }

    @Override
    public boolean supports(TransferMethod method) {
        return TransferMethod.OVERBOOKING.equals(method);
    }

    @Override
    public NcbsResponse execute(Transferable item) {

        String refId = UUID.randomUUID().toString();

        OverbookingRequest req = new OverbookingRequest();
        req.setDebitAccount(item.getDebitAccount());
        req.setCreditAccount(item.getCreditAccount());
        req.setAmount(item.getAmount());
        req.setRemark(item.getDescription());

        NcbsRequest ncbsReq = createRequest(item, MiddlewareServiceType.OVERBOOKING_CASA, refId);
        requestRepository.save(ncbsReq);

        OverbookingResponse res = middleware.overbooking(refId, req);

        NcbsResponse ncbsRes = createResponse(ncbsReq, res);
        responseRepository.save(ncbsRes);

        return ncbsRes;
    }
}
```

## 7. TransferOrchestratorService

```java
@Service
public class TransferOrchestratorService {

    private final List<TransferExecutor> executors;

    public TransferOrchestratorService(List<TransferExecutor> executors) {
        this.executors = executors;
    }

    public <T extends Transferable> ProcessResult process(List<T> items) {

        ProcessResult result = new ProcessResult();

        for (T item : items) {

            try {

                TransferExecutor executor = null;

                for (TransferExecutor e : executors) {
                    if (e.supports(item.getTransferMethod())) {
                        executor = e;
                        break;
                    }
                }

                if (executor == null) {
                    throw new IllegalStateException("No executor found");
                }

                executor.execute(item);

                result.addSuccess();

            } catch (Exception e) {

                result.addError(
                        ErrorDetail.of(
                                "id",
                                String.valueOf(item.getId()),
                                Collections.singletonList(e.getMessage())
                        )
                );
            }
        }

        return result;
    }
}
```

## 8. Service ManagementFee

```java
@Service
public class ManagementFeeMapService {

    private final ManagementFeeMapRepository repo;
    private final TransferableMapper mapper;
    private final TransferOrchestratorService orchestrator;

    public ManagementFeeMapService(ManagementFeeMapRepository repo,
                                   TransferableMapper mapper,
                                   TransferOrchestratorService orchestrator) {
        this.repo = repo;
        this.mapper = mapper;
        this.orchestrator = orchestrator;
    }

    public ProcessResult send(List<Long> ids) {

        List<ManagementFeeMap> data = repo.findAllById(ids);

        List<Transferable> list = new ArrayList<Transferable>();

        for (ManagementFeeMap e : data) {
            list.add(mapper.fromManagementFee(e));
        }

        return orchestrator.process(list);
    }
}
```
## 9. Service TaxBrokerFee

```java
@Service
public class TaxBrokerFeeService {

    private final TaxBrokerFeeRepository repo;
    private final TransferableMapper mapper;
    private final TransferOrchestratorService orchestrator;

    public TaxBrokerFeeService(TaxBrokerFeeRepository repo,
                               TransferableMapper mapper,
                               TransferOrchestratorService orchestrator) {
        this.repo = repo;
        this.mapper = mapper;
        this.orchestrator = orchestrator;
    }

    public ProcessResult send(List<Long> ids) {

        List<TaxBrokerFee> data = repo.findAllById(ids);

        List<Transferable> list = new ArrayList<Transferable>();

        for (TaxBrokerFee e : data) {
            list.add(mapper.fromTaxBroker(e));
        }

        return orchestrator.process(list);
    }
}
```