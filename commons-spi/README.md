# commons-spi

Carrega implementações registradas pelo mecanismo Java Service Provider
Interface (SPI).

[Voltar ao README principal](../README.md)

## Contrato

`Loader.loadService(Class<T>)` busca uma implementação.
`Loader.loadServices(Class<T>)` obtém várias implementações e a sobrecarga
com `boolean` retorna um `Stream`. A ausência de implementação pode
produzir `NoImplementationFoundException`.

## Consumo

Versão de trabalho: `br.com.armange:commons-spi:2.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25.

```gradle
dependencies {
    implementation 'br.com.armange:commons-spi:2.0.0-SNAPSHOT'
}
```

A versão estável anterior `1.0.0`, disponível no GitHub Packages, foi
compilada para Java 8. A versão 2 eleva o requisito mínimo para Java 25 e
ainda não foi publicada. Os [relatórios históricos](../docs/README.md)
continuam separados dos resultados do build atual.
