package com.smartprocure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vendor quotation for a procurement request. One-to-One with Order when approved.
 */
@Entity
@Table(
    name = "quotations",
    indexes = {
        @jakarta.persistence.Index(name = "idx_quotations_request_id", columnList = "request_id"),
        @jakarta.persistence.Index(name = "idx_quotations_vendor_id", columnList = "vendor_id"),
        @jakarta.persistence.Index(name = "idx_quotations_status", columnList = "status")
    }
)
public class Quotation extends BaseEntity {

    @Column(name = "quoted_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal quotedAmount;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private QuotationStatus status = QuotationStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ProcurementRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @OneToOne(mappedBy = "quotation", fetch = FetchType.LAZY)
    private Order order;

    public BigDecimal getQuotedAmount() {
        return quotedAmount;
    }

    public void setQuotedAmount(BigDecimal quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    public ProcurementRequest getRequest() {
        return request;
    }

    public void setRequest(ProcurementRequest request) {
        this.request = request;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
