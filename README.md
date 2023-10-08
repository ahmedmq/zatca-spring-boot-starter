# Zatca Spring Boot Starter

This repo is an unofficial Spring Boot starter for Saudi Arabia's Zatca (Zakat and Tax Authority) API.


## Usage

### Add Zatca Properties to `application.yml`

```text
zatca:
  baseUrl: https://zatca.sa
  apiVersion: V2
```

### Use `ZatcaService`

```java

@Service
public class MyService {

    private final ZatcaService zatcaService;

    public MyService(ZatcaService zatcaService) {
        this.zatcaService = zatcaService;
    }

    public void myMethod() {
        var csrRequest = new CsrRequest("csr");
        zatcaService.complianceCSID(csrRequest, "otp");
    }
}

```

## Zatca API Interface

```java
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
```

## References

- [Zatca Sandbox API Developer Portal ](https://sandbox.zatca.gov.sa/IntegrationSandbox/) (Requires Login)
- [E-invoicing Guideline] (https://zatca.gov.sa/en/E-Invoicing/Introduction/Guidelines/Documents/E-Invoicing_Detailed__Guideline.pdf)