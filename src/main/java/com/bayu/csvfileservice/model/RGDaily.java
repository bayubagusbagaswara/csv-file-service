package com.bayu.csvfileservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "rg_daily")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RGDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private Integer number;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "product")
    private String product;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "price_formatter")
    private String priceFormatter;

    @Column(name = "price_a")
    private BigDecimal priceA;

    @Column(name = "price_b")
    private Integer priceB;
}
