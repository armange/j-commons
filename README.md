# j-commons

Bibliotecas Java organizadas em seis módulos Gradle.

## Módulos

- [commons-thread](commons-thread/README.md): construção e execução de tarefas em threads.
- [commons-spi](commons-spi/README.md): carregamento de serviços Java SPI.
- `commons-reflection`: utilitários de reflexão.
- `commons-object-api`: contratos de conversão de objetos.
- `commons-object-impl`: implementações dos contratos de objetos.
- `commons-message`: mensagens e internacionalização.

## Build

O Wrapper usa Gradle 9.8.1. Para executá-lo, selecione JDK 17 ou superior;
o CI usa JDK 21. O Gradle obtém automaticamente um toolchain JDK 11 para
compilar, testar e gerar Javadoc. Os artefatos ainda têm Java 11 como requisito
de execução.

Na raiz do repositório, execute:

```bash
./gradlew clean build
./gradlew jacocoTestReport
```

O primeiro comando executa os testes disponíveis nos seis módulos ativos e gera JARs de
classes, fontes e Javadoc. O segundo gera relatórios de cobertura para os
módulos com testes.

## Metadados de publicação

Os POMs e Gradle Module Metadata podem ser conferidos sem publicar artefatos:

```bash
./gradlew generatePomFileForMavenJavaPublication generateMetadataFileForMavenJavaPublication
```

Os arquivos ficam em `commons-*/build/publications/mavenJava/`. A configuração
de publicação usa o Central Portal para Maven Central e GitHub Packages. As
credenciais são lidas somente do ambiente: `CENTRAL_PORTAL_USERNAME` e
`CENTRAL_PORTAL_PASSWORD` para o Portal, `SIGNING_KEY` e `SIGNING_PASSWORD`
para assinatura, e `GHP_USER` e `GHP_TOKEN` para GitHub Packages. A variável
opcional `J_COMMONS_GHP_URL` substitui a URL padrão do repositório GitHub.
