package br.com.armange.commons.testing;

import groovy.json.JsonSlurper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PublishedArtifactsFixture {
    public static final List<String> MODULES = List.of(
            "commons-message", "commons-object-api", "commons-object-impl",
            "commons-reflection", "commons-spi", "commons-thread");

    private final Path projectRoot;
    private final Path repositoryRoot;
    private final Map<String, String> versions;

    private PublishedArtifactsFixture(final Path projectRoot, final Path repositoryRoot,
                                      final Map<String, String> versions) {
        this.projectRoot = projectRoot;
        this.repositoryRoot = repositoryRoot;
        this.versions = Map.copyOf(versions);
    }

    public static PublishedArtifactsFixture stage(final Path projectRoot, final Path repositoryRoot)
            throws IOException {
        final Map<String, String> versions = new HashMap<>();

        for (final String moduleName : MODULES) {
            versions.put(moduleName, stageModule(projectRoot, repositoryRoot, moduleName));
        }

        return new PublishedArtifactsFixture(projectRoot, repositoryRoot, versions);
    }

    public Path repositoryRoot() {
        return repositoryRoot;
    }

    public String version(final String moduleName) {
        return versions.get(moduleName);
    }

    public Path productionJar(final String moduleName) {
        return projectRoot
                .resolve(moduleName)
                .resolve("build/libs")
                .resolve(moduleName + "-" + version(moduleName) + ".jar");
    }

    private static String stageModule(final Path projectRoot, final Path repositoryRoot,
                                      final String moduleName) throws IOException {
        final Path moduleRoot = projectRoot.resolve(moduleName);
        final Path publicationRoot = moduleRoot.resolve("build/publications/mavenJava");
        final Path pom = publicationRoot.resolve("pom-default.xml");
        final Path metadataFile = publicationRoot.resolve("module.json");
        final Map<?, ?> metadata = asMap(new JsonSlurper().parse(metadataFile.toFile()));
        final Map<?, ?> component = asMap(metadata.get("component"));
        final String group = String.valueOf(component.get("group"));
        final String module = String.valueOf(component.get("module"));
        final String version = String.valueOf(component.get("version"));

        if (!"br.com.armange".equals(group) || !moduleName.equals(module)) {
            throw new IllegalStateException("Coordenadas de metadados incorretas: " + moduleName);
        }
        validatePom(pom, group, module, version);

        final Path relativePath = Path.of(group.replace('.', '/'), module, version);
        final Path moduleRepository = repositoryRoot.resolve("module").resolve(relativePath);
        final Path pomRepository = repositoryRoot.resolve("pom").resolve(relativePath);
        Files.createDirectories(moduleRepository);
        Files.createDirectories(pomRepository);
        copy(pom, moduleRepository.resolve(module + "-" + version + ".pom"));
        copy(pom, pomRepository.resolve(module + "-" + version + ".pom"));
        copy(metadataFile, moduleRepository.resolve(module + "-" + version + ".module"));

        final Set<String> artifactNames = new HashSet<>();
        for (final Object variantValue : asList(metadata.get("variants"))) {
            final Map<?, ?> variant = asMap(variantValue);
            if (String.valueOf(variant.get("name")).contains("testFixtures")) {
                throw new IllegalStateException("Fixture publicada: " + moduleName);
            }
            if (!variant.containsKey("files")) {
                continue;
            }
            for (final Object fileValue : asList(variant.get("files"))) {
                final Map<?, ?> file = asMap(fileValue);
                final String artifactName = String.valueOf(file.get("name"));
                final Path artifact = moduleRoot.resolve("build/libs").resolve(artifactName);
                validateArtifact(artifact, file);
                if (artifactNames.add(artifactName)) {
                    copy(artifact, moduleRepository.resolve(artifactName));
                }
            }
        }

        final String primaryJar = module + "-" + version + ".jar";
        if (!artifactNames.contains(primaryJar)) {
            throw new IllegalStateException("JAR principal ausente dos metadados: " + primaryJar);
        }
        copy(moduleRoot.resolve("build/libs").resolve(primaryJar), pomRepository.resolve(primaryJar));

        return version;
    }

    private static void validatePom(final Path pom, final String group, final String module,
                                    final String version) throws IOException {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final Document document = factory.newDocumentBuilder().parse(pom.toFile());
            if (!group.equals(document.getElementsByTagName("groupId").item(0).getTextContent())
                    || !module.equals(document.getElementsByTagName("artifactId").item(0).getTextContent())
                    || !version.equals(document.getElementsByTagName("version").item(0).getTextContent())) {
                throw new IllegalStateException("POM incompatível com " + module);
            }
        } catch (final ParserConfigurationException | SAXException e) {
            throw new IOException("Não foi possível ler o POM de " + module, e);
        }
    }

    private static void validateArtifact(final Path artifact, final Map<?, ?> file)
            throws IOException {
        final byte[] contents = Files.readAllBytes(artifact);
        if (contents.length != ((Number) file.get("size")).longValue()) {
            throw new IllegalStateException("Tamanho incompatível: " + artifact);
        }
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(contents);
            if (!HexFormat.of().formatHex(digest).equals(file.get("sha256"))) {
                throw new IllegalStateException("SHA-256 incompatível: " + artifact);
            }
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static void copy(final Path source, final Path destination) throws IOException {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static Map<?, ?> asMap(final Object value) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalStateException("Objeto JSON esperado: " + value);
        }

        return (Map<?, ?>) value;
    }

    private static List<?> asList(final Object value) {
        if (!(value instanceof List<?>)) {
            throw new IllegalStateException("Lista JSON esperada: " + value);
        }

        return (List<?>) value;
    }
}
