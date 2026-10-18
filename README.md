# j-commons

Bibliotecas Java organizadas em seis módulos Gradle. O código de trabalho é
compilado para Java 25 e exige JDK 25 para execução.

## Sumário

- [1 Módulos e versões](#1-módulos-e-versões)
- [2 Build e validação](#2-build-e-validação)
- [3 Consumo](#3-consumo)
- [4 Metadados e publicação](#4-metadados-e-publicação)
- [5 Atualização do Java](#5-atualização-do-java)

## 1 Módulos e versões

[Voltar ao Sumário](#sumário)

| Módulo | Responsabilidade | Versão de trabalho |
| --- | --- | --- |
| [commons-message](commons-message/README.md) | Formatação de mensagens | `1.0.0-SNAPSHOT` |
| [commons-object-api](commons-object-api/README.md) | Contratos de conversão de objetos | `1.0.0-SNAPSHOT` |
| [commons-object-impl](commons-object-impl/README.md) | Implementações de conversores | `1.0.0-SNAPSHOT` |
| [commons-reflection](commons-reflection/README.md) | Utilitários de reflexão | `1.0.0-SNAPSHOT` |
| [commons-spi](commons-spi/README.md) | Carregamento de serviços SPI | `2.0.0-SNAPSHOT` |
| [commons-thread](commons-thread/README.md) | Construção e execução de tarefas | `3.0.0-SNAPSHOT` |

As versões `-SNAPSHOT` acima representam código em desenvolvimento e não foram
publicadas. Os exemplos de dependência dos módulos usam essas coordenadas para
consumo local ou após uma publicação futura.

## 2 Build e validação

[Voltar ao Sumário](#sumário)

Selecione JDK 25 em `JAVA_HOME` para executar o Gradle Wrapper 9.8.1. O
toolchain compila fontes, testes e Javadoc para Java 25; os JARs produzidos têm
bytecode de versão 69. A partir da raiz do repositório:

```bash
./gradlew clean build jacocoTestReport
./gradlew generatePomFileForMavenJavaPublication generateMetadataFileForMavenJavaPublication
```

`clean build` executa os testes dos módulos e os testes JUnit de integração
da raiz. Estes montam um repositório temporário com os JARs, POMs e metadados
gerados, executam um consumidor binário por cada formato de metadados e
analisam os seis JARs de produção com `jdeps` e `jdeprscan`. O build também
gera JARs de classes, fontes e Javadoc. `jacocoTestReport` gera cobertura nos
módulos com testes. Nenhuma dessas tarefas publica artefatos. O CI executa as
mesmas validações com Temurin 25. Os
[relatórios históricos](docs/README.md) são preservados separadamente dos
resultados deste build.

## 3 Consumo

[Voltar ao Sumário](#sumário)

Use `implementation` em aplicações. Em outra biblioteca, use `api` somente
quando tipos de j-commons aparecerem em suas assinaturas públicas. Por exemplo,
após disponibilizar a versão de trabalho em um repositório:

```gradle
dependencies {
    implementation 'br.com.armange:commons-object-impl:1.0.0-SNAPSHOT'
}
```

`commons-object-impl` expõe `commons-object-api` como dependência transitiva
de compilação. Para testar as versões de trabalho diretamente deste checkout,
execute `./gradlew clean build`: a suíte monta um consumidor isolado com os
artefatos locais antes da publicação dessas coordenadas.

## 4 Metadados e publicação

[Voltar ao Sumário](#sumário)

Os POMs e Gradle Module Metadata são gerados em
`commons-*/build/publications/mavenJava/`. As tarefas acima geram arquivos
locais e não publicam artefatos. O Gradle Module Metadata informa o requisito
de JVM 25 nas variantes de consumo; consumidores Maven devem observar o
requisito descrito aqui.

A configuração de publicação usa Central Portal e GitHub Packages. Para uma
publicação futura, `CENTRAL_PORTAL_USERNAME` e `CENTRAL_PORTAL_PASSWORD`
identificam o Portal; `SIGNING_KEY` e `SIGNING_PASSWORD` fornecem a
assinatura; `GHP_USER` e `GHP_TOKEN` autenticam no GitHub Packages.
`J_COMMONS_GHP_URL` pode substituir a URL padrão do repositório GitHub.
Somente uma versão estável, validada e marcada com tag deve ser publicada. A
migração atual não executa tarefas de publicação.

Após configurar as credenciais e preparar o release estável, as tarefas
configuradas são `publishAggregationToCentralPortal` para enviar a agregação
ao Central Portal e `:commons-thread:publishMavenJavaPublicationToGitHubPackagesRepository`
para enviar o módulo indicado ao GitHub Packages; os outros módulos possuem a
mesma tarefa com seu próprio caminho Gradle. O Central Portal está configurado
para liberação manual (`USER_MANAGED`). Essas tarefas não fazem parte da
validação local desta migração.

## 5 Atualização do Java

[Voltar ao Sumário](#sumário)

`commons-spi:1.0.0` e `commons-thread:2.0.0` são versões estáveis anteriores,
compiladas para Java 8 e Java 11, respectivamente. Suas próximas versões
estáveis terão números major 2 e 3 porque o requisito mínimo passa a Java 25.
Os outros quatro módulos seguem em `1.0.0-SNAPSHOT`. Para atualizar um
consumidor, instale JDK 25, troque as coordenadas quando a nova versão for
publicada e execute os testes do consumidor. A versão de trabalho não substitui
uma versão estável já publicada.
