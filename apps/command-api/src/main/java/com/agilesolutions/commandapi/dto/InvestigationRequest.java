package com.agilesolutions.commandapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InvestigationRequest(
        @NotBlank @Size(max = 500) String objective,
        @Size(max = 20) List<@NotBlank String> evidenceEventIds) {
}
