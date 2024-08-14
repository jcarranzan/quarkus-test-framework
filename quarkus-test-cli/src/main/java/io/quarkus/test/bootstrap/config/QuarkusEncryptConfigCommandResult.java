package io.quarkus.test.bootstrap.config;

import io.quarkus.test.util.QuarkusCLIUtils;
import io.smallrye.common.os.OS;

import static io.quarkus.test.bootstrap.config.QuarkusEncryptConfigCommandBuilder.AES_GCM_NO_PADDING_HANDLER_ENC_KEY;

import java.util.Objects;
import java.util.function.Consumer;

public class QuarkusEncryptConfigCommandResult extends QuarkusConfigCommandResult {

    private static final String SECRET_ENCRYPTED_TO = "was encrypted to";
    private static final String WITH_GENERATED_KEY = "with the generated encryption key";
    private final QuarkusConfigCommand configCommand;
    private String encryptedSecret = null;

    QuarkusEncryptConfigCommandResult(QuarkusConfigCommandResult delegate, QuarkusConfigCommand configCommand) {
        super(delegate.output, delegate.applicationPropertiesAsString);
        this.configCommand = configCommand;
    }

    public String getGeneratedEncryptionKey() {
        if (output.contains(WITH_GENERATED_KEY)) {
            return output.transform(o -> o.substring(o.lastIndexOf(" "))).trim();
        }
        return null;
    }

    public String getEncryptedSecret() {
        if (encryptedSecret == null) {
            encryptedSecret = output
                    .transform(o -> o.split(SECRET_ENCRYPTED_TO)[1])
                    .transform(remaining -> remaining.split(WITH_GENERATED_KEY)[0])
                    .trim();
        }
        return encryptedSecret;
    }

    public QuarkusEncryptConfigCommandResult secretConsumer(Consumer<String> secretConsumer) {
        secretConsumer.accept(getEncryptedSecret());
        return this;
    }

    public QuarkusEncryptConfigCommandResult storeSecretAsSecretExpression(String propertyName) {
        System.out.println("PROPERTY NAME ==== " + propertyName);
        String secretExpression = withDefaultSecretKeyHandler(getEncryptedSecret());
        System.out.println("SECRET EXPRESSION " + secretExpression);
        if (OS.WINDOWS.isCurrent()) {
            secretExpression = QuarkusCLIUtils.escapeSecretCharsForWindows(secretExpression);
            System.out.println("SECRET EXPRESSION ON WINDOWS SYSTEM " + secretExpression);
        }
        configCommand.addToApplicationPropertiesFile(propertyName, secretExpression);

        return this;
    }

    public QuarkusEncryptConfigCommandResult storeSecretAsRawValue(String propertyName) {
        configCommand.addToApplicationPropertiesFile(propertyName, getEncryptedSecret());
        return this;
    }

    public QuarkusEncryptConfigCommandResult storeGeneratedKeyAsProperty() {
        var generatedEncryptionKey = getGeneratedEncryptionKey();
        Objects.requireNonNull(generatedEncryptionKey);
        configCommand.addToApplicationPropertiesFile(AES_GCM_NO_PADDING_HANDLER_ENC_KEY, generatedEncryptionKey);
        return this;
    }

    public QuarkusEncryptConfigCommandResult generatedKeyConsumer(Consumer<String> encKeyConsumer) {
        encKeyConsumer.accept(getGeneratedEncryptionKey());
        return this;
    }

    public static String withDefaultSecretKeyHandler(String secret) {
        return "${aes-gcm-nopadding::%s}".formatted(secret);
    }
}
