# commons-reflection

Fornece fluxos e utilitários para inspecionar e acessar classes, construtores,
métodos, campos e anotações Java.

[Voltar ao README principal](../README.md)

## Contrato

`FieldSupport` lê e escreve campos de classes próprias quando o acesso
reflexivo é permitido. Em módulos do JDK que não abrem o pacote, a tentativa
de acesso a um campo privado propaga
`java.lang.reflect.InaccessibleObjectException`. O build não abre pacotes
do JDK globalmente com `--add-opens`.

## Consumo

Versão de trabalho: `br.com.armange:commons-reflection:1.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25.

```gradle
dependencies {
    implementation 'br.com.armange:commons-reflection:1.0.0-SNAPSHOT'
}
```

Esta versão ainda não foi publicada. Consulte o
[build local](../README.md#2-build-e-validação).
