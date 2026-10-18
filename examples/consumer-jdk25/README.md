# Consumidor JDK 25

Este projeto Gradle isolado valida o consumo de `commons-object-impl` como
dependência binária de um aplicativo Java 25. Os JARs, POMs e Gradle Module
Metadata gerados pelos seis módulos são copiados para o repositório temporário
`build/consumer-jdk25/repository/` da raiz, sem executar tarefas de publicação.
O consumidor resolve as coordenadas a partir desses arquivos.

[Voltar ao README principal](../../README.md)

Na raiz de `j-commons`, com JDK 25 selecionado em `JAVA_HOME`, execute:

```bash
./gradlew clean build generatePomFileForMavenJavaPublication generateMetadataFileForMavenJavaPublication
python3 examples/consumer-jdk25/stage_artifacts.py
./gradlew -p examples/consumer-jdk25 clean run -PmetadataSource=module
./gradlew -p examples/consumer-jdk25 clean run writeApiScanClasspath -PmetadataSource=pom
bash examples/consumer-jdk25/verify_jdk_apis.sh
```

O consumidor declara somente `commons-object-impl` e usa um tipo de
`commons-object-api` em seu código. Assim, a compilação também verifica se a
dependência transitiva está exposta corretamente no POM e nos metadados Gradle.
`writeApiScanClasspath` resolve os seis módulos e as dependências transitivas
para o `jdeprscan`. O script de análise verifica apenas os JARs de produção do
projeto e falha se encontrar APIs internas, APIs obsoletas ou classes ausentes.
