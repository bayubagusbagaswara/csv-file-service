package com.bayu.csvfileservice.util;

public class JsonViews {
    // Untuk query biasa
    public interface Query {}

    // Base audit view
    public interface Audit {}

    // Untuk ADD operation - TIDAK menampilkan id, month, year
    public interface AuditForAdd extends Audit {}

    // Untuk EDIT operation - menampilkan id, month, year
    public interface AuditForEdit extends Audit {}

    // Untuk DELETE operation - menampilkan id, month, year
    public interface AuditForDelete extends Audit {}

}
