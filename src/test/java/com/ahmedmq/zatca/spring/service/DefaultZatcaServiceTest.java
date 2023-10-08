package com.ahmedmq.zatca.spring.service;

import com.ahmedmq.zatca.ZatcaProperties;
import com.ahmedmq.zatca.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultZatcaServiceTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    ZatcaProperties zatcaProperties;

    @Captor
    ArgumentCaptor<HttpEntity<Object>> httpEntityArgumentCaptor;

    @InjectMocks
    DefaultZatcaService sut;

    @BeforeEach
    void setUp() {
        when(zatcaProperties.apiVersion()).thenReturn("V2");
    }

    @Test
    void complianceCSID() {
        CSIDResponse csidResponse = new CSIDResponse("123", "ISSUED", "certificate", "secret");
        CSRRequest csrRequest = new CSRRequest("csr");
        when(restTemplate.postForObject(eq("/compliance"), httpEntityArgumentCaptor.capture(), eq(CSIDResponse.class))).thenReturn(csidResponse);

        CSIDResponse response = sut.complianceCSID(csrRequest, "999");

        assertThat(response).isEqualTo(csidResponse);
        HttpEntity<Object> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get("OTP")).containsExactly("999");
        assertThat(httpEntity.getHeaders().get("Accept-Version")).containsExactly("V2");
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getBody()).isEqualTo(csrRequest);
    }

    @Test
    void checkInvoiceCompliance() {
        ValidationResult validationResult = new ValidationResult(List.of(), List.of(), List.of(), "PASS");
        InvoiceComplianceResponse invoiceComplianceResponse = new InvoiceComplianceResponse(validationResult,
                "PASS","REPORTED","","","");
        InvoiceRequest invoiceRequest = new InvoiceRequest("hash","","xml");
        when(restTemplate.postForObject(eq("/compliance/invoices"), httpEntityArgumentCaptor.capture(), eq(InvoiceComplianceResponse.class))).thenReturn(invoiceComplianceResponse);

        InvoiceComplianceResponse response = sut.checkInvoiceCompliance("certificate", "secret", invoiceRequest);

        assertThat(response).isEqualTo(invoiceComplianceResponse);
        HttpEntity<Object> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().getAcceptLanguage()).containsExactlyElementsOf(Locale.LanguageRange.parse("en"));
        assertThat(httpEntity.getHeaders().get("Accept-Version")).containsExactly("V2");
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getHeaders().getFirst("Authorization")).isEqualTo("Basic Y2VydGlmaWNhdGU6c2VjcmV0");
        assertThat(httpEntity.getBody()).isEqualTo(invoiceRequest);
    }

    @Test
    void productionCSID() {
        CSIDResponse csidResponse = new CSIDResponse("456",
                "ISSUED", "productionToken", "productionSecret");
        ProductionCSIDRequest productionCSIDRequest = new ProductionCSIDRequest("123");
        when(restTemplate.postForObject(eq("/production/csids"), httpEntityArgumentCaptor.capture(), eq(CSIDResponse.class))).thenReturn(csidResponse);

        CSIDResponse response = sut.productionCSID("certificate", "secret", productionCSIDRequest);

        assertThat(response).isEqualTo(csidResponse);
        HttpEntity<Object> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get("Accept-Version")).containsExactly("V2");
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getHeaders().getFirst("Authorization")).isEqualTo("Basic Y2VydGlmaWNhdGU6c2VjcmV0");
        assertThat(httpEntity.getBody()).isEqualTo(productionCSIDRequest);
    }

    @Test
    void reportInvoice() {
        InvoiceResult invoiceResult = new InvoiceResult("hash", "REPORTED", List.of(), List.of());
        InvoiceRequest invoiceRequest = new InvoiceRequest("hash", "","invoice");
        when(restTemplate.postForObject(eq("/invoices/reporting/single"), httpEntityArgumentCaptor.capture(), eq(InvoiceResult.class))).thenReturn(invoiceResult);

        InvoiceResult response = sut.reportInvoice("certificate", "secret", invoiceRequest);

        assertThat(response).isEqualTo(invoiceResult);
        HttpEntity<Object> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().getAcceptLanguage()).containsExactlyElementsOf(Locale.LanguageRange.parse("en"));
        assertThat(httpEntity.getHeaders().get("Accept-Version")).containsExactly("V2");
        assertThat(httpEntity.getHeaders().get("Clearance-Status")).containsExactly("0");
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getHeaders().getFirst("Authorization")).isEqualTo("Basic Y2VydGlmaWNhdGU6c2VjcmV0");
        assertThat(httpEntity.getBody()).isEqualTo(invoiceRequest);
    }

    @Test
    void renewProductionCSID() {
        CSIDResponse csidResponse = new CSIDResponse("456",
                "ISSUED", "productionToken", "productionSecret");
        CSRRequest csrRequest = new CSRRequest("csr");
        when(restTemplate.patchForObject(eq("/production/csids"), httpEntityArgumentCaptor.capture(), eq(CSIDResponse.class))).thenReturn(csidResponse);

        CSIDResponse response = sut.renewProductionCSID(csrRequest, "999");

        assertThat(response).isEqualTo(csidResponse);
        HttpEntity<Object> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().get("OTP")).containsExactly("999");
        assertThat(httpEntity.getHeaders().get("Accept-Version")).containsExactly("V2");
        assertThat(httpEntity.getHeaders().getAcceptLanguage()).containsExactlyElementsOf(Locale.LanguageRange.parse("en"));
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getBody()).isEqualTo(csrRequest);
    }

    @Test
    void clearedInvoice() {
        ClearedInvoiceResult clearanceResponse = new ClearedInvoiceResult("hash",
                "invoice", "CLEARED", List.of(), List.of());
        InvoiceRequest invoiceRequest = new InvoiceRequest("hash", "","invoice");
        when(restTemplate.postForObject(eq("/invoices/clearance/single"), httpEntityArgumentCaptor.capture(), eq(ClearedInvoiceResult.class))).thenReturn(clearanceResponse);

        ClearedInvoiceResult response = sut.clearedInvoice("certificate", "secret", invoiceRequest);

        assertThat(response).isEqualTo(clearanceResponse);
        HttpEntity<Object> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity.getHeaders().getAcceptLanguage()).containsExactlyElementsOf(Locale.LanguageRange.parse("en"));
        assertThat(httpEntity.getHeaders().get("Accept-Version")).containsExactly("V2");
        assertThat(httpEntity.getHeaders().get("Clearance-Status")).containsExactly("0");
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getHeaders().getFirst("Authorization")).isEqualTo("Basic Y2VydGlmaWNhdGU6c2VjcmV0");
        assertThat(httpEntity.getBody()).isEqualTo(invoiceRequest);
    }
}