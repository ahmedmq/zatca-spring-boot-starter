package com.ahmedmq.zatca.model;

import java.util.List;

public record InvoiceResult(String invoiceHash,
                            String status,
                            List<Warning> warnings,
                            List<Error> errors) {
}
