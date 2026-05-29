package br.com.orbittapi.soap.endpoint;

/**
 * Identidade tecnica usada pelo gateway SOAP ao chamar o satellite-service
 * via REST. O satellite apenas exige a presenca do header X-User-Id; aqui
 * usamos um UUID fixo conhecido pelo time tecnico para representar consultas
 * iniciadas pela camada SOAP. Auditoria em SatelliteQuery preserva esse id.
 */
final class SoapSystemIdentity {

    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000501";

    private SoapSystemIdentity() {}

    static String systemUserId() {
        return SYSTEM_USER_ID;
    }
}
