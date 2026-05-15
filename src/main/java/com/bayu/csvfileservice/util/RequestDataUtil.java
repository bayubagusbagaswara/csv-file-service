package com.bayu.csvfileservice.util;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class RequestDataUtil {

    public static void trimIfNotNull(Supplier<String> getter, Consumer<String> setter) {
        String value = getter.get();
        if (value != null) {
            setter.accept(value.trim());
        }
    }

    public static void setIfNullOrEmpty(Supplier<String> getter, Consumer<String> setter, String entityValue) {
        if (getter.get() == null || getter.get().isEmpty()) {
            setter.accept(entityValue);
        }
    }

}
