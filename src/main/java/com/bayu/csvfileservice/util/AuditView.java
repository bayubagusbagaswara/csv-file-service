package com.bayu.csvfileservice.util;

public class AuditView {

    public static class Before {}

    // After extends Before → supaya include semua field Before
    public static class After extends Before {}

}