package br.com.armange.commons.testing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConsumerProjectFixture {
    private ConsumerProjectFixture() {
    }

    public static Path create(final Path destination, final PublishedArtifactsFixture artifacts)
            throws IOException {
        copyResource("consumer/build.gradle", destination.resolve("build.gradle"));
        copyResource("consumer/settings.gradle", destination.resolve("settings.gradle"));
        copyResource("consumer/ConsumerSmoke.java", destination.resolve(
                "src/main/java/br/com/armange/commons/example/ConsumerSmoke.java"));
        Files.writeString(destination.resolve("gradle.properties"),
                "fixtureRepository=" + artifacts.repositoryRoot().toUri() + "\n"
                        + "objectImplVersion=" + artifacts.version("commons-object-impl") + "\n"
                        + "threadVersion=" + artifacts.version("commons-thread") + "\n");

        return destination;
    }

    private static void copyResource(final String resourceName, final Path destination)
            throws IOException {
        Files.createDirectories(destination.getParent());
        try (InputStream source = ConsumerProjectFixture.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (source == null) {
                throw new IOException("Recurso de consumidor ausente: " + resourceName);
            }
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
