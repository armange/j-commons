# commons-message

Formata mensagens parametrizadas para uso por aplicações e outros módulos.

[Voltar ao README principal](../README.md)

## Contrato

`MessageFormat` define a mensagem e oferece `format(Object...)`.
`CommonMessages` contém mensagens comuns; por exemplo,
`CommonMessages.REQUIRED_PARAMETER.format("nome")`.

## Consumo

Versão de trabalho: `br.com.armange:commons-message:1.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25. Em uma aplicação Gradle:

```gradle
dependencies {
    implementation 'br.com.armange:commons-message:1.0.0-SNAPSHOT'
}
```

Esta versão ainda não foi publicada. Consulte o
[build e os metadados](../README.md#2-build-e-validação) para validá-la
localmente.
