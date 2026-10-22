# commons-thread

Constrói e executa tarefas em threads, com opções para agendamento, timeout,
tratamento de erros e resultado.

Use `Runnable` quando a tarefa não precisar devolver um valor e `Callable<T>`
quando ela precisar devolver `T` ou declarar uma exceção. Essa escolha também
define como obter o resultado e observar falhas por meio de um `Future`.

Na versão de trabalho `3.0.0-SNAPSHOT`, os builders do pacote `builder` mantêm
as doze facilidades descritas abaixo. `TryAsyncBuilder` também continua
disponível. Seis classes anteriormente marcadas para remoção saíram do pacote
raiz, e os nomes dos estágios sem agendamento do pacote `builder` foram
simplificados; a [seção de migração](#6-migração-das-seis-classes-removidas)
descreve as mudanças. As receitas diretas do JDK 25 são uma opção de composição.

[Voltar ao README principal](../README.md)

## Sumário

- [1. Facilidades da biblioteca](#1-facilidades-da-biblioteca)
  - [Quando usar a biblioteca](#quando-usar-a-biblioteca)
  - [Atraso](#atraso)
  - [Timeout](#timeout)
  - [Intervalo e repetição](#intervalo-e-repetição)
  - [Tratamento de falha](#tratamento-de-falha)
  - [Observação após execução](#observação-após-execução)
  - [Nome de thread](#nome-de-thread)
  - [Prioridade](#prioridade)
  - [Execução sem agendamento](#execução-sem-agendamento)
  - [Pool de threads](#pool-de-threads)
  - [Consumidor do resultado](#consumidor-do-resultado)
  - [Obter valor com Future](#obter-valor-com-future)
  - [Combinação das facilidades](#combinação-das-facilidades)
- [2. Builders de plataforma](#2-builders-de-plataforma)
- [3. Agendamento em threads virtuais](#3-agendamento-em-threads-virtuais)
- [4. TryAsyncBuilder](#4-tryasyncbuilder)
- [5. Composição direta com o JDK](#5-composição-direta-com-o-jdk)
  - [5.1. Tarefas simples com o JDK](#51-tarefas-simples-com-o-jdk)
  - [5.2. Tratamento assíncrono de falhas com o JDK](#52-tratamento-assíncrono-de-falhas-com-o-jdk)
  - [5.3. Recursos assíncronos com o JDK](#53-recursos-assíncronos-com-o-jdk)
  - [5.4. Observação de tarefas com o JDK](#54-observação-de-tarefas-com-o-jdk)
  - [5.5. Agendamento e prazo em threads de plataforma com o JDK](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk)
- [6. Migração das seis classes removidas](#6-migração-das-seis-classes-removidas)
- [7. Consumo](#7-consumo)

## 1. Facilidades da biblioteca

[Voltar ao sumário](#sumário)

As doze facilidades da documentação de 2.0.0 continuam disponíveis
nos builders do pacote `builder` nesta versão de trabalho. Cada item indica
a API da biblioteca e, quando útil, a composição opcional com o JDK 25.
As seis classes removidas do pacote raiz e sua migração estão na
[seção 6](#6-migração-das-seis-classes-removidas).

Os exemplos desta seção são trechos independentes de um método que declara
`throws Exception` e usam os imports abaixo. Os exemplos agendados fecham os
executores que criam, inclusive o cancelador usado com timeout. Nos estágios
sem agendamento, `start()` solicita o fechamento ordenado por padrão.

```java
import br.com.armange.commons.thread.builder.ThreadBuilder;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
```

As comparações de [execução sem agendamento](#execução-sem-agendamento) e
[leitura de um `Future`](#obter-valor-com-future) mostram a mesma tarefa com a
biblioteca e com o JDK. Nesses dois casos, o JDK oferece uma composição direta
para executar e aguardar; o chamador fecha seu executor. O `start()` padrão do
builder solicita `shutdown()` e retorna sem aguardar, enquanto o `close()` do
executor JDK aguarda sua terminação. Os builders ajudam sobretudo quando o mesmo uso
combina configuração temporal, callbacks e resultado; compare cada combinação
antes de escolher a API.

### Quando usar a biblioteca

Use os builders quando um `Runnable` precisar combinar atraso, repetição em
taxa fixa, prazo que cancela e observação de falhas. Para um `Callable`
individual, é possível combinar atraso ou timeout com consumo do resultado;
`Callable` periódico não é suportado. O
[exemplo de plataforma](#2-builders-de-plataforma) mostra os dois casos e o
fechamento dos executores de tarefa e de timeout. O mesmo builder pode enviar
várias tarefas a um pool configurado, como no [exemplo do pool](#2-builders-de-plataforma);
o tamanho do pool não cria tarefas automaticamente. Para uma série agendada em
threads virtuais novas, a [API virtual](#3-agendamento-em-threads-virtuais)
coordena prazo de disparo, cancelamento e quiescência sem um temporizador
separado fornecido pelo consumidor.

`TryAsyncBuilder` reúne tarefa, tratamento configurável de `Exception` e
finalizadores, inclusive variantes para recursos já abertos. Seu
`execute()` devolve `void`, encerra seu executor interno após os callbacks,
sem bloquear, e não entrega `Future`; consulte [o contrato](#4-tryasyncbuilder)
antes de depender de sua conclusão ou falhas.

Para um `Runnable` ou `Callable` isolado sem agendamento, a
[submissão direta ao JDK](#51-tarefas-simples-com-o-jdk) oferece `Future` e posse
explícita do executor. Veja os [dois exemplos equivalentes](#execução-sem-agendamento).
Prefira a [composição direta](#5-composição-direta-com-o-jdk)
também quando precisar escolher um `ExecutorService` arbitrário ou políticas
de agendamento que os builders não expõem.

### Atraso

Para iniciar uma vez depois de um prazo, use `setScheduling(...).setDelay(...)`
no `builder.ThreadBuilder` ou `ScheduledExecutorService.schedule(...)` como na
[seção 5.5](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk). O atraso é medido
antes da execução; cancelar o `ScheduledFuture` antes do disparo impede a
execução se ela ainda não começou.

```java
var result = ThreadBuilder
        .newBuilder()
        .setScheduling((Runnable) () -> System.out.println("iniciou"))
        .setDelay(200)
        .start();
try (var worker = result.getExecutorService()) {
    result.getFutures()
            .get(0)
            .get();
}
```

### Timeout

`setScheduling(...).setTimeout(...)` continua disponível no builder. Seu
prazo é contado desde a submissão, inclusive se houver atraso inicial ou
repetição, e cria um executor cancelador separado, exposto por
`getTimeoutExecutorResults()`. Feche esse executor e o executor da tarefa. Com
o JDK, use um temporizador independente para chamar `future.cancel(true)` no
prazo, como na [seção 5.5](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk):
`Future.get(timeout, unit)` limita só a espera. Interrupção é uma tentativa,
não uma confirmação de término do corpo.

```java
var result = ThreadBuilder
        .newBuilder()
        .setScheduling((Runnable) () ->
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1)))
        .setTimeout(100)
        .setMayInterruptIfRunning(true)
        .setSilentInterruption(true)
        .setUncaughtExceptionConsumer(failure -> System.err.println(failure))
        .start();
try (var worker = result.getExecutorService();
     var timer = result.getTimeoutExecutorResults()
             .get(0)
             .getExecutorService()) {
    try {
        result.getFutures()
                .get(0)
                .get();
    } catch (CancellationException expected) {
        System.out.println("prazo atingido");
    }
}
```

O cancelamento pelo prazo também é registrado no log do callback legado,
mesmo quando o chamador trata `CancellationException`.

### Intervalo e repetição

`setInterval(...)` no builder de `Runnable` usa **taxa fixa**, equivalente a
`scheduleAtFixedRate(...)`; não sobrepõe execuções da mesma série. Para atraso
fixo depois de cada conclusão, use `scheduleWithFixedDelay(...)` do JDK. O
builder de `Callable` aceita configurar intervalo, mas `start()` lança
`UnsupportedOperationException` nessa combinação. Veja a
[seção 5.5](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk) e o
[exemplo do builder](#2-builders-de-plataforma).

```java
var cycles = new CountDownLatch(3);
var result = ThreadBuilder
        .newBuilder()
        .setScheduling((Runnable) cycles::countDown)
        .setInterval(100)
        .start();
try (var worker = result.getExecutorService()) {
    cycles.await(1, TimeUnit.SECONDS);
    result.getFutures()
            .get(0)
            .cancel(false);
}
```

### Tratamento de falha

`setUncaughtExceptionConsumer(...)` permanece nos builders e observa falhas
que seu executor extrai do `Future`. Quem precisa ler a causa diretamente
pode usar `Future.get()`, que lança `ExecutionException`, como na
[seção 5.2](#52-tratamento-assíncrono-de-falhas-com-o-jdk). Um
`UncaughtExceptionHandler` de `ThreadFactory` não substitui essa leitura:
`submit(...)` captura a falha no `Future`.

```java
var result = ThreadBuilder
        .newBuilder()
        .setScheduling((Runnable) () -> {
            throw new IllegalStateException("falha");
        })
        .setDelay(100)
        .setUncaughtExceptionConsumer(failure ->
                System.err.println("falha observada: " + failure.getCause()))
        .start();
try (var worker = result.getExecutorService()) {
    try {
        result.getFutures()
                .get(0)
                .get();
    } catch (ExecutionException expected) {
        System.err.println("causa: " + expected.getCause());
    }
}
```

### Observação após execução

`setAfterExecuteConsumer(...)` ainda registra callback no executor do builder.
Ele pode receber eventos de outras tarefas do mesmo executor; o argumento de
falha de `afterExecute` pode ser `null` para tarefa cujo `Future` capturou a
exceção. Para observação isolada, use `whenComplete(...)`,
`ExecutorCompletionService` ou um `Runnable` composto para cada ciclo, como na
[seção 5.4](#54-observação-de-tarefas-com-o-jdk).

```java
var result = ThreadBuilder
        .newBuilder()
        .setScheduling((Runnable) () -> System.out.println("tarefa"))
        .setDelay(100)
        .setAfterExecuteConsumer((task, failure) ->
                System.out.println("execução encerrada"))
        .start();
try (var worker = result.getExecutorService()) {
    result.getFutures()
            .get(0)
            .get();
}
```

### Nome de thread

`setThreadNameSupplier(...)` continua disponível no builder: sua fábrica
padrão consulta o fornecedor por **thread criada**, não por tarefa. A fábrica
fornecida por uma subclasse segue a política descrita na
[seção 2](#2-builders-de-plataforma). O JDK também permite configurar nome via
`ThreadFactory`; veja a [seção 6](#6-migração-das-seis-classes-removidas).

```java
var names = new AtomicInteger();
ThreadBuilder
        .newBuilder()
        .setExecution((Runnable) () ->
                System.out.println(Thread.currentThread()
                        .getName()))
        .setThreadNameSupplier(() -> "worker-" + names.incrementAndGet())
        .start()
        .get();
```

### Prioridade

`setThreadPrioritySupplier(...)` configura a prioridade das threads de
plataforma criadas pelo builder. No JDK, use `Thread.ofPlatform().priority(...)`
na fábrica do executor. Prioridade de thread virtual é fixa e não deve ser
oferecida como opção equivalente; o [exemplo de combinação](#combinação-das-facilidades)
usa plataforma.

```java
ThreadBuilder
        .newBuilder()
        .setExecution((Runnable) () ->
                System.out.println(Thread.currentThread()
                        .getPriority()))
        .setThreadPrioritySupplier(() -> Thread.NORM_PRIORITY + 1)
        .start()
        .get();
```

### Execução sem agendamento

`builder.ThreadBuilder.newBuilder().setExecution(Runnable)` continua disponível;
para valor, passe um `Callable<T>` a `setExecution(...)`. A composição direta
com `ExecutorService.submit(...)` retorna um `Future` por tarefa. A
[seção 5.1](#51-tarefas-simples-com-o-jdk) mostra plataforma, virtual e executor
fornecido.

**Biblioteca:**

```java
ThreadBuilder
        .newBuilder()
        .setExecution((Runnable) () -> System.out.println("tarefa"))
        .start()
        .get();
```

**JDK, mesma tarefa com executor fechado pelo chamador:**

```java
try (var worker = Executors.newSingleThreadExecutor()) {
    worker.submit((Runnable) () -> System.out.println("tarefa"))
            .get();
}
```

### Pool de threads

`builder.ThreadBuilder.newBuilder(poolSize)` define o tamanho base do executor
criado pelo builder. O `start()` final solicita seu fechamento ordenado por
padrão, após enviar as tarefas. Use `keepExecutorOpen()` antes desse `start()`
se quiser manter o executor e assumir seu fechamento. O [exemplo do pool](#2-builders-de-plataforma)
mostra `startAndBuildOther()` enviando três tarefas para dois workers. Para
controlar diretamente tamanho, fábrica e
ciclo de vida, use `Executors.newFixedThreadPool(...)` ou outro executor do
JDK, como na [seção 5.1](#51-tarefas-simples-com-o-jdk). Tarefas virtuais simples
podem usar `newVirtualThreadPerTaskExecutor()` sem pool fixo.

```java
List<Future<Void>> futures = ThreadBuilder
        .newBuilder(2)
        .setExecution((Runnable) () -> System.out.println("primeira"))
        .startAndBuildOther()
        .setExecution(() -> System.out.println("segunda"))
        .start();
for (Future<Void> task : futures) {
    task.get();
}
```

### Consumidor do resultado

Após `setExecution(Callable<T>)`, o builder aceita
`setThreadResultConsumer(...)`, mostrado na [seção 2](#2-builders-de-plataforma).
No JDK, observe o `Future` ou componha o estágio da tarefa, como na
[seção 5.4](#54-observação-de-tarefas-com-o-jdk). O callback do builder pode terminar depois
de `Future.get()` retornar; se precisar aguardar o próprio callback, inclua-o
no corpo da tarefa ou use um estágio dependente retido.

```java
var builder = ThreadBuilder
        .newBuilder()
        .setScheduling((Callable<Integer>) () -> 42)
        .setDelay(100);
builder.setThreadResultConsumer(number -> System.out.println("resultado: " + number));
var result = builder.start();
try (var worker = result.getExecutorService()) {
    result.getFutures()
            .get(0)
            .get();
}
```

### Obter valor com Future

`Future<T>.get()` devolve o resultado de um `Callable` individual iniciado
pelo estágio padrão do builder, ou lança `ExecutionException`/
`CancellationException` conforme o estado. `ExecutorService.submit(Callable<T>)`
do JDK devolve diretamente `Future<T>`, como na
[seção 5.1](#51-tarefas-simples-com-o-jdk). Retenha o `Future` correspondente a
cada tarefa se precisar consultá-lo ou cancelá-lo depois. O `start()` sem
agendamento solicita o fechamento ordenado do executor por padrão; com
`keepExecutorOpen()`, o fechamento cabe ao chamador.

**Biblioteca:**

```java
System.out.println(ThreadBuilder
        .newBuilder()
        .setExecution((Callable<Integer>) () -> 42)
        .start()
        .get());
```

**JDK, mesmo resultado com executor fechado pelo chamador:**

```java
try (var worker = Executors.newSingleThreadExecutor()) {
    System.out.println(worker
            .submit((Callable<Integer>) () -> 42)
            .get());
}
```

### Combinação das facilidades

O [exemplo do builder](#2-builders-de-plataforma) combina pool, nome, prioridade,
atraso, taxa fixa, prazo e callback; o exemplo `Callable` ao lado combina
consumidor e `Future`. A
[composição opcional com o JDK](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk)
reúne essas capacidades em uma aplicação de plataforma.

```java
var result = ThreadBuilder
        .newBuilder(2)
        .setScheduling((Runnable) () -> System.out.println("ciclo"))
        .setDelay(100)
        .setInterval(100)
        .setTimeout(350)
        .setThreadNameSupplier(() -> "worker")
        .setAfterExecuteConsumer((task, failure) ->
                System.out.println("ciclo observado"))
        .start();
try (var worker = result.getExecutorService();
     var timer = result.getTimeoutExecutorResults()
             .get(0)
             .getExecutorService()) {
    try {
        result.getFutures()
                .get(0)
                .get();
    } catch (CancellationException expected) {
        System.out.println("série encerrada pelo prazo");
    }
}
```

## 2. Builders de plataforma

[Voltar ao sumário](#sumário)

`br.com.armange.commons.thread.builder.ThreadBuilder.setExecution(...)`
continua disponível para `Runnable` e `Callable`. A entrada devolve um estágio
`SingleThreadBuilder`; `startAndBuildOther()` envia a primeira tarefa e
devolve um estágio `MultipleThreadBuilder`. Cada lote compartilha um
`ScheduledThreadBuilderExecutor`. A escolha de `keepExecutorOpen()` muda o
tipo do estágio e o contrato de retorno:

| Estágio | `start()` final | Posse do executor |
| --- | --- | --- |
| Single padrão | `Future<S>` da tarefa | O builder solicita `shutdown()` antes de retornar. |
| Multiple padrão | `List<Future<S>>` imutável do lote, em ordem de submissão | O builder solicita `shutdown()` antes de retornar. |
| `OpenSingleThreadBuilder` | `SingleTaskExecutorResult<S>` com `getFuture()` e `getExecutorService()` | O chamador fecha o executor recebido. |
| `OpenMultipleThreadBuilder` | `MultipleTaskExecutorResult<S>` com lista imutável em `getFutures()` e executor | O chamador fecha o executor recebido. |

Os estágios especializados `SingleCallableBuilder`,
`MultipleCallableBuilder`, `OpenSingleCallableBuilder` e
`OpenMultipleCallableBuilder` conservam a fluência de
`setThreadResultConsumer(...)`. `start()` final retorna sem esperar as tarefas
ou interrompê-las; consulte os `Future`s para resultado ou falha.
`startAndBuildOther()` apenas envia a tarefa intermediária e não fecha o
executor. Se um envio falhar antes do primeiro resultado `Open`, o builder
solicita `shutdown()` sem esconder a falha; após a entrega do executor, seu
fechamento continua a cargo do chamador, inclusive após falha posterior.

Após `keepExecutorOpen()`, os aliases padrão ficam inválidos para configuração
e submissão. O estágio Single fica bloqueado enquanto um lote Multiple está
aberto. O estágio Multiple deixa de aceitar chamadas após seu `start()`; o
Single `Open` retido pode iniciar outro lote no mesmo executor. Se a opção
`Open` foi escolhida no Multiple, o Single padrão retido pode ser convertido
uma vez por `keepExecutorOpen()` após o fim desse lote. A opção de manter o
executor aberto persiste para os lotes seguintes. Cada resultado Multiple
conserva o snapshot dos `Future`s do seu próprio lote.

`setScheduling(...)` continua devolvendo `TimingRunnableBuilder` ou
`TimingCallableBuilder` e `ExecutorResult` com a lista de `Future`s, sem
mudança no fluxo de agendamento. Timeout e as demais facilidades dos builders
continuam disponíveis. Para quem prefere compor executores diretamente, a
[seção 5](#5-composição-direta-com-o-jdk) mostra alternativas do JDK.
`setInterval()` usa taxa fixa, embora sua descrição antiga possa sugerir atraso após cada
conclusão. `TimingCallableBuilder` aceita configurar um intervalo, mas
`start()` lança `UnsupportedOperationException` nessa combinação.

`setSilentInterruption(boolean)` também permanece: quando ativado, evita
enviar `CancellationException` ao consumidor configurado por
`setUncaughtExceptionConsumer(...)`, mas não muda o estado do `Future` nem
transforma cancelamento em conclusão normal. O callback legado ainda registra
a ocorrência no log. O [exemplo de timeout](#timeout) mostra os dois setters.

As seis classes do pacote raiz descritas na [seção 6](#6-migração-das-seis-classes-removidas)
não estão disponíveis nesta versão de trabalho. Os builders do pacote
`builder` permanecem disponíveis.

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

O `ThreadBuilder` **do pacote `builder`** ainda combina atraso, repetição em
taxa fixa e timeout para `Runnable`. O exemplo usa pool de duas threads de
plataforma, nome e prioridade configurados, além de um callback após cada
execução. O callback recebe a tarefa do executor e pode receber `null` no
parâmetro de falha mesmo quando o `Future` contém a exceção; leia o `Future`
para saber se a série falhou. O `ExecutorResult` expõe tanto o executor da
tarefa quanto os executores criados para o timeout: quem inicia esse builder
deve fechar todos eles.

```java
import br.com.armange.commons.thread.builder.ExecutorResult;
import br.com.armange.commons.thread.builder.ThreadBuilder;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class BuilderScheduledExample {
    public static void main(final String[] args) throws Exception {
        final AtomicInteger names = new AtomicInteger();
        final ExecutorResult<Void> result = ThreadBuilder
                .newBuilder(2)
                .setScheduling((Runnable) () -> System.out.println("ciclo"))
                .setDelay(20)
                .setInterval(20)
                .setTimeout(120)
                .setMayInterruptIfRunning(true)
                .setThreadNameSupplier(() -> "worker-" + names.incrementAndGet())
                .setThreadPrioritySupplier(() -> 4)
                .setUncaughtExceptionConsumer(failure ->
                        System.out.println("falha observada: " + failure.getClass().getSimpleName()))
                .setAfterExecuteConsumer((task, failure) ->
                        System.out.println("ciclo encerrado"))
                .start();
        final Future<Void> series = result.getFutures()
                .get(0);

        try {
            series.get();
        } catch (final CancellationException cancellation) {
            System.out.println("série cancelada pelo prazo");
        } catch (final ExecutionException failure) {
            System.err.println(failure.getCause());
        } finally {
            series.cancel(true);
            result.getExecutorService()
                    .close();

            for (final ExecutorResult<Void> timeoutResult : result.getTimeoutExecutorResults()) {
                timeoutResult.getExecutorService()
                        .close();
            }
        }
    }
}
```

Para uma tarefa com valor, o mesmo `ThreadBuilder` do pacote `builder` aceita
`Callable`, consumidor do resultado e consulta pelo `Future`. O consumidor é
chamado pelo callback do executor; `Future.get()` pode retornar antes de esse
callback terminar. O `start()` padrão solicita `shutdown()` após enviar a
tarefa e devolve diretamente o `Future`.

```java
import br.com.armange.commons.thread.builder.ThreadBuilder;

import java.util.concurrent.Callable;

public class BuilderCallableExample {
    public static void main(final String[] args) throws Exception {
        System.out.println("Future: " + ThreadBuilder
                .newBuilder()
                .setExecution((Callable<Integer>) () -> 42)
                .setThreadResultConsumer(value -> System.out.println("consumidor: " + value))
                .start()
                .get());
    }
}
```

`newBuilder(2)` configura dois workers, mas não cria duas tarefas por si só.
Use `startAndBuildOther()` para enviar tarefas adicionais ao mesmo executor;
o `start()` padrão devolve um `Future` por envio. Aqui três tarefas
compartilham o pool e o builder solicita `shutdown()` após o último envio:

```java
import br.com.armange.commons.thread.builder.ThreadBuilder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class BuilderPoolExample {
    private static Runnable task(final CountDownLatch start, final int number) {
        return () -> {
            try {
                start.await();
                final String threadName = Thread.currentThread()
                        .getName();
                System.out.println("tarefa " + number + " em " + threadName);
            } catch (final InterruptedException interruption) {
                Thread.currentThread()
                        .interrupt();
            }
        };
    }

    public static void main(final String[] args) throws Exception {
        final CountDownLatch start = new CountDownLatch(1);
        final List<Future<Void>> futures = ThreadBuilder
                .newBuilder(2)
                .setExecution(task(start, 1))
                .startAndBuildOther()
                .setExecution(task(start, 2))
                .startAndBuildOther()
                .setExecution(task(start, 3))
                .start();

        start.countDown();
        for (final Future<Void> future : futures) {
            future.get();
        }
    }
}
```

## 3. Agendamento em threads virtuais

[Voltar ao sumário](#sumário)

`VirtualScheduledThreadBuilderExecutor` cria uma thread virtual nova por
execução ou ciclo. Essa mesma thread aguarda o prazo e executa o corpo; não há
temporizador nem executor próprio. Há uma thread virtual em espera por handle
pendente. Feche o agendador quando terminar. Para uma tarefa virtual sem
agendamento, o JDK oferece `Executors.newVirtualThreadPerTaskExecutor()`
como na [seção 5.1](#51-tarefas-simples-com-o-jdk); o
`builder.ThreadBuilder.setExecution(...)` continua disponível para tarefas
de plataforma.
As threads carrier usadas internamente pelo JDK não são configuradas por esta API.

Com o JDK direto, o consumidor pode criar uma thread virtual, aguardar o
prazo em um laço que reverifique o relógio monotônico e executar a tarefa.
Para cancelar a espera sem interromper o corpo, preservar resultado/falha e
repetir sem sobreposição, precisa coordenar estado, interrupção e criação da
próxima thread. Esta classe reúne essa coordenação e aceita uma fábrica de
threads virtuais. Quem precisar controlar um temporizador externo pode
compor `ScheduledExecutorService` e um executor virtual diretamente.

`schedule(Runnable/Callable, delay, unit)` executa uma vez. `get()` devolve o
valor de `Callable` ou `null` para `Runnable`, e encapsula uma falha em
`ExecutionException`. Para repetições, `scheduleAtFixedRate` mede a taxa entre
inícios planejados e `scheduleWithFixedDelay` mede o atraso após o fim de cada
corpo. As séries aceitam `Runnable`; seus `get()` não terminam normalmente
enquanto elas estiverem ativas. Os ciclos da mesma série não se sobrepõem e
cada um recebe uma thread virtual diferente. O `Future` representa a série
inteira, sem expor uma posição em fila de temporizador.

```java
import br.com.armange.commons.thread.core.VirtualScheduledTask;
import br.com.armange.commons.thread.core.VirtualScheduledThreadBuilderExecutor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class VirtualScheduledTasks {
    public static void main(final String[] args) throws Exception {
        try (VirtualScheduledThreadBuilderExecutor scheduler =
                     VirtualScheduledThreadBuilderExecutor.create()) {
            final VirtualScheduledTask<Integer> value = scheduler.schedule(() -> {
                System.out.println("Virtual: " + Thread.currentThread().isVirtual());

                return 42;
            }, 10, TimeUnit.MILLISECONDS);

            System.out.println("Resultado: " + value.get());
            value.quiescence().toCompletableFuture().get();

            final CountDownLatch cycles = new CountDownLatch(2);
            final VirtualScheduledTask<Void> series = scheduler.scheduleAtFixedRate(
                    () -> {
                        System.out.println("Ciclo: " + Thread.currentThread());
                        cycles.countDown();
                    }, 0, 10, TimeUnit.MILLISECONDS);

            try {
                if (!cycles.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("A série não completou dois ciclos");
                }
            } finally {
                series.cancel(false);
            }

            series.quiescence().toCompletableFuture().get();
        }
    }
}
```

O construtor público recebe uma `ThreadFactory`. Ela precisa criar uma thread
virtual nova e ainda não iniciada para cada execução ou ciclo; uma thread de
plataforma, um valor `null` ou uma falha da fábrica conclui o handle com
falha. O exemplo personaliza os nomes e fecha o agendador:

```java
import br.com.armange.commons.thread.core.VirtualScheduledTask;
import br.com.armange.commons.thread.core.VirtualScheduledThreadBuilderExecutor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class VirtualScheduledWithFactory {
    public static void main(final String[] args) throws Exception {
        final ThreadFactory virtualFactory = Thread.ofVirtual()
                .name("virtual-task-", 0)
                .factory();

        try (VirtualScheduledThreadBuilderExecutor scheduler =
                     new VirtualScheduledThreadBuilderExecutor(virtualFactory)) {
            final CountDownLatch cycles = new CountDownLatch(2);
            final VirtualScheduledTask<Void> task = scheduler.scheduleWithFixedDelay(
                    () -> {
                        System.out.println(Thread.currentThread().getName());
                        cycles.countDown();
                    },
                    0, 20, TimeUnit.MILLISECONDS);

            if (!cycles.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("A série não completou dois ciclos");
            }

            task.cancel(false);
            task.quiescence().toCompletableFuture().get();

            final VirtualScheduledTask<String> pending = scheduler.schedule(
                    () -> "não executado", 1, TimeUnit.HOURS);
            pending.cancel(false);
            pending.quiescence().toCompletableFuture().get();
        }
    }
}
```

`cancel(false)` impede disparos futuros e permite que um corpo já iniciado
termine; `cancel(true)` também tenta interrompê-lo. Em ambos os casos, `get()`
passa a lançar `CancellationException` assim que o cancelamento é aceito.
`quiescence()` é um estágio de leitura que só termina quando não há corpo ativo
nem espera pendente. Se o corpo falhar depois de um cancelamento, `get()`
continua cancelado e `quiescence()` relata a falha; observe ambos quando a
falha tardia importar. Uma falha de um ciclo encerra a série. O cancelamento
não garante que um corpo que ignora interrupção pare; `close()` aguarda corpos
já iniciados e pode demorar. Chame `close()` fora do corpo, da fábrica de
threads fornecida e dos callbacks síncronos de `quiescence()`.

Threads virtuais são daemon: uma tarefa pendente não mantém o processo vivo.
Enquanto depender de seu resultado ou encerramento, aguarde `get()`,
`quiescence()` ou `close()` em uma thread não daemon ativa.

Esta API não implementa `ScheduledExecutorService` nem fornece
`ScheduledFuture` ou timeout automático. Para combinar um prazo com atraso ou
série, use um temporizador independente, como na
[seção 5.5](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk),
cancelando o `VirtualScheduledTask` no alarme. O temporizador e a precisão do
alarme pertencem ao consumidor; a espera desta classe usa relógio monotônico
e é de melhor esforço.

## 4. TryAsyncBuilder

[Voltar ao sumário](#sumário)

`TryAsyncBuilder`, `addCatcher()` e `finallyAsync()` também permanecem
disponíveis nesta versão. A cadeia
`tryAsync(...).addCatcher(...).finallyAsync(...).execute()` reúne execução
assíncrona, tratamento configurável de exceções e finalização; as sobrecargas
com recursos também fecham os `Closeable`s recebidos. Seu `execute()` retorna
`void` e cria um executor internamente; não entrega ao chamador um `Future`
para aguardar, cancelar ou observar falhas da tarefa, nem um executor para
fechar manualmente. O executor encerra após a tarefa e seus callbacks,
inclusive quando um callback lança `Exception`, sem bloquear a chamada a
`execute()`. Uma rejeição de submissão é propagada e também encerra o
executor; a tarefa e seus finalizadores não são executados nesse caso.

```java
import br.com.armange.commons.thread.async.AsyncRuntimeException;
import br.com.armange.commons.thread.async.TryAsyncBuilder;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TryAsyncExample {
    public static void main(final String[] args) throws Exception {
        final CountDownLatch finished = new CountDownLatch(1);

        TryAsyncBuilder
                .tryAsync(() -> {
                    throw new IOException("falha da tarefa");
                })
                .addCatcher(AsyncRuntimeException.class, failure ->
                        System.out.println("falha tratada"))
                .finallyAsync(finished::countDown)
                .execute();

        if (!finished.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("A finalização não terminou");
        }
    }
}
```

O consumidor de resultado é chamado antes dos
finalizadores, depois da conclusão do
`Future` interno. Exceções lançadas por esses callbacks ficam somente no log;
`Error` não é capturado pelo callback legado. Para obter um `Future` próprio
e controlar o executor, use o `builder.ThreadBuilder` da
[seção 2](#2-builders-de-plataforma) ou a composição
direta do JDK na [seção 5.2](#52-tratamento-assíncrono-de-falhas-com-o-jdk).
As variantes que recebem recursos continuam disponíveis e esperam recursos já
abertos. Nas variantes de array e mapa, encadear `addCatcher()` ou
`finallyAsync()` pode lançar `ClassCastException` devido ao tipo fluente
herdado; não dependa dessa combinação até sua correção. Nessas variantes, o
fechamento segue a ordem de iteração e para na primeira `IOException`, sem
preservar as demais falhas como exceções suprimidas. A composição direta com
`try-with-resources`, incluindo aquisição dentro da tarefa, está na
[seção 5.3](#53-recursos-assíncronos-com-o-jdk).

## 5. Composição direta com o JDK

[Voltar ao sumário](#sumário)

As receitas abaixo usam diretamente o JDK 25 quando o consumidor prefere
compor seus próprios executores, tarefas e observadores. As facilidades da
biblioteca permanecem nas seções anteriores.

### 5.1. Tarefas simples com o JDK

[Voltar ao sumário](#sumário)

Para tarefas sem agendamento, use `ExecutorService.submit(...)` diretamente.
Cada chamada devolve um `Future` próprio. O exemplo abaixo usa um pool de
threads de plataforma para executar um `Runnable` e um `Callable`:

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlatformTasks {
    public static void main(final String[] args) throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            final Future<?> action = executor.submit(() -> {
                final Thread worker = Thread.currentThread();

                System.out.println(worker.getName());
            });
            final Future<Integer> result = executor.submit(() -> 40 + 2);

            action.get();
            System.out.println(result.get());
        }
    }
}
```

Para criar uma thread virtual por tarefa, troque a criação do executor. O
restante do uso de `Runnable`, `Callable` e `Future` permanece igual:

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualTasks {
    public static void main(final String[] args) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final Future<?> action = executor.submit(() -> {
                final Thread worker = Thread.currentThread();

                System.out.println(worker.isVirtual());
            });
            final Future<Integer> result = executor.submit(() -> 40 + 2);

            action.get();
            System.out.println(result.get());
        }
    }
}
```

Nos dois exemplos, `get()` aguarda a conclusão da tarefa. Quando ela termina
normalmente, o `Future` do `Runnable` devolve `null` e o do `Callable` devolve
seu resultado. Se a tarefa falhar, `get()` lança `ExecutionException`, cuja
causa é a falha original.
Após cancelamento, `get()` lança `CancellationException`. O `try` fecha o
executor criado nesses exemplos e aguarda seu término.

Também é possível fornecer uma `ThreadFactory` ao criar o executor. Neste
exemplo, `main` escolhe fábricas de plataforma e virtual e fecha os executores
que cria. `runWith` recebe cada executor do chamador e não o fecha:

```java
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class SuppliedExecutorTasks {
    public static void runWith(final ExecutorService executor) throws InterruptedException {
        observeFailure(executor);
        requestCancellation(executor);
    }

    private static void observeFailure(final ExecutorService executor) throws InterruptedException {
        final Callable<Integer> failingTask = () -> {
            throw new IllegalStateException("falha");
        };
        final Future<Integer> failed = executor.submit(failingTask);

        try {
            failed.get();
        } catch (ExecutionException exception) {
            System.out.println(exception.getCause());
        }
    }

    private static void requestCancellation(final ExecutorService executor) {
        final Future<?> pending = executor.submit(() -> {
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException exception) {
                Thread.currentThread()
                        .interrupt();
            }
        });
        final boolean cancellationAccepted = pending.cancel(true);

        System.out.println("Pedido aceito: " + cancellationAccepted);
        System.out.println("Future cancelado: " + pending.isCancelled());
    }

    public static void main(final String[] args) throws InterruptedException {
        final ThreadFactory platformFactory = Thread.ofPlatform()
                .name("platform-", 0)
                .factory();

        try (ExecutorService executor = Executors.newFixedThreadPool(2, platformFactory)) {
            System.out.println("Plataforma:");
            runWith(executor);
        }

        final ThreadFactory virtualFactory = Thread.ofVirtual()
                .name("virtual-", 0)
                .factory();

        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(virtualFactory)) {
            System.out.println("Virtual:");
            runWith(executor);
        }
    }
}
```

`Future.cancel(true)` tenta cancelar a tarefa e, se ela já estiver em execução,
solicita a interrupção da thread. O retorno de `cancel(true)` informa se o
pedido foi aceito, e `isCancelled()` informa o estado do `Future`; nenhum dos
dois confirma que o código da tarefa parou. A tarefa precisa reagir à
interrupção.

### 5.2. Tratamento assíncrono de falhas com o JDK

[Voltar ao sumário](#sumário)

Como alternativa com controle do `ExecutorService` e um `Future` observável,
coloque `try/catch/finally` dentro de um `Callable` e submeta-o ao executor.
O exemplo recebe o
executor do chamador, trata uma `IOException` específica sem ocultá-la e usa o
`Future` para observar valor e falha. A mesma operação funciona com threads de
plataforma e virtuais:

```java
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncTryTasks {
    private static Future<Integer> submit(final ExecutorService executor,
                                          final Callable<Integer> operation) {
        return executor.submit(() -> {
            try {
                return operation.call();
            } catch (final IOException exception) {
                System.out.println("Falha tratada: " + exception.getMessage());

                throw exception;
            } finally {
                System.out.println("Finalização");
            }
        });
    }

    private static void runWith(final ExecutorService executor) throws Exception {
        final Future<Integer> success = submit(executor, () -> 42);
        final Future<Integer> failure = submit(executor, () -> {
            throw new IOException("entrada inválida");
        });

        System.out.println(success.get());

        try {
            failure.get();
        } catch (final ExecutionException exception) {
            System.out.println(exception.getCause());
        }
    }

    public static void main(final String[] args) throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            runWith(executor);
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runWith(executor);
        }
    }
}
```

`runWith` não fecha o executor recebido; `main` fecha os dois executores que
criou. O `finally` termina antes da conclusão normal ou excepcional observada
por `Future.get()`. Se o `catch` consumir a exceção sem relançá-la, o `Future`
representará uma conclusão normal. Se o `finally` também lançar uma exceção,
ela pode substituir a falha original; agregue as duas explicitamente quando
precisar observar ambas.

Um consumidor de resultado chamado dentro do `Callable` integra sua falha ao
`Future`. Se for chamado após `get()`, sua falha pertence ao código do chamador.
Para operações sem valor que lançam exceção verificada, use `Callable<Void>` e
retorne `null`; `Runnable` não declara exceções verificadas. Use
`CompletableFuture` quando precisar compor tarefas e seu contrato de
cancelamento for adequado: `CompletableFuture.cancel(true)` não interrompe a
computação subjacente. `Future.cancel(true)` apenas tenta interromper uma tarefa
em execução, como descrito acima.

### 5.3. Recursos assíncronos com o JDK

[Voltar ao sumário](#sumário)

Adquira os recursos dentro da tarefa submetida e use `try-with-resources`.
Assim, uma tarefa rejeitada ou cancelada antes de iniciar não deixa recursos
abertos por ela. O exemplo usa dois `AutoCloseable` que falham ao fechar para
mostrar como `Future.get()` expõe a falha da tarefa e as falhas de fechamento:

```java
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncResourceTasks {
    private static final class FailingResource implements AutoCloseable {
        private final String name;

        private FailingResource(final String name) {
            this.name = name;
        }

        @Override
        public void close() throws IOException {
            throw new IOException("fechar " + name);
        }
    }

    private static Future<Void> submit(final ExecutorService executor) {
        final Callable<Void> task = () -> {
            try (FailingResource first = new FailingResource("primeiro");
                 FailingResource second = new FailingResource("segundo")) {
                throw new IOException("executar tarefa");
            }
        };

        return executor.submit(task);
    }

    private static void runWith(final ExecutorService executor)
            throws InterruptedException {
        try {
            submit(executor).get();
        } catch (final ExecutionException exception) {
            final Throwable failure = exception.getCause();

            System.out.println(failure.getMessage());

            for (final Throwable closeFailure : failure.getSuppressed()) {
                System.out.println(closeFailure.getMessage());
            }
        }
    }

    public static void main(final String[] args) throws InterruptedException {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            runWith(executor);
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runWith(executor);
        }
    }
}
```

Em cada execução, a falha `executar tarefa` é a causa de
`ExecutionException`; `fechar segundo` e `fechar primeiro` aparecem, nessa
ordem, em `getSuppressed()`. O JDK fecha em ordem inversa à declaração e tenta
fechar ambos mesmo quando um fechamento falha. Sem cancelamento, o fechamento
termina antes de `Future.get()` devolver o valor ou a falha. `runWith` não fecha
o executor recebido; `main` fecha os executores que criou.

Para uma quantidade dinâmica de recursos, como array ou mapa, o cabeçalho de
`try-with-resources` não aceita a coleção diretamente. Mantenha na tarefa uma
lista dos recursos adquiridos. Em qualquer saída, inclusive falha durante uma
aquisição posterior, feche cada recurso adquirido uma vez, na ordem inversa, e
tente todos os fechamentos. Preserve a falha da operação como principal e anexe
as falhas de fechamento com `Throwable.addSuppressed(...)`; se apenas o
fechamento falhar, a primeira falha de fechamento será a principal. A escolha
sobre entradas `null`, objetos repetidos e ordem de um mapa pertence ao
consumidor. Recursos abertos antes da submissão continuam sob responsabilidade
de quem os abriu, inclusive se a tarefa for rejeitada ou cancelada antes de
começar.

`Future.cancel(true)` pode concluir o `Future` antes de o fechamento terminar.
Nesse caso, `get()` lança `CancellationException` e não expõe uma falha posterior
de `close()`. Se essa falha precisar ser observada, use um canal de conclusão
independente, preenchido pela tarefa após o fechamento; trate à parte a tarefa
que nunca começou. Cancelamento não confirma que os recursos foram fechados.

### 5.4. Observação de tarefas com o JDK

[Voltar ao sumário](#sumário)

Para observar cada tarefa isoladamente, associe `whenComplete(...)` ao seu
`CompletableFuture` e guarde o estágio devolvido. O evento observado é a
conclusão do estágio: se ele for cancelado, o observador pode ser chamado
enquanto o corpo de `supplyAsync` ainda executa. Cancelamento do estágio não
confirma o término da tarefa. O observador recebe o valor ou a falha apenas
do estágio ao qual foi associado. Neste exemplo, a segunda tarefa termina
normalmente, mas seu observador falha; a falha fica no estágio
dependente:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObservedTasks {
    private static void runWith(final ExecutorService executor) {
        final CompletableFuture<Integer> first = CompletableFuture.supplyAsync(
                () -> 42, executor);
        final CompletableFuture<Integer> firstObserved = first
                .whenComplete((value, failure) ->
                        System.out.println("Primeira tarefa: " + value));

        final CompletableFuture<Integer> second = CompletableFuture.supplyAsync(
                () -> 7, executor);
        final CompletableFuture<Integer> secondObserved = second
                .whenComplete((value, failure) -> {
                    System.out.println("Segunda tarefa: " + value);

                    throw new IllegalStateException("falha do observador");
                });

        System.out.println("Resultado da primeira: " + firstObserved.join());
        System.out.println("Resultado da segunda: " + second.join());

        try {
            secondObserved.join();
        } catch (final CompletionException exception) {
            System.out.println(exception.getCause());
        }
    }

    public static void main(final String[] args) {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            runWith(executor);
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runWith(executor);
        }
    }
}
```

`runWith` usa o executor recebido sem fechá-lo; `main` fecha os dois executores
que criou. No modo virtual, a tarefa usa o executor virtual, mas o observador
de `whenComplete` sem `Async` pode rodar na thread que conclui a tarefa ou na
thread que registra o estágio após a conclusão; ele não tem garantia de rodar
em uma thread virtual. `whenComplete` também roda quando a tarefa falha. Se
tarefa e observador falharem, a falha da tarefa prevalece no estágio dependente;
não dependa da presença da falha do observador em `getSuppressed()`. A execução
do observador integra a conclusão do estágio dependente, mas não muda a conclusão
do `CompletableFuture` original. Para várias tarefas cujo resultado deve ser
lido na ordem em que terminam, crie um `ExecutorCompletionService` com o
executor fornecido, submeta cada `Callable` por `completionService.submit(...)`
e consuma cada conclusão com `take().get()`. Os `Future`s criados diretamente
por `executor.submit(...)` não entram nessa fila. O `ExecutorCompletionService`
não fecha o executor. Se o cancelamento precisar tentar interromper a tarefa,
use `cancel(true)` no `Future` devolvido pelo método `submit` escolhido; ambos
permitem essa tentativa. `CompletableFuture.cancel(true)` não solicita
interrupção da execução.

Para observar cada ciclo de uma tarefa periódica em threads de plataforma,
coloque o observador depois de `task.run()` no próprio `Runnable` agendado. O
exemplo observa dois ciclos bem-sucedidos; a falha no terceiro encerra a
série e pode ser lida pelo `ScheduledFuture`:

```java
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ObservedPeriodicTasks {
    public static void main(final String[] args) throws InterruptedException {
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            final AtomicInteger cycles = new AtomicInteger();
            final AtomicInteger observations = new AtomicInteger();
            final Runnable task = () -> {
                if (cycles.incrementAndGet() == 3) {
                    throw new IllegalStateException("falha da tarefa");
                }
            };
            final Runnable onCycleSuccess = () -> {
                observations.incrementAndGet();
                System.out.println("Ciclo concluído: " + cycles.get());
            };
            final ScheduledFuture<?> series = scheduler.scheduleAtFixedRate(() -> {
                task.run();
                onCycleSuccess.run();
            }, 0, 10, TimeUnit.MILLISECONDS);

            try {
                series.get();
            } catch (final ExecutionException exception) {
                System.out.println(exception.getCause());
            }

            System.out.println("Ciclos observados: " + observations.get());

            final ScheduledFuture<?> pending = scheduler.scheduleAtFixedRate(
                    () -> System.out.println("Outra série"), 1, 1, TimeUnit.HOURS);
            System.out.println("Cancelamento aceito: " + pending.cancel(false));
        }
    }
}
```

O `ScheduledFuture` representa a série, não cada ciclo: seu `get()` não retorna
normalmente enquanto a série permanecer ativa. Se a tarefa ou o observador
lançar uma exceção, os próximos ciclos são suprimidos e `get()` expõe a falha.
O observador deste exemplo recebe somente ciclos bem-sucedidos; se for preciso
observar falhas de cada ciclo e continuar a série, a política de tratamento
deve ser definida pelo consumidor. O tempo gasto no observador integra a
duração do ciclo e afeta os próximos disparos. `cancel(false)` impede novos
disparos após aceito, sem interromper um ciclo já em execução; `cancel(true)`
apenas tenta interrompê-lo. O `try` fecha o agendador criado pelo exemplo e
aguarda seu término. Uma fábrica de threads virtuais fornecida a um executor
agendado não garante uma thread nova por ciclo.

### 5.5. Agendamento e prazo em threads de plataforma com o JDK

[Voltar ao sumário](#sumário)

Use um `ScheduledExecutorService` escolhido pelo consumidor para agendar
tarefas em threads de plataforma. Cada chamada devolve um `ScheduledFuture`:
numa tarefa isolada, `get()` aguarda a conclusão e devolve o valor do
`Callable` ou `null` para `Runnable`; se ela falhar, lança
`ExecutionException` com a falha em `getCause()`. O exemplo recebe o
agendador do chamador e não o fecha. `main` fornece uma `ThreadFactory` e
fecha o agendador que criou:

```java
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class PlatformScheduledTasks {
    private static void runWith(final ScheduledExecutorService scheduler) throws Exception {
        final ScheduledFuture<?> action = scheduler.schedule(
                () -> System.out.println("Runnable agendado"), 10, TimeUnit.MILLISECONDS);
        final ScheduledFuture<Integer> result = scheduler.schedule(
                () -> 42, 10, TimeUnit.MILLISECONDS);
        final ScheduledFuture<Integer> failure = scheduler.schedule(() -> {
            throw new IllegalStateException("falha da tarefa");
        }, 10, TimeUnit.MILLISECONDS);
        final ScheduledFuture<?> pending = scheduler.schedule(
                () -> System.out.println("Não executa"), 1, TimeUnit.HOURS);

        System.out.println("Cancelamento aceito: " + pending.cancel(false));
        action.get();
        System.out.println("Resultado: " + result.get());

        try {
            failure.get();
        } catch (final ExecutionException exception) {
            System.out.println(exception.getCause());
        }

        try {
            pending.get();
        } catch (final CancellationException exception) {
            System.out.println("Tarefa cancelada");
        }
    }

    public static void main(final String[] args) throws Exception {
        final ThreadFactory factory = Thread.ofPlatform()
                .name("scheduled-platform-", 0)
                .factory();

        try (ScheduledExecutorService scheduler =
                     Executors.newSingleThreadScheduledExecutor(factory)) {
            runWith(scheduler);
        }
    }
}
```

Para repetição, `scheduleAtFixedRate` mede o período entre inícios planejados;
se um ciclo atrasar, o seguinte pode começar logo após ele terminar, sem
sobreposição na mesma série. `scheduleWithFixedDelay` espera o atraso após a
conclusão de cada ciclo. Ambos aceitam `Runnable`, não `Callable`. O
`ScheduledFuture` representa a série: `get()` não retorna normalmente enquanto
ela estiver ativa. Cancele a série ao terminar de usá-la. Uma falha no corpo
ou no observador composto suprime os próximos ciclos e aparece em `get()`;
o [exemplo de observação](#54-observação-de-tarefas-com-o-jdk) mostra esse caso.

```java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlatformPeriodicTasks {
    private static void awaitTwoCycles(final CountDownLatch cycles)
            throws InterruptedException {
        if (!cycles.await(1, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Dois ciclos não terminaram");
        }
    }

    private static void runWith(final ScheduledExecutorService scheduler)
            throws InterruptedException {
        final CountDownLatch rateCycles = new CountDownLatch(2);
        final ScheduledFuture<?> fixedRate = scheduler.scheduleAtFixedRate(
                () -> {
                    System.out.println("Ciclo em taxa fixa");
                    rateCycles.countDown();
                }, 0, 10, TimeUnit.MILLISECONDS);

        try {
            awaitTwoCycles(rateCycles);
        } finally {
            fixedRate.cancel(false);
        }

        final CountDownLatch delayCycles = new CountDownLatch(2);
        final ScheduledFuture<?> fixedDelay = scheduler.scheduleWithFixedDelay(
                () -> {
                    System.out.println("Ciclo com atraso fixo");
                    delayCycles.countDown();
                }, 0, 10, TimeUnit.MILLISECONDS);

        try {
            awaitTwoCycles(delayCycles);
        } finally {
            fixedDelay.cancel(false);
        }
    }

    public static void main(final String[] args) throws InterruptedException {
        try (ScheduledExecutorService scheduler =
                     Executors.newSingleThreadScheduledExecutor()) {
            runWith(scheduler);
        }
    }
}
```

| Configuração disponível no builder | Composição opcional em plataforma no JDK 25 |
| --- | --- |
| `setDelay(d)` | `scheduler.schedule(Runnable/Callable, d, unit)` |
| `setInterval(p)` | `scheduler.scheduleAtFixedRate(Runnable, 0, p, unit)` |
| `setDelay(d)` e `setInterval(p)` | `scheduler.scheduleAtFixedRate(Runnable, d, p, unit)` |
| `setTimeout(t)` | `executor.submit(Runnable/Callable)` e um temporizador independente que cancela o `Future` após `t` |
| `setDelay(d)` e `setTimeout(t)` | `scheduler.schedule(Runnable/Callable, d, unit)` e um temporizador independente que cancela o `ScheduledFuture` |
| `setInterval(p)` e `setTimeout(t)` | `scheduler.scheduleAtFixedRate(Runnable, 0, p, unit)` e um temporizador independente que cancela a série |
| `setDelay(d)`, `setInterval(p)` e `setTimeout(t)` | `scheduler.scheduleAtFixedRate(Runnable, d, p, unit)` e um temporizador independente que cancela a série |
| Atraso após cada conclusão | `scheduler.scheduleWithFixedDelay(Runnable, d, p, unit)` |

O `setTimeout()` existente agenda um único cancelamento após a submissão:
seu prazo inclui o atraso inicial e não reinicia a cada ciclo. A receita a
seguir agenda o alarme imediatamente após obter cada `Future`, preservando
esse marco prático. O temporizador tem worker independente do agendador de
trabalho; com apenas um worker compartilhado, uma tarefa bloqueada pode
impedir que seu próprio alarme execute. `runWith` recebe os dois agendadores
do chamador; `main` cria e fecha ambos.

```java
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlatformDeadlineTasks {
    private static void waitUntilInterrupted() {
        try {
            new CountDownLatch(1).await();
        } catch (final InterruptedException exception) {
            Thread.currentThread()
                    .interrupt();
        }
    }

    private static void awaitWithDeadline(final Future<?> task,
                                          final ScheduledExecutorService timer)
            throws InterruptedException, ExecutionException {
        final ScheduledFuture<?> alarm;

        try {
            alarm = timer.schedule(() -> task.cancel(true), 100, TimeUnit.MILLISECONDS);
        } catch (final RuntimeException exception) {
            task.cancel(true);

            throw exception;
        }

        try {
            task.get();
        } catch (final CancellationException exception) {
            System.out.println("Cancelamento aceito: " + task.isCancelled());
        } catch (final InterruptedException exception) {
            task.cancel(true);

            throw exception;
        } finally {
            alarm.cancel(false);
        }
    }

    private static void runWith(final ScheduledExecutorService work,
                                final ScheduledExecutorService timer)
            throws InterruptedException, ExecutionException {
        final Future<?> isolated = work.submit(PlatformDeadlineTasks::waitUntilInterrupted);
        awaitWithDeadline(isolated, timer);

        final ScheduledFuture<Integer> delayed = work.schedule(
                () -> 42, 1, TimeUnit.HOURS);
        awaitWithDeadline(delayed, timer);

        final ScheduledFuture<?> series = work.scheduleAtFixedRate(
                PlatformDeadlineTasks::waitUntilInterrupted, 10, 10, TimeUnit.MILLISECONDS);
        awaitWithDeadline(series, timer);
    }

    public static void main(final String[] args) throws Exception {
        try (ScheduledExecutorService work = Executors.newSingleThreadScheduledExecutor();
             ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor()) {
            runWith(work, timer);
        }
    }
}
```

O exemplo cancela o alarme pendente no `finally` e cancela a tarefa se o
temporizador rejeitar o alarme. Para uma tarefa que termina antes do prazo,
`get()` devolve seu valor ou sua falha; o alarme é dispensado. Uma série ativa
só libera `get()` após cancelamento ou falha. `cancel(false)` impede novos
disparos sem interromper o ciclo em andamento; `cancel(true)` tenta também
interrompê-lo. O cancelamento aceito muda o estado do `Future`, mas não
confirma que o corpo da tarefa parou ou que um recurso foi fechado. Uma falha
posterior ao cancelamento exige outro canal de observação se ela for relevante.

`Future.get(timeout, unit)` limita apenas a espera do chamador: não cancela
a tarefa nem a série. `CompletableFuture.orTimeout()` conclui o estágio com
falha por prazo, sem interromper o trabalho subjacente. Se o consumidor escolher
um `ForkJoinPool`, `submitWithTimeout(...)` pode oferecer cancelamento
automático para uma tarefa isolada, mas não combina por si só atraso ou série
com um agendador arbitrário. Temporizadores do JDK são de melhor esforço;
nenhum exemplo promete precisão de tempo real. Quem cria os executores os
fecha; quem recebe executores fornecidos por terceiros não os fecha. Antes do
fechamento, cancele a série e o alarme ainda ativos. `close()` aguarda tarefas
em execução e pode demorar se elas não responderem à interrupção.

O exemplo seguinte reúne resultado de `Callable`, observação de cada ciclo,
nome e prioridade, atraso, taxa fixa e timeout em uma aplicação de plataforma.
Ela usa dois agendadores para que um ciclo bloqueado
não impeça o alarme de cancelar a série. O observador de sucesso atua após
cada ciclo; uma falha da tarefa ou do observador encerra a série e chega ao
`ScheduledFuture`. O alarme conta desde a submissão e `cancel(true)` tenta
interromper o ciclo em andamento. Ambos os executores são fechados pelo
chamador.

```java
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CombinedPlatformTasks {
    public static void main(final String[] args) throws Exception {
        final AtomicInteger names = new AtomicInteger();
        final ThreadFactory factory = task -> Thread.ofPlatform()
                .name("worker-" + names.incrementAndGet())
                .priority(4)
                .unstarted(task);

        try (ScheduledExecutorService work = Executors.newScheduledThreadPool(2, factory);
             ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor()) {
            final AtomicInteger cycles = new AtomicInteger();
            final Runnable observedCycle = () -> {
                try {
                    final int cycle = cycles.incrementAndGet();
                    System.out.println("ciclo " + cycle);
                    System.out.println("ciclo concluído");
                } catch (final RuntimeException | Error failure) {
                    System.err.println("ciclo falhou: " + failure);
                    throw failure;
                }
            };
            final ScheduledFuture<?> series = work.scheduleAtFixedRate(
                    observedCycle, 20, 20, TimeUnit.MILLISECONDS);
            final ScheduledFuture<?> alarm = timer.schedule(
                    () -> series.cancel(true), 120, TimeUnit.MILLISECONDS);

            try {
                final Future<Integer> value = work.submit(() -> 42);
                final Consumer<Integer> resultConsumer = result ->
                        System.out.println("resultado: " + result);
                resultConsumer.accept(value.get());
                series.get();
            } catch (final CancellationException cancellation) {
                System.out.println("série cancelada pelo prazo");
            } catch (final ExecutionException failure) {
                System.err.println("tarefa ou observador falhou: " + failure.getCause());
            } finally {
                alarm.cancel(false);
                series.cancel(true);
            }
        }
    }
}
```

Para a série periódica, apenas `Runnable` é aceito por `scheduleAtFixedRate` e
`scheduleWithFixedDelay`. O resultado do `Callable` acima pertence à tarefa
isolada; o `Future` da série representa cancelamento ou falha da série inteira,
não um resultado por ciclo. A fábrica pode nomear e definir prioridade porque
as threads de trabalho são de plataforma. Se a tarefa ignorar interrupção,
`cancel(true)` poderá marcar o `Future` como cancelado antes do corpo parar.

## 6. Migração das seis classes removidas

[Voltar ao sumário](#sumário)

A versão de trabalho `3.0.0-SNAPSHOT` remove seis classes públicas
marcadas `@Deprecated(since="2.0.0", forRemoval=true)` no pacote
`br.com.armange.commons.thread` da versão estável `2.0.0`.
Consumidores dessas classes precisam atualizar o código fonte e recompilar:
os binários anteriores não são compatíveis com essa remoção. A versão 2.0.0
publicada permanece disponível e inalterada. A versão 3 ainda não foi
publicada. As facilidades dos builders, `TryAsyncBuilder` e os tipos homônimos
em outros pacotes continuam disponíveis.

Há também uma mudança de assinatura nos builders sem agendamento entre 2.0.0
e esta versão de trabalho: `ThreadBuilder.setExecution(Runnable/Callable)`
deixa de retornar `CommonRunnableBuilder`/`CommonCallableBuilder`
e passa a retornar o estágio `SingleThreadBuilder` ou
`SingleCallableBuilder`. Seu `start()` padrão retorna diretamente
`Future<S>`: substitua `getFutures().get(0)` pelo `Future` devolvido.
Após `startAndBuildOther()`, use `MultipleThreadBuilder` ou
`MultipleCallableBuilder`; o `start()` padrão desse estágio retorna
`List<Future<S>>` imutável. Para receber também o executor, chame
`keepExecutorOpen()` antes de `start()`: os estágios `OpenSingle` e
`OpenMultiple` retornam, respectivamente, `SingleThreadExecutorResult` com
`getFuture()` e `MultipleThreadExecutorResult` com `getFutures()`. Atualize
variáveis com tipos explícitos e recompile os consumidores, pois os descritores
binários dos métodos mudaram. O caminho `setScheduling(...)` conserva seus
tipos e `ExecutorResult`.

Os nomes dos estágios sem agendamento foram simplificados nesta versão de
trabalho: `SingleTaskThreadBuilder` virou `SingleThreadBuilder`,
`MultipleTaskThreadBuilder` virou `MultipleThreadBuilder`,
`OpenSingleTaskThreadBuilder` virou `OpenSingleThreadBuilder` e
`OpenMultipleTaskThreadBuilder` virou `OpenMultipleThreadBuilder`. As quatro variantes
`Callable` seguem a mesma regra: por exemplo,
`SingleCallableTaskThreadBuilder` virou `SingleCallableBuilder`. Atualize
declarações e imports explícitos que usavam os nomes anteriores.

Nos estágios sem agendamento, o `start()` final desta versão solicita
`shutdown()` por padrão. Para continuar enviando tarefas ao executor após
receber o resultado, use `keepExecutorOpen()` antes de `start()` e feche o
executor recebido quando terminar. Essa escolha devolve outro tipo de estágio,
invalida os aliases padrão e persiste nos lotes posteriores. Após concluir um
lote `OpenMultiple`, o `OpenSingle` retido pode iniciar outro lote; se a opção
foi selecionada no Multiple, converta o Single padrão retido depois do lote.
Se houver falha antes da primeira entrega de executor, o builder solicita seu
encerramento; após a transferência da posse, o chamador continua responsável.
Esse ajuste de ciclo de vida também deve ser considerado na migração de
consumidores da versão 2.0.0.

| Classe removida do pacote raiz | Caminho atual na biblioteca | Alternativa direta do JDK |
| --- | --- | --- |
| `ThreadBuilder` | `builder.ThreadBuilder` mantém `setExecution(...)`, `setScheduling(...)`, atraso, intervalo, timeout e callbacks; veja [builders](#2-builders-de-plataforma). | `ExecutorService.submit(...)` ou `ScheduledExecutorService`, conforme a [seção 5](#5-composição-direta-com-o-jdk). |
| `ExecutorResult` | `builder.ExecutorResult` atende aos builders agendados. O `start()` padrão sem agendamento devolve `Future` ou `List<Future>` imutável; após `keepExecutorOpen()`, `SingleThreadExecutorResult` e `MultipleThreadExecutorResult` também expõem o executor. | Retenha os `Future`s; no fluxo padrão sem agendamento, o builder solicita `shutdown()`. No fluxo `Open` ou agendado, feche os executores recebidos. |
| `CaughtExecutorThreadFactory` | `core.ExceptionCatcherThreadFactory` configura nome fixo, prioridade e handler de threads de plataforma. | `Thread.ofPlatform()` com `ThreadFactory` própria quando precisar de nome por criação. |
| `ScheduledCaughtExecutorService` | `core.ScheduledThreadBuilderExecutor` mantém `addAfterExecuteConsumer(...)` e aceita `ThreadFactory` fornecida. | `ScheduledExecutorService` ou `ScheduledThreadPoolExecutor` com observação explícita. |
| `ThreadUtil` | `util.ThreadUtil` mantém os métodos de sono e acesso a recursos. | `Thread.sleep(...)` e métodos do classloader de contexto. |
| `UncheckedException` | `exception.UncheckedException` ainda aceita uma causa. | `new RuntimeException(cause)` ou exceção de domínio. |

O `ThreadBuilder` removido executava `Runnable`; o homônimo do pacote
`builder` também aceita `Callable`. Tanto o builder removido quanto o atual
usam taxa fixa em `setInterval()`. `scheduleWithFixedDelay(...)` do JDK é a
opção quando o próximo ciclo deve aguardar a conclusão do anterior.
`setTimeout()` no builder atual agenda um cancelamento único desde a submissão,
incluindo eventual atraso inicial e toda a série periódica, e expõe o executor
cancelador em `getTimeoutExecutorResults()`. `Future.get(timeout, unit)` somente
limita a espera; para compor cancelamento por prazo com JDK direto, use o
temporizador independente mostrado na [seção 5.5](#55-agendamento-e-prazo-em-threads-de-plataforma-com-o-jdk).
`cancel(true)` tenta interromper uma execução em andamento,
enquanto `cancel(false)` não a interrompe; nenhum deles confirma que o corpo
da tarefa já terminou. Não presuma uma thread virtual nova por ciclo periódico
ao trocar apenas a fábrica de um agendador.

Os três setters de observação e interrupção abaixo continuam no builder atual.
Ao optar pelo JDK direto, escolha como reproduzir cada comportamento:

- `setUncaughtExceptionConsumer(...)`: ao usar `submit(...)`, a falha da
  tarefa fica no `Future`, de modo que configurar um
  `UncaughtExceptionHandler` na fábrica não substitui esse consumidor. Observe
  a causa de `ExecutionException` em `get()` ou capture a falha dentro do
  próprio ciclo se precisar reagir durante ele.
- `setAfterExecuteConsumer(...)`: associe um observador ao resultado de cada
  tarefa, como na [seção 5.4](#54-observação-de-tarefas-com-o-jdk), ou componha o
  `Runnable` periódico para observar cada ciclo. O `ScheduledFuture`
  periódico informa a falha ou o cancelamento da série, mas não conclui após
  cada ciclo.
- `setSilentInterruption(...)`: trate `CancellationException` ao ler o
  `Future` separadamente de `InterruptedException` durante a espera ou no
  corpo da tarefa. Ao capturar `InterruptedException`, restaure o sinal com
  `Thread.currentThread().interrupt()` e decida quais eventos ignorar sem
  ocultar falhas da tarefa.

O antigo `ScheduledCaughtExecutorService` e o atual
`ScheduledThreadBuilderExecutor` usam callbacks globais, que podem receber
eventos de outras tarefas do mesmo executor. A observação por tarefa da
[seção 5.4](#54-observação-de-tarefas-com-o-jdk) tem abrangência diferente.
Em particular, o argumento `Throwable` de `afterExecute(...)`
normalmente é `null` para uma tarefa submetida por `submit(...)`; consulte o
`Future` para obter a falha capturada.

Para configurar nome, prioridade e handler de threads de plataforma, use
`ExceptionCatcherThreadFactory` ou forneça outra `ThreadFactory` ao executor.
O exemplo opcional com JDK
consulta o fornecedor de nomes a cada nova thread, sem supor uma consulta por
tarefa. O handler configurado pode receber falhas não capturadas pela thread;
falhas de tarefas enviadas por `submit(...)` continuam no `Future`.

A fábrica removida expunha setters mutáveis. Quando usada pelo builder antigo,
o fornecedor de nomes era consultado ao configurar a fábrica, e o nome
resultante era reaproveitado nas threads que ela criava. A composição abaixo
consulta o fornecedor em cada criação; consumidores que dependam de outra
política podem implementá-la na fábrica fornecida.

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PlatformFactoryMigration {
    public static void main(final String[] args) throws Exception {
        final ThreadFactory base = Thread.ofPlatform()
                .priority(Thread.NORM_PRIORITY)
                .uncaughtExceptionHandler((thread, failure) ->
                        System.err.println(thread.getName() + ": " + failure))
                .factory();
        final AtomicInteger nextName = new AtomicInteger();
        final Supplier<String> names = () -> "worker-" + nextName.incrementAndGet();
        final ThreadFactory named = task -> {
            final Thread thread = base.newThread(task);
            thread.setName(names.get());

            return thread;
        };

        try (ExecutorService executor = Executors.newFixedThreadPool(2, named)) {
            final Future<String> result = executor.submit(() ->
                    Thread.currentThread()
                            .getName());

            System.out.println(result.get());
        }
    }
}
```

`util.ThreadUtil.sleepUnchecked(...)` continua disponível. Como alternativa,
chame `Thread.sleep(...)`
e trate `InterruptedException` conforme a tarefa; o exemplo da seção 5.1
restaura o sinal quando a interrupção é capturada. Para recursos, use
`Thread.currentThread().getContextClassLoader().getResource(path)` ou
`getResourceAsStream(path)`; o resultado pode ser `null` e o consumidor deve
fechar o `InputStream` recebido. O tipo `util.ThreadUtil` ainda existe nesta
versão de trabalho, mas a chamada direta ao JDK dispensa esse atalho. O tipo
`exception.UncheckedException` também permanece; como alternativa, preserve a causa em
`new RuntimeException(cause)` ou numa exceção própria.

Os tipos homônimos `builder.ThreadBuilder`, `builder.ExecutorResult`,
`util.ThreadUtil` e `exception.UncheckedException` permanecem no projeto.
Eles não são os seis tipos removidos do pacote raiz. Os exemplos da
[seção 2](#2-builders-de-plataforma) mostram a API atual da biblioteca; o
[uso básico](#51-tarefas-simples-com-o-jdk) e o exemplo com
[executor fornecido](#51-tarefas-simples-com-o-jdk) mostram a alternativa
direta do JDK.

## 7. Consumo

[Voltar ao sumário](#sumário)

Versão de trabalho: `br.com.armange:commons-thread:3.0.0-SNAPSHOT`.
A compilação e a execução exigem JDK 25. Para integração local ou coordenada,
após disponibilizar esse artefato ao build consumidor:

```gradle
dependencies {
    implementation 'br.com.armange:commons-thread:3.0.0-SNAPSHOT'
}
```

A versão estável anterior `2.0.0`, disponível na Maven Central, foi
compilada para Java 11. A versão 3 eleva o requisito mínimo para Java 25 e
ainda não foi publicada. Os [relatórios históricos](../docs/README.md)
continuam separados dos resultados do build atual.

Para consumir a versão estável **2.0.0** pela
[Maven Central](https://search.maven.org/artifact/br.com.armange/commons-thread),
declare a dependência no Maven:

```xml
<dependency>
    <groupId>br.com.armange</groupId>
    <artifactId>commons-thread</artifactId>
    <version>2.0.0</version>
</dependency>
```

Ou no Gradle:

```gradle
dependencies {
    implementation 'br.com.armange:commons-thread:2.0.0'
}
```

Essas coordenadas estáveis descrevem a API publicada de 2.0.0. Os exemplos
que importam classes desta biblioteca usam a API da versão de trabalho
**3.0.0-SNAPSHOT**; as receitas diretas do JDK 25 não dependem do artefato.
Os relatórios preservados de 2.0.0 incluem
[Javadoc](../docs/commons-thread/2-0-0/javadoc/index.html),
[testes](../docs/commons-thread/2-0-0/test/index.html) e
[JaCoCo](../docs/commons-thread/2-0-0/jacoco/index.html).
O projeto é distribuído sob a [licença Apache 2.0](../LICENSE).
