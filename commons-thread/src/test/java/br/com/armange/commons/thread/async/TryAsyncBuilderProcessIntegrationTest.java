/*
 * Copyright [2026] [Diego Armange Costa]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.armange.commons.thread.async;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TryAsyncBuilderProcessIntegrationTest {
    @Test
    void successfulTaskAllowsChildJvmToExitAfterFinalization() throws Exception {
        final String output = runChild("success");

        assertTrue(output.contains("TASK_SUCCESS"), output);
        assertTrue(output.contains("FINALIZED"), output);
        assertTrue(output.contains("READY_TO_EXIT"), output);
    }

    @Test
    void failedTaskAllowsChildJvmToExitAfterCatchAndFinalization() throws Exception {
        final String output = runChild("failure");

        assertTrue(output.contains("TASK_FAILURE"), output);
        assertTrue(output.contains("CAUGHT_ASYNC_RUNTIME_EXCEPTION"), output);
        assertTrue(output.contains("FINALIZED"), output);
        assertTrue(output.contains("READY_TO_EXIT"), output);
    }

    private static String runChild(final String scenario) throws Exception {
        final Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        final Process process = new ProcessBuilder(java.toString(), "-cp",
                testRuntimeClasspath(), ChildMain.class.getName(), scenario)
                .redirectErrorStream(true)
                .start();
        final boolean exitedNaturally;

        try {
            exitedNaturally = process.waitFor(12, TimeUnit.SECONDS);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(3, TimeUnit.SECONDS),
                        "A JVM filha não terminou após destroyForcibly");
            }
        }

        final byte[] bytes = process
                .getInputStream()
                .readAllBytes();
        final String output = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(exitedNaturally, "A JVM filha não saiu espontaneamente:\n" + output);
        assertEquals(0, process.exitValue(), output);

        return output;
    }

    private static String testRuntimeClasspath() throws Exception {
        final Set<Path> entries = new LinkedHashSet<>();

        for (ClassLoader loader = ChildMain.class.getClassLoader();
             loader != null; loader = loader.getParent()) {
            if (loader instanceof URLClassLoader urlLoader) {
                for (final URL url : urlLoader.getURLs()) {
                    if ("file".equals(url.getProtocol())) {
                        entries.add(Path.of(url.toURI()));
                    }
                }
            }
        }

        final Path testClasses = Path.of(ChildMain.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        final Path mainClasses = Path.of(TryAsyncBuilder.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());

        entries.add(testClasses);
        entries.add(mainClasses);
        addClassLocation(entries, "org.slf4j.LoggerFactory");
        addClassLocation(entries, "org.slf4j.simple.SimpleServiceProvider");
        addClassLocation(entries, "org.apache.logging.log4j.LogManager");
        addClassLocation(entries, "org.apache.logging.log4j.core.LoggerContext");

        return entries.stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private static void addClassLocation(final Set<Path> entries,
                                         final String className) throws Exception {
        final Class<?> type = Class.forName(className, false,
                ChildMain.class.getClassLoader());
        final Path location = Path.of(type
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());

        entries.add(location);
    }

    public static final class ChildMain {
        private ChildMain() {
        }

        public static void main(final String[] args) throws Exception {
            final CountDownLatch finalized = new CountDownLatch(1);
            final RunnableWithException task;

            if ("failure".equals(args[0])) {
                task = () -> {
                    System.out.println("TASK_FAILURE");
                    throw new IOException("expected failure");
                };
            } else if ("success".equals(args[0])) {
                task = () -> System.out.println("TASK_SUCCESS");
            } else {
                throw new IllegalArgumentException("Unknown scenario: " + args[0]);
            }

            TryAsyncBuilder
                    .tryAsync(task)
                    .addCatcher(AsyncRuntimeException.class, failure ->
                            System.out.println("CAUGHT_ASYNC_RUNTIME_EXCEPTION"))
                    .finallyAsync(() -> {
                        System.out.println("FINALIZED");
                        finalized.countDown();
                    })
                    .execute();

            if (!finalized.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Finalizer did not run");
            }

            System.out.println("READY_TO_EXIT");
        }
    }
}
