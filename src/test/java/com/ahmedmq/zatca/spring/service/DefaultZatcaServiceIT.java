package com.ahmedmq.zatca.spring.service;

import com.ahmedmq.zatca.model.Error;
import com.ahmedmq.zatca.model.*;
import com.ahmedmq.zatca.spring.service.sample.ZatcaSampleApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(classes = ZatcaSampleApp.class)
public class DefaultZatcaServiceIT {

    static int MOCK_SERVER_PORT;

    static {
        try (var serverSocket = new ServerSocket(0)) {
            MOCK_SERVER_PORT = serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    ZatcaService zatcaService;

    MockWebServer mockWebServer;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(MOCK_SERVER_PORT);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("zatca.baseUrl", () -> "http://localhost:" + MOCK_SERVER_PORT);
    }

    @Nested
    class ComplianceCSID {

        CSRRequest csrRequest = new CSRRequest("csr");
        String csrRequestJson;

        @BeforeEach
        void setUp() throws JsonProcessingException {
            csrRequestJson = objectMapper.writeValueAsString(csrRequest);
        }

        @Test
        void testComplianceApi200() throws IOException, InterruptedException {
            CSIDResponse csidResponse = new CSIDResponse("123", "ISSUED", "token", "secret");
            String csidResponseJson = objectMapper.writeValueAsString(csidResponse);
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(csidResponseJson));

            CSIDResponse actualCsidResponse = zatcaService.complianceCSID(csrRequest, "123");

            assertThat(actualCsidResponse).isEqualTo(csidResponse);
            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath()).isEqualTo("/compliance");

        }

        @Test
        void testComplianceCSID400() throws JsonProcessingException {
            CertificatesErrorsResponse errorsResponse = new CertificatesErrorsResponse(
                    List.of(new Error(null, "Missing-OTP", "OTP is required field")));
            String csidResponseJson = objectMapper.writeValueAsString(errorsResponse);

            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setStatus("HTTP/1.1 400 Bad Request")
                    .setBody(csidResponseJson));

            ZatcaClientApiException zatcaClientApiException = catchThrowableOfType(() ->
                    zatcaService.complianceCSID(csrRequest, ""), ZatcaClientApiException.class);


