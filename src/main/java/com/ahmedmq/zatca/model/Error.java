package com.ahmedmq.zatca.model;

public record Error(
        String category,
        String code,
        String message
) {
}
