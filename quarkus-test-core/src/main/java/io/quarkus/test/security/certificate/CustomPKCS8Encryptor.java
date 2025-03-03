package io.quarkus.test.security.certificate;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CustomPKCS8Encryptor {

    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH = 128;
    private static final int SALT_LENGTH = 16;
    private static final String TRANSFORMATION = "PBEWithSHA1AndDESede";
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int MIME_ENCODER_LINE_LENGTH = 64;

    private CustomPKCS8Encryptor() {
        throw new UnsupportedOperationException("Utility class should not be instantiated.");
    }

    public static String encryptPrivateKey(PrivateKey privateKey, String password)
            throws GeneralSecurityException, IOException {

        if (privateKey == null || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("privateKey or password should not be null");
        }

        byte[] salt = generateSalt();
        SecretKey secretKey = deriveKey(password, salt);
        Cipher cipher = initCipher(secretKey, salt);
        byte[] encryptedKeyBytes = cipher.doFinal(privateKey.getEncoded());
        AlgorithmParameters algParams = cipher.getParameters();
        if (algParams == null) {
            algParams = AlgorithmParameters.getInstance(TRANSFORMATION);
            algParams.init(new PBEParameterSpec(salt, ITERATION_COUNT));
        }

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(algParams, encryptedKeyBytes);
        byte[] encoded = encryptedPrivateKeyInfo.getEncoded();

        return buildPEM(encoded);
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws GeneralSecurityException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmpKey = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(tmpKey.getEncoded(), "AES");
    }

    private static Cipher initCipher(SecretKey secretKey, byte[] salt) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParamSpec);
        return cipher;
    }

    private static String buildPEM(byte[] encoded) {
        String base64Encoded = Base64.getMimeEncoder(MIME_ENCODER_LINE_LENGTH, "\n".getBytes())
                .encodeToString(encoded);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ENCRYPTED PRIVATE KEY-----\n");
        pem.append(base64Encoded);
        pem.append("\n-----END ENCRYPTED PRIVATE KEY-----");
        return pem.toString();
    }
}
