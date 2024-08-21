package io.quarkus.test.bootstrap.config;

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
        System.out.println("this is configCommand from QuarkusEncryptConfigCommandResult ..> " + this.configCommand.toString());
    }

    public String getGeneratedEncryptionKey() {
        if (output.contains(WITH_GENERATED_KEY)) {
            String outputKey = output.transform(o -> o.substring(o.lastIndexOf(" "))).trim();
            System.out.println("This is getGeneratedEncryptionKey ----** --> " + outputKey);
            return outputKey;
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
        System.out.println("This is encryptedSecret ---------> " + encryptedSecret);
        return encryptedSecret;
    }

    public QuarkusEncryptConfigCommandResult secretConsumer(Consumer<String> secretConsumer) {
        secretConsumer.accept(getEncryptedSecret());
        return this;
    }

    public QuarkusEncryptConfigCommandResult storeSecretAsSecretExpression(String propertyName) {
        configCommand.addToApplicationPropertiesFile(propertyName, withDefaultSecretKeyHandler(getEncryptedSecret()));
        return this;
    }

    public QuarkusEncryptConfigCommandResult storeSecretAsRawValue(String propertyName) {
        configCommand.addToApplicationPropertiesFile(propertyName, getEncryptedSecret());
        return this;
    }

    public QuarkusEncryptConfigCommandResult storeGeneratedKeyAsProperty() {
        var generatedEncryptionKey = getGeneratedEncryptionKey();
        Objects.requireNonNull(generatedEncryptionKey);
        System.out.println("This is generatedEncryptionKey on storeGeneratedKeyAsProperty --> " + generatedEncryptionKey);
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
