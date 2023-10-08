package com.ahmedmq.zatca.model;

public record CSIDResponse(String requestID,
                           String dispositionMessage,
                           String binarySecurityToken,
                           String secret) {
}
