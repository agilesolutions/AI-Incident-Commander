package com.agilesolutions.remediation.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "remediation.verification-mode", havingValue = "manual", matchIfMissing = true)
public class ManualRemediationVerifier implements RemediationVerifier {
    public VerificationResult verify(ExecutionCommand c, ExecutionResult r) {
        return new VerificationResult(false, "Automatische verificatie niet geconfigureerd; menselijke controle vereist.");
    }
}
