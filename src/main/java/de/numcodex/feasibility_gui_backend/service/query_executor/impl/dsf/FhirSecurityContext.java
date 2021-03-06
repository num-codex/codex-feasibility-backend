package de.numcodex.feasibility_gui_backend.service.query_executor.impl.dsf;

import java.security.KeyStore;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Holds information about how to securely communicate with a FHIR server.
 */
@Data
@AllArgsConstructor
class FhirSecurityContext {
    KeyStore keyStore;
    KeyStore trustStore;
    private char[] keyStorePassword;
}
