package com.bft.LK_Insurence.SZV_M;

import com.bft.service.graphql.GraphQLClient;
import org.junit.jupiter.api.Test;

/**
 * ПРОБА: интроспекция схемы mesh (reader-user Bearer) — найти мутации создания отчёта,
 * чтобы создать отчёт от имени reader-user (тогда reader-user владелец, и processSignatureCommon пройдёт).
 */
public class MeshSchemaProbe extends com.bft.test.base.UITestBase {

    @Test
    public void listMutations() {
        GraphQLClient client = new GraphQLClient();
        String q = "{ __schema { mutationType { fields { name args { name } } } } }";
        String resp = client.executeMutation(q);
        System.out.println("SCHEMA_RESP >>>");
        System.out.println(resp);
        System.out.println("<<< SCHEMA_RESP");
    }
}
