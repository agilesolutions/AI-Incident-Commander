package com.agilesolutions.remediation.application;

public record ExecutionResult(boolean accepted, String executionReference, String message) {
}
