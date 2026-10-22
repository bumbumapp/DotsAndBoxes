package com.dotsandboxes;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Regression guard: onServiceDisconnected used startService(new Intent(ACTION)) - an implicit
 * service intent, which throws IllegalArgumentException on API 21+ and crashed the app.
 */
public class ServiceIntentRegressionTest {
    @Test
    public void noImplicitStartServiceCalls() throws IOException {
        Path src = Paths.get("src/main/java");
        try (Stream<Path> files = Files.walk(src)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    String code = new String(Files.readAllBytes(p));
                    assertFalse(p + " starts a service with an implicit intent",
                            code.matches("(?s).*startService\\(new Intent\\(MusicPlayerService\\.ACTION_[A-Z_]+\\)\\).*"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
