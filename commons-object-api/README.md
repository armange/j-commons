# commons-object-api

Define os contratos de conversão de valores e beans, sem implementações de
conversores.

[Voltar ao README principal](../README.md)

## Contrato

`TypeConverter<S, T>` define `from`, `to` e `matches`. O módulo também
fornece interfaces especializadas, `BeanConverter` e anotações usadas por
conversores. As implementações ficam em
[commons-object-impl](../commons-object-impl/README.md).

## Consumo

Versão de trabalho: `br.com.armange:commons-object-api:1.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25. Uma biblioteca que expõe
`TypeConverter` em sua API pública deve declarar:

```gradle
dependencies {
    api 'br.com.armange:commons-object-api:1.0.0-SNAPSHOT'
}
```

Em aplicações, use `implementation`. Esta versão ainda não foi publicada;
consulte o [build local](../README.md#2-build-e-validação).
