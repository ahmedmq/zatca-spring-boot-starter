package com.ahmedmq.zatca.model;

import java.util.List;

public record ClearedInvoiceResult(String invoiceHash,
                                   String clearedInvoice,
                                   String status,
                                   List<Warning> warnings,
                                   List<java.lang.Error> errors) {
}
