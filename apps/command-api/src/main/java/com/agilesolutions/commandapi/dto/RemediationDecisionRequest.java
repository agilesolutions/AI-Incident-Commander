package com.agilesolutions.commandapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RemediationDecisionRequest(
        @NotBlank @Size(max = 200) String approvedBy,
        @NotBlank @Size(max = 1000) String reason,
        @NotBlank @Size(max = 200) String approvalReference) {
}
