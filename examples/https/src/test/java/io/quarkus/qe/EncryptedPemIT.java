package io.quarkus.qe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class EncryptedPemIT {

    private static final String CLIENT_CN_1 = "my-client-1";

    @QuarkusApplication(ssl = true, certificates = @Certificate(format = Certificate.Format.ENCRYPTED_PEM, password = "PASSWORD_2025", configureKeystore = true, configureHttpServer = true, clientCertificates = {
            @Certificate.ClientCertificate(cnAttribute = "my-client-1")
    }))
    static final RestService app = new RestService();

    @Test
    public void testEncryptedPemEndpoint() {
        var response = app.mutinyHttps(CLIENT_CN_1).get("/greeting").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Hello World!", response.bodyAsString());
    }
}
