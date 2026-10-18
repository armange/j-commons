# commons-object-impl

Implementa os contratos de conversão definidos em
[commons-object-api](../commons-object-api/README.md).

[Voltar ao README principal](../README.md)

## Contrato

O módulo oferece conversores de tipos e de beans. `commons-object-api` é
uma dependência transitiva de compilação porque suas interfaces aparecem nas
assinaturas públicas das implementações. Os módulos de mensagens, reflexão e
SPI são detalhes de implementação.

## Consumo

Versão de trabalho: `br.com.armange:commons-object-impl:1.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25.

```gradle
dependencies {
    implementation 'br.com.armange:commons-object-impl:1.0.0-SNAPSHOT'
}
```

O [consumidor local](../examples/consumer-jdk25/) compila e executa um
`StringToLongConverterImpl` declarando somente este módulo. Esta versão
ainda não foi publicada.
