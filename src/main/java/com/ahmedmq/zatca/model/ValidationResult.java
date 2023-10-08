package com.ahmedmq.zatca.model;

import java.util.List;

public record ValidationResult(List<Info> infoMessages,
        List<Warning> warningMessages,
        List<java.lang.Error> errorMessages,
        String status) {
}
