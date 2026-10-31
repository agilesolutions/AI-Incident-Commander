package com.agilesolutions.remediation.application;

public interface RemediationVerifier {
    VerificationResult verify(ExecutionCommand command, ExecutionResult result);
}
