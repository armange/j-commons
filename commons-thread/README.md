# commons-thread

Constrói e executa tarefas em threads, com opções para agendamento, timeout,
tratamento de erros e resultado.

[Voltar ao README principal](../README.md)

## Sumário

- [1. Tarefas simples com o JDK](#1-tarefas-simples-com-o-jdk)
- [2. Tratamento assíncrono de falhas](#2-tratamento-assíncrono-de-falhas)
- [3. Recursos assíncronos](#3-recursos-assíncronos)
- [4. Observação de tarefas](#4-observação-de-tarefas)
- [5. Agendamento e prazo em threads de plataforma](#5-agendamento-e-prazo-em-threads-de-plataforma)
- [6. Migração das classes removidas](#6-migração-das-classes-removidas)
- [7. Builders existentes](#7-builders-existentes)
- [8. Consumo](#8-consumo)

## 1. Tarefas simples com o JDK

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

## 2. Tratamento assíncrono de falhas

[Voltar ao sumário](#sumário)

Para substituir `TryAsyncBuilder` em tarefas novas, coloque `try/catch/finally`
dentro de um `Callable` e submeta-o a um `ExecutorService`. O exemplo recebe o
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

## 3. Recursos assíncronos

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

## 4. Observação de tarefas

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

## 5. Agendamento e prazo em threads de plataforma

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
o [exemplo de observação](#4-observação-de-tarefas) mostra esse caso.

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

| Configuração anterior | Uso em threads de plataforma no JDK 25 |
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

## 6. Migração das classes removidas

[Voltar ao sumário](#sumário)

A versão de trabalho `3.0.0-SNAPSHOT` remove seis classes públicas do pacote
`br.com.armange.commons.thread` presentes na versão estável `2.0.0`.
Consumidores dessas classes precisam atualizar o código fonte e recompilar:
os binários anteriores não são compatíveis com essa remoção. A versão 2.0.0
publicada permanece disponível e inalterada. A versão 3 ainda não foi
publicada.

| Classe removida de `br.com.armange.commons.thread` | Caminho de atualização |
| --- | --- |
| `ThreadBuilder` | Use `ExecutorService.submit(Runnable)` e retenha seu `Future` para tarefa simples; para atraso ou repetição em plataforma, use `ScheduledExecutorService` e `ScheduledFuture`, como na [seção 5](#5-agendamento-e-prazo-em-threads-de-plataforma). |
| `ExecutorResult` | Retenha diretamente o executor e cada `Future`; feche o executor que criou. |
| `CaughtExecutorThreadFactory` | Configure uma `Thread.Builder.OfPlatform` e obtenha sua `ThreadFactory`; componha uma fábrica própria quando o nome precisar ser calculado a cada criação. |
| `ScheduledCaughtExecutorService` | Use `ScheduledExecutorService` ou `ScheduledThreadPoolExecutor` do JDK e observe cada `Future` ou cada ciclo por um `Runnable` composto. |
| `ThreadUtil` | Use `Thread.sleep(...)` e os métodos de recurso do classloader de contexto. |
| `UncheckedException` | Use `new RuntimeException(cause)` ou uma exceção de domínio que preserve a causa. |

O `ThreadBuilder` removido executava `Runnable`. Seu `setInterval()` usava
taxa fixa; `scheduleWithFixedDelay(...)` é uma escolha nova quando o próximo
ciclo deve aguardar a conclusão do anterior. `setTimeout()` agendava um
cancelamento único desde a submissão, incluindo eventual atraso inicial e
toda a série periódica. `Future.get(timeout, unit)` somente limita a espera;
para manter o cancelamento por prazo, use o temporizador independente mostrado
na seção 5. `cancel(true)` tenta interromper uma execução em andamento,
enquanto `cancel(false)` não a interrompe; nenhum deles confirma que o corpo
da tarefa já terminou. Não presuma uma thread virtual nova por ciclo periódico
ao trocar apenas a fábrica de um agendador.

Os três setters de observação e interrupção do builder antigo exigem uma
escolha explícita ao migrar:

- `setUncaughtExceptionConsumer(...)`: ao usar `submit(...)`, a falha da
  tarefa fica no `Future`, de modo que configurar um
  `UncaughtExceptionHandler` na fábrica não substitui esse consumidor. Observe
  a causa de `ExecutionException` em `get()` ou capture a falha dentro do
  próprio ciclo se precisar reagir durante ele.
- `setAfterExecuteConsumer(...)`: associe um observador ao resultado de cada
  tarefa, como na [seção 4](#4-observação-de-tarefas), ou componha o
  `Runnable` periódico para observar cada ciclo. O `ScheduledFuture`
  periódico informa a falha ou o cancelamento da série, mas não conclui após
  cada ciclo.
- `setSilentInterruption(...)`: trate `CancellationException` ao ler o
  `Future` separadamente de `InterruptedException` durante a espera ou no
  corpo da tarefa. Ao capturar `InterruptedException`, restaure o sinal com
  `Thread.currentThread().interrupt()` e decida quais eventos ignorar sem
  ocultar falhas da tarefa.

O antigo `ScheduledCaughtExecutorService` mantinha uma lista mutável de
callbacks globais: eles podiam receber eventos de outras tarefas do mesmo
executor. Essa abrangência não existe na observação por tarefa acima. Se um
consumidor precisar dela, deve implementar a observação no executor que
controla. Em particular, o argumento `Throwable` de `afterExecute(...)`
normalmente é `null` para uma tarefa submetida por `submit(...)`; consulte o
`Future` para obter a falha capturada.

Para configurar nome, prioridade e handler de threads de plataforma, o
consumidor pode fornecer sua própria `ThreadFactory` ao executor. Este exemplo
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

Para substituir `ThreadUtil.sleepUnchecked(...)`, chame `Thread.sleep(...)`
e trate `InterruptedException` conforme a tarefa; o exemplo da seção 1
restaura o sinal quando a interrupção é capturada. Para recursos, use
`Thread.currentThread().getContextClassLoader().getResource(path)` ou
`getResourceAsStream(path)`; o resultado pode ser `null` e o consumidor deve
fechar o `InputStream` recebido. O tipo `util.ThreadUtil` ainda existe nesta
versão de trabalho, mas a chamada direta ao JDK dispensa esse atalho. Para substituir
o construtor de `UncheckedException`, preserve a causa em
`new RuntimeException(cause)` ou numa exceção própria.

Os tipos homônimos `builder.ThreadBuilder`, `builder.ExecutorResult`,
`util.ThreadUtil` e `exception.UncheckedException` permanecem no projeto.
Eles não são os seis tipos removidos do pacote raiz nem substitutos
obrigatórios. O [uso básico](#1-tarefas-simples-com-o-jdk) e o exemplo com
[executor fornecido](#1-tarefas-simples-com-o-jdk) mostram as escolhas diretas
do JDK para novos consumidores.

## 7. Builders existentes

[Voltar ao sumário](#sumário)

`br.com.armange.commons.thread.builder.ThreadBuilder.setExecution(...)`
continua disponível para `Runnable` e `Callable`. Seus builders comuns usam
`ScheduledThreadBuilderExecutor`, mesmo para tarefas sem agendamento; o tipo
`ExecutorResult` fornece os `Future`s e o `ExecutorService` resultantes.
`setScheduling(...)`, timeout e as demais facilidades dos builders continuam
disponíveis. Para novos usos em plataforma, as seções acima mostram os
equivalentes diretos do JDK para tarefas simples e agendadas. `setInterval()`
usa taxa fixa, embora sua descrição antiga possa sugerir atraso após cada
conclusão. `TimingCallableThreadBuilder` aceita configurar um intervalo, mas
`start()` lança `UnsupportedOperationException` nessa combinação.

`TryAsyncBuilder`, `addCatcher()` e `finallyAsync()` também permanecem
disponíveis nesta versão. Seu `execute()` retorna `void` e cria um executor
internamente; não entrega ao chamador um `Future` para aguardar, cancelar ou
observar falhas da tarefa. O consumidor de resultado é chamado antes dos
finalizadores, depois da conclusão do `Future` interno. Exceções lançadas por
esses callbacks ficam somente no log; `Error` não é capturado pelo callback
legado. Para novos fluxos assíncronos de tratamento de exceções,
use o caminho com `Callable` acima. As variantes que recebem recursos continuam
disponíveis e esperam recursos já abertos. Nas variantes de array e mapa, o
fechamento segue a ordem de iteração e para na primeira `IOException`, sem
preservar as demais falhas como exceções suprimidas. Para novos usos com
recursos, siga o caminho da seção anterior.

As seis classes do pacote raiz descritas na seção de migração não estão
disponíveis nesta versão de trabalho. Os builders do pacote `builder`
permanecem disponíveis.

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

## 8. Consumo

[Voltar ao sumário](#sumário)

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
