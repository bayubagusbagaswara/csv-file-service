package com.bayu.csvfileservice.model.enumerator;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Month {

    JANUARY("January"),
    FEBRUARY("February"),
    MARCH("March"),
    APRIL("April"),
    MAY("May"),
    JUNE("June"),
    JULY("July"),
    AUGUST("August"),
    SEPTEMBER("September"),
    OCTOBER("October"),
    NOVEMBER("November"),
    DECEMBER("December");

    private final String label;

    Month(String label) {
        this.label = label;
    }

    @JsonValue  // ← Tambahkan ini! Jackson akan pakai label untuk serialisasi
    public String getLabel() {
        return label;
    }

    public static Month fromLabel(String label) {
        for (Month m : values()) {
            if (m.label.equalsIgnoreCase(label)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid month: " + label);
    }

}
