package br.com.armange.commons.testing;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublishedConsumerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    public void consumesGradleModuleMetadata() throws IOException {
        verifyConsumption("module");
    }

    @Test
    public void consumesMavenPom() throws IOException {
        verifyConsumption("pom");
    }

    private void verifyConsumption(final String metadataSource) throws IOException {
        final Path projectRoot = Path.of(System.getProperty("jCommons.projectRoot"));
        final PublishedArtifactsFixture artifacts = PublishedArtifactsFixture.stage(
                projectRoot, temporaryDirectory.resolve("repository"));
        final Path consumer = ConsumerProjectFixture.create(
                temporaryDirectory.resolve("consumer"), artifacts);
        final BuildResult result = GradleRunner.create()
                .withProjectDir(consumer.toFile())
                .withArguments("run", "-PmetadataSource=" + metadataSource, "--stacktrace")
                .build();

        assertEquals(TaskOutcome.SUCCESS, result.task(":run").getOutcome());
        assertTrue(result.getOutput().contains("Consumo de commons-object-impl no JDK 25: OK"));
    }
}
