package com.ahmedmq.zatca.spring.service;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

class ZatcaClientErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return !response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        throw new ZatcaClientApiException(response.getStatusCode(),
                response.getStatusText(),
                getResponseBodyAsString(response));
    }

    private String getResponseBodyAsString(ClientHttpResponse response) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody()))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }
}
