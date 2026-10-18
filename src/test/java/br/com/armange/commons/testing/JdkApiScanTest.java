package br.com.armange.commons.testing;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdkApiScanTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    public void productionJarsDoNotUseInternalOrDeprecatedJdkApis() throws Exception {
        final Path projectRoot = Path.of(System.getProperty("jCommons.projectRoot"));
        final PublishedArtifactsFixture artifacts = PublishedArtifactsFixture.stage(
                projectRoot, temporaryDirectory.resolve("repository"));
        final Path consumer = ConsumerProjectFixture.create(
                temporaryDirectory.resolve("consumer"), artifacts);

        GradleRunner.create()
                .withProjectDir(consumer.toFile())
                .withArguments("writeApiScanClasspath", "-PmetadataSource=pom", "--stacktrace")
                .build();

        final String classpath = Files.readString(consumer.resolve("build/api-scan-classpath.txt"));
        assertTrue(!classpath.isBlank(), "O classpath de análise deve conter as dependências resolvidas");

        for (final String moduleName : PublishedArtifactsFixture.MODULES) {
            final Path jar = artifacts.productionJar(moduleName);
            assertTrue(Files.isRegularFile(jar), "JAR de produção ausente: " + jar);
            assertCleanScan(List.of(tool("jdeps"), "--jdk-internals", "--multi-release", "25",
                    jar.toString()), false);
            assertCleanScan(List.of(tool("jdeprscan"), "--release", "25", "--class-path",
                    classpath, jar.toString()), true);
        }
    }

    private static String tool(final String name) {
        return Path.of(System.getProperty("java.home"), "bin", name).toString();
    }

    private static void assertCleanScan(final List<String> command, final boolean deprscan)
            throws Exception {
        final Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final int exitCode = process.waitFor();
        assertEquals(0, exitCode, String.join(" ", command) + "\n" + output);

        final String findings = deprscan
                ? output.replaceFirst("^Jar file [^\\r\\n]+:\\R?", "")
                : output;
        assertTrue(findings.isBlank(), String.join(" ", command) + "\n" + findings);
    }
}
