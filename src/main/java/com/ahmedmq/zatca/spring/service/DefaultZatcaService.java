package com.ahmedmq.zatca.spring.service;

import com.ahmedmq.zatca.ZatcaProperties;
import com.ahmedmq.zatca.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

class DefaultZatcaService implements ZatcaService {
    private final RestTemplate restTemplate;
    private final ZatcaProperties zatcaProperties;

    public DefaultZatcaService(RestTemplate restTemplate, ZatcaProperties zatcaProperties) {
        this.restTemplate = restTemplate;
        this.zatcaProperties = zatcaProperties;
    }

    @Override
    public CSIDResponse complianceCSID(CSRRequest CSRRequest, String otp) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("OTP", otp);
        headers.set("Accept-Version", zatcaProperties.apiVersion());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CSRRequest> request = new HttpEntity<>(CSRRequest, headers);
        return restTemplate.postForObject("/compliance", request, CSIDResponse.class);

    }

    @Override
    public InvoiceComplianceResponse checkInvoiceCompliance(String userName, String password, InvoiceRequest invoiceRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguage(Locale.LanguageRange.parse("en"));
        headers.set("Accept-Version", zatcaProperties.apiVersion());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(userName, password);
        HttpEntity<InvoiceRequest> request = new HttpEntity<>(invoiceRequest, headers);
        return restTemplate.postForObject("/compliance/invoices", request, InvoiceComplianceResponse.class);
    }

    @Override
    public CSIDResponse productionCSID(String certificate, String secret, ProductionCSIDRequest productionCSIDRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Version", zatcaProperties.apiVersion());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(certificate, secret);
        HttpEntity<ProductionCSIDRequest> request = new HttpEntity<>(productionCSIDRequest, headers);
        return restTemplate.postForObject("/production/csids", request, CSIDResponse.class);
    }

    @Override
    public InvoiceResult reportInvoice(String certificate, String secret, InvoiceRequest invoiceRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguage(Locale.LanguageRange.parse("en"));
        headers.set("Accept-Version", zatcaProperties.apiVersion());
        headers.set("Clearance-Status", "0");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(certificate, secret);
        HttpEntity<InvoiceRequest> request = new HttpEntity<>(invoiceRequest, headers);
        return restTemplate.postForObject("/invoices/reporting/single", request, InvoiceResult.class);
    }

    @Override
    public CSIDResponse renewProductionCSID(CSRRequest CSRRequest, String otp) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("OTP", otp);
        headers.set("Accept-Version", zatcaProperties.apiVersion());
        headers.setAcceptLanguage(Locale.LanguageRange.parse("en"));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CSRRequest> request = new HttpEntity<>(CSRRequest, headers);
        return restTemplate.patchForObject("/production/csids", request, CSIDResponse.class);
    }

    @Override
    public ClearedInvoiceResult clearedInvoice(String certificate, String secret, InvoiceRequest invoiceRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAcceptLanguage(Locale.LanguageRange.parse("en"));
        headers.set("Accept-Version", zatcaProperties.apiVersion());
        headers.set("Clearance-Status", "0");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(certificate, secret);
        HttpEntity<InvoiceRequest> request = new HttpEntity<>(invoiceRequest, headers);
        return restTemplate.postForObject("/invoices/clearance/single", request, ClearedInvoiceResult.class);
    }
}
