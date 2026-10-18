# commons-thread

Constrói e executa tarefas em threads, com opções para agendamento, timeout,
tratamento de erros e resultado.

[Voltar ao README principal](../README.md)

## Contrato

Use `br.com.armange.commons.thread.builder.ThreadBuilder` para escolher uma
tarefa `Runnable` ou `Callable` e configurar a execução. O tipo
`ExecutorResult` fornece os `Future`s e o `ExecutorService` resultantes.
A API antiga em `br.com.armange.commons.thread` está marcada como depreciada
para remoção; novos consumidores devem usar o pacote `builder`.

Com `setThreadNameSupplier`, a fábrica padrão de threads de plataforma consulta
o fornecedor uma vez para cada thread que cria. Como o executor reutiliza
threads do pool, isso não significa uma consulta por tarefa. Se o fornecedor
devolver `null`, a thread conserva o nome gerado.

Uma subclasse de `AbstractThreadBuilder` pode fornecer uma
`ExceptionCatcherThreadFactory`. Nesse caso, o builder consulta o fornecedor
uma vez ao configurar a fábrica e passa o resultado a `setThreadName(String)`.
A fábrica base usa esse nome nas threads seguintes; se receber `null`, conserva
o nome gerado. Uma subclasse da fábrica pode personalizar esse setter e
determinar outro nome efetivo.

Exemplo com uma tarefa simples:

```java
final ExecutorResult<?> result = ThreadBuilder
        .newBuilder()
        .setExecution(() -> System.out.println("tarefa"))
        .start();
result.getExecutorService().shutdown();
```

## Consumo

Versão de trabalho: `br.com.armange:commons-thread:3.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25.

```gradle
dependencies {
    implementation 'br.com.armange:commons-thread:3.0.0-SNAPSHOT'
}
```

A versão estável anterior `2.0.0`, disponível na Maven Central, foi
compilada para Java 11. A versão 3 eleva o requisito mínimo para Java 25 e
ainda não foi publicada. Os [relatórios históricos](../docs/README.md)
continuam separados dos resultados do build atual.