            assertThat(zatcaClientApiException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(zatcaClientApiException.getStatusText()).isEqualTo("Bad Request");
            assertThat(zatcaClientApiException.getResponseBody()).isEqualTo(csidResponseJson);

            CertificatesErrorsResponse response = objectMapper.readValue(zatcaClientApiException.getResponseBody(), CertificatesErrorsResponse.class);
            assertThat(response).isEqualTo(errorsResponse);
        }
    }

    @Nested
    class InvoiceReporting {
        InvoiceRequest invoiceRequest = new InvoiceRequest("hash", "", "invoice");
        String invoiceReportRequestJson;

        @BeforeEach
        void setUp() throws JsonProcessingException {
            invoiceReportRequestJson = objectMapper.writeValueAsString(invoiceRequest);
        }

        @Test
        void testReportInvoiceWhen200() throws JsonProcessingException {

            InvoiceResult invoiceResult = new InvoiceResult("hash", "REPORTED", List.of(), List.of());
            String invoiceReportResponseJson = objectMapper.writeValueAsString(invoiceResult);
            mockWebServer
                    .enqueue(new MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(invoiceReportResponseJson));

            InvoiceResult actualInvoiceResult = zatcaService
                    .reportInvoice("certificate", "secret", invoiceRequest);

            assertThat(actualInvoiceResult).isEqualTo(invoiceResult);
        }

        @Test
        void testReportInvoice202() throws JsonProcessingException {
            Warning warning = new Warning("Seller-Address",
                    "BR-KSA-09",
                    "Seller address must contain additional number (KSA-23), street name (BT-35), building number (KSA-17), postal code (BT-38), city (BT-37), Neighborhood (KSA-3), country code (BT-40).");
            InvoiceResult invoiceResult = new InvoiceResult("hash", "REPORTED", List.of(warning), null);
            String invoiceReportResponseJson = objectMapper.writeValueAsString(invoiceResult);
            mockWebServer
                    .enqueue(new MockResponse()
                            .setResponseCode(202)
                            .setHeader("Content-Type", "application/json")
                            .setBody(invoiceReportResponseJson));

            InvoiceResult actualInvoiceResult = zatcaService.reportInvoice("certificate", "secret", invoiceRequest);

            assertThat(actualInvoiceResult).isEqualTo(invoiceResult);

        }

        @Test
        void testReportInvoiceWhen400() throws JsonProcessingException {
            Error error = new Error("INVOICE_ERRORS",
                    "Invalid-Invoice-Hash",
                    "The provided invoice hash is invalid");
            InvoiceResult invoiceResult = new InvoiceResult("hash", "REPORTED", null, List.of(error));
            String invoiceReportResponseJson = objectMapper.writeValueAsString(invoiceResult);
            mockWebServer
                    .enqueue(new MockResponse()
                            .setStatus("HTTP/1.1 400 Bad Request")
                            .setHeader("Content-Type", "application/json")
                            .setBody(invoiceReportResponseJson));

            ZatcaClientApiException zatcaClientApiException = catchThrowableOfType(() -> zatcaService
                    .reportInvoice("certificate", "secret", invoiceRequest), ZatcaClientApiException.class);

            assertThat(zatcaClientApiException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(zatcaClientApiException.getStatusText()).isEqualTo("Bad Request");
            assertThat(zatcaClientApiException.getResponseBody()).isEqualTo(invoiceReportResponseJson);

            InvoiceResult response = objectMapper.readValue(zatcaClientApiException.getResponseBody(), InvoiceResult.class);
            assertThat(response).isEqualTo(invoiceResult);
        }

        @Test
        void testReportInvoiceWhen401() {
            String invoiceReportResponseJson = """
                    {
                      "timestamp": 1654514661409,
                      "status": 401,
                      "error": "Unauthorized",
                      "message": ""
                    }""";

            mockWebServer
                    .enqueue(new MockResponse()
                            .setStatus("HTTP/1.1 401 Unauthorized")
                            .setHeader("Content-Type", "application/json")
                            .setBody(invoiceReportResponseJson));


            ZatcaClientApiException zatcaClientApiException = catchThrowableOfType(() -> zatcaService
                    .reportInvoice("certificate", "secret", invoiceRequest), ZatcaClientApiException.class);

            assertThat(zatcaClientApiException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(zatcaClientApiException.getStatusText()).isEqualTo("Unauthorized");
            assertThat(zatcaClientApiException.getResponseBody()).isEqualTo(invoiceReportResponseJson);
        }

        @Test
        void testReportInvoiceWhen500() {

            String invoiceReportResponseJson = """
                    {
                       "category": "HTTP-Errors",
                       "code": "500",
                       "message": "Something went wrong and caused an Internal Server Error."
                     }""";

            mockWebServer
                    .enqueue(new MockResponse()
                            .setStatus("HTTP/1.1 500 Internal Server Error")
                            .setHeader("Content-Type", "application/json")
                            .setBody(invoiceReportResponseJson));


            ZatcaClientApiException zatcaClientApiException = catchThrowableOfType(() -> zatcaService
                    .reportInvoice("certificate", "secret", invoiceRequest), ZatcaClientApiException.class);

            assertThat(zatcaClientApiException.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(zatcaClientApiException.getStatusText()).isEqualTo("Internal Server Error");
            assertThat(zatcaClientApiException.getResponseBody()).isEqualTo(invoiceReportResponseJson);

        }
    }

    @Nested
    @DisplayName("Production CSID")
    class ProductionCSIDTest {

        ProductionCSIDRequest csidRequest = new ProductionCSIDRequest("123");
        String csidRequestJson;

        @BeforeEach
        void setUp() throws JsonProcessingException {
            csidRequestJson = objectMapper.writeValueAsString(csidRequest);
        }

        @Test
        void testProductionCSID200() throws JsonProcessingException {
            CSIDResponse csidResponse = new CSIDResponse("456",
                    "ISSUED", "productionToken", "productionSecret");

            String csidResponseJson = objectMapper.writeValueAsString(csidResponse);
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(csidResponseJson));


            CSIDResponse actualCsidResponse = zatcaService.productionCSID("certificate", "secret", csidRequest);

            assertThat(actualCsidResponse).isEqualTo(csidResponse);
        }

        @Test
        void testProductionCSID400() throws JsonProcessingException {
            CertificatesErrorsResponse errorsResponse = new CertificatesErrorsResponse(
                    List.of(new Error(null, "Missing-OTP", "OTP is required field")));
            String csidResponseJson = objectMapper.writeValueAsString(errorsResponse);
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setStatus("HTTP/1.1 400 Bad Request")
                    .setBody(csidResponseJson));

            ZatcaClientApiException zatcaClientApiException = catchThrowableOfType(() ->
                            zatcaService.productionCSID("certificate", "secret", csidRequest),
                    ZatcaClientApiException.class);

            assertThat(zatcaClientApiException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(zatcaClientApiException.getStatusText()).isEqualTo("Bad Request");
            assertThat(zatcaClientApiException.getResponseBody()).isEqualTo(csidResponseJson);
        }
    }

    @Nested
    @DisplayName("Invoice Clearance")
    class InvoiceClearanceTest {

        InvoiceRequest invoiceRequest = new InvoiceRequest("hash", "", "invoice");
        String invoiceRequestJson;

        @BeforeEach
        void setUp() throws JsonProcessingException {
            invoiceRequestJson = objectMapper.writeValueAsString(invoiceRequest);
        }

        @Test
        void testInvoiceClearance200() throws JsonProcessingException {
            ClearedInvoiceResult clearanceResponse = new ClearedInvoiceResult("hash",
                    "invoice", "CLEARED", List.of(), List.of());

            String clearanceResponseJson = objectMapper.writeValueAsString(clearanceResponse);
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(clearanceResponseJson));

            ClearedInvoiceResult actualClearanceResponse = zatcaService
                    .clearedInvoice("certificate", "secret", invoiceRequest);

            assertThat(actualClearanceResponse).isEqualTo(clearanceResponse);
        }

        @Test
        void testInvoiceClearance202() throws JsonProcessingException {
            Warning warning = new Warning("Seller-Address",
                    "BR-KSA-09",
                    "Seller address must contain additional number (KSA-23), street name (BT-35), building number (KSA-17), postal code (BT-38), city (BT-37), Neighborhood (KSA-3), country code (BT-40).");
            ClearedInvoiceResult clearanceResponse = new ClearedInvoiceResult("hash", null, "Cleared", List.of(warning), null);
            String invoiceReportResponseJson = objectMapper.writeValueAsString(clearanceResponse);
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(202)
                    .setHeader("Content-Type", "application/json")
                    .setBody(invoiceReportResponseJson));

            ClearedInvoiceResult actualClearanceResponse = zatcaService.clearedInvoice("certificate", "secret", invoiceRequest);

            assertThat(actualClearanceResponse).isEqualTo(clearanceResponse);


        }

        @Test
        void testInvoiceClearance303() {

            String clearanceResponse = """
                    {
                      "message": "Clearance is deactiviated. Please use the /invoices/reporting/single endpoint instead."
                    }""";
            mockWebServer.enqueue(new MockResponse()
                    .setStatus("HTTP/1.1 303 See Other")
                    .setHeader("Content-Type", "application/json")
                    .setBody(clearanceResponse));

            ZatcaClientApiException zatcaClientApiException = catchThrowableOfType(() ->
                            zatcaService.clearedInvoice("certificate", "secret", invoiceRequest),
                    ZatcaClientApiException.class);

            assertThat(zatcaClientApiException.getStatusCode()).isEqualTo(HttpStatus.SEE_OTHER);
            assertThat(zatcaClientApiException.getStatusText()).isEqualTo("See Other");
            assertThat(zatcaClientApiException.getResponseBody()).isEqualTo(clearanceResponse);
        }
    }
}
