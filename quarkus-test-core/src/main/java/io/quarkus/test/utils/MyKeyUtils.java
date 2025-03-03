package io.quarkus.test.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class MyKeyUtils {

    // Private constructor to prevent instantiation.
    private MyKeyUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    /**
     * Loads an unencrypted private key from a PEM file.
     *
     * @param keyLocation the file path to the PEM file containing the private key.
     * @return the PrivateKey instance generated from the PEM file.
     * @throws Exception if an error occurs while reading or parsing the file.
     */
    public static PrivateKey loadPrivateKey(String keyLocation) throws Exception {
        // Read the entire file content into a String
        String pem = Files.readString(Paths.get(keyLocation), StandardCharsets.UTF_8);

        // Remove the PEM header and footer lines, e.g. "-----BEGIN PRIVATE KEY-----" and "-----END PRIVATE KEY-----"
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)-----", "");
        // Remove any whitespace or newline characters
        pem = pem.replaceAll("\\s", "");

        // Decode the Base64 encoded string to obtain the DER encoded key bytes
        byte[] keyBytes = Base64.getDecoder().decode(pem);

        // Create a PKCS8EncodedKeySpec with the decoded key bytes
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        // Instantiate a KeyFactory for RSA (adjust algorithm if needed)
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // Generate and return the PrivateKey
        return keyFactory.generatePrivate(keySpec);
    }
}
