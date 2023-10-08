package com.ahmedmq.zatca.spring.service;

import org.springframework.http.HttpStatusCode;

public class ZatcaClientApiException extends RuntimeException{
    private final HttpStatusCode statusCode;
    private final String statusText;
    private final String responseBody;

    public ZatcaClientApiException(
            HttpStatusCode statusCode,
            String statusText,
            String responseBody) {

        super();
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
