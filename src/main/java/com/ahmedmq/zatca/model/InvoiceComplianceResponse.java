package com.ahmedmq.zatca.model;

public record InvoiceComplianceResponse(ValidationResult validationResults,
                                        String status,
                                        String reportingStatus,
                                        String clearanceStatus,
                                        String qrSellertStatus,
                                        String qrBuyertStatus) {
}
