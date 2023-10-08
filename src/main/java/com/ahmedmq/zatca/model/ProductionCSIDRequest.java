package com.ahmedmq.zatca.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductionCSIDRequest(
        @JsonProperty("compliance_request_id") String complianceRequestId) {
}
