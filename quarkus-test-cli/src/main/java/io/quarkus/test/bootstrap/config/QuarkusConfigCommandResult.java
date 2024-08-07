package io.quarkus.test.bootstrap.config;

import io.smallrye.common.os.OS;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuarkusConfigCommandResult {

    final String applicationPropertiesAsString;
    final String output;

    QuarkusConfigCommandResult(String output, String applicationPropertiesAsString) {
        this.output = output;
        this.applicationPropertiesAsString = applicationPropertiesAsString;
    }

    public QuarkusConfigCommandResult assertCommandOutputNotContains(String expected) {
        assertFalse(output.contains(expected.trim()), "Expected output '" + output + "' contains '" + expected + "'");
        return this;
    }

    public QuarkusConfigCommandResult assertCommandOutputContains(String expected) {
        System.out.println("EXPECTED OoUTPUT :  " + expected);
        System.out.println("REAL command OUTPUT " + output);

        if (OS.WINDOWS.isCurrent()) {
            String windowsEscapedExpected = expected.replaceAll("\"", "");
            windowsEscapedExpected = windowsEscapedExpected.replaceAll("\n", " ");
            String outputWithoutQuotes = output.replaceAll("\"", "");
            outputWithoutQuotes = windowsEscapedExpected.replaceAll("\n", " ");
            assertTrue(outputWithoutQuotes.contains(windowsEscapedExpected.trim()),
                    "Expected output '" + outputWithoutQuotes + "' does not contain '" + windowsEscapedExpected + "'");

            assertTrue(output.contains(expected),
                    "Expected output '" + output + "' does not contain '" + expected + "'");

        } else {
            assertTrue(output.contains(expected.trim()), "Expected output '" + output + "' does not contain '" + expected + "'");
        }
        return this;
    }

    public QuarkusConfigCommandResult assertApplicationPropertiesContains(String str) {
        assertTrue(applicationPropertiesAsString.contains(str),
                "Expected value '" + str + "' is missing in application.properties: " + applicationPropertiesAsString);
        return this;
    }

    public QuarkusConfigCommandResult assertApplicationPropertiesContains(String key, String value) {
        return assertApplicationPropertiesContains(key + "=" + value);
    }

    public QuarkusConfigCommandResult assertApplicationPropertiesDoesNotContain(String key, String value) {
        return assertApplicationPropertiesDoesNotContain(key + "=" + value);
    }

    public QuarkusConfigCommandResult assertApplicationPropertiesDoesNotContain(String str) {
        assertFalse(applicationPropertiesAsString.contains(str),
                "Expected value '" + str + "' is present in application.properties: " + applicationPropertiesAsString);
        return this;
    }
}
