package com.ahmedmq.zatca.model;

public record InvoiceRequest(String invoiceHash,
                             String uuid,
                             String invoice) {
}
