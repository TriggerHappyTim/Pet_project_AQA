package com.bft.LK_Insurence.SZV_M;

import com.bft.service.graphql.KeycloakTokenProvider;
import com.bft.service.sign.DaPortalClient;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * ПРОБА: получить Bearer-токен Keycloak (admin-cli / ROPC) и послать мутацию
 * processSignatureCommon (GetSignatureDoneRouter) напрямую на port-forward localhost:20266.
 * Если ошибка меняется с "UNAUTHENTICATED: не передан токен" на иную — значит Bearer принимается
 * (mesh берёт любой валидный токен realm geop), и это и есть чистый API-путь без расшифровки cookie.
 */
public class MeshBearerProbeTest {

    @Test
    public void probeBearerMutation() {
        String tok = KeycloakTokenProvider.getAccessToken();
        LoggerFactory.getLogger(MeshBearerProbeTest.class).info("TOKEN claims: {}",
                KeycloakTokenProvider.decodeClaimsPublic(tok));
        String query = "mutation ProcessSignatureCommon {"
                + "  ruGovPfrEcpEvsDaIpuService_CommonReportSignatureService_GetSignatureDoneRouter("
                + "    input: {"
                + "      itemId: {value: \"62640\"}"
                + "      signedXmlFileRef: {value: \"dummy\"}"
                + "      mimeTypes: [XML]"
                + "      requestId: {value: \"dummy\"}"
                + "    }"
                + "  ) { result {value} statusMessage {value} }"
                + "}";
        DaPortalClient da = new DaPortalClient();
        String r = da.probeMeshViaJavaBearer("http://localhost:20266/graphql", tok, query);
        LoggerFactory.getLogger(MeshBearerProbeTest.class).info("BEARER MUTATION PROBE: {}", r);
    }
}
