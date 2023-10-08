package com.ahmedmq.zatca.spring.service;


import com.ahmedmq.zatca.model.*;

public interface ZatcaService {

    CSIDResponse complianceCSID(CSRRequest CSRRequest,
                                String otp);

    InvoiceComplianceResponse checkInvoiceCompliance(String userName,
                                           String password,
                                           InvoiceRequest invoiceRequest);

    CSIDResponse productionCSID(String certificate,
                                          String secret,
                                          ProductionCSIDRequest productionCSIDRequest);

    InvoiceResult reportInvoice(String certificate,
                                String secret,
                                InvoiceRequest invoiceRequest);

    CSIDResponse renewProductionCSID(CSRRequest CSRRequest,
                                     String otp);

    ClearedInvoiceResult clearedInvoice(String certificate,
                                          String secret,
                                          InvoiceRequest invoiceRequest);

}
