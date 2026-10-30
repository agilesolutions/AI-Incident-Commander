package com.agilesolutions.commandapi.api;

public record CommandAcceptedResponse(
        String commandId,
        String status,
        String message) {
}
