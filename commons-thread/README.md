# Commons-thread

This release was created based based on the need to simplify daily software development tasks. It contains a framework that simplifies thread building for some specific scenarios that may be routine for some types of software projects.

##### The tasks simplified by this lib thread are:
- Thread delayed.
- Thread timed out.
- Thread loop.
- Exception handling.
- The combination of these factors together.

###### See [Javadoc](https://armange.github.io/j-commons/commons-thread/)

### Samples

#### Delay
```java
    ThreadBuilder
        .newBuilder()
        .setDelay(2000)
        .setExecution(() -> System.out.println("An execution with delay"))
        .start();
```

#### Timeout
```java
    ThreadBuilder
        .newBuilder()
        .setTimeout(3000)
        .setExecution(() -> System.out.println("An execution with timeout"))
        .start();
```

#### Interval
```java
    ThreadBuilder
        .newBuilder()
        .setInterval(1000)
        .setExecution(() -> System.out.println("An execution with interval"))
        .start();
```

#### Uncaught exception
```java
    ThreadBuilder
        .newBuilder()
        .setUncaughtExceptionConsumer(throwable -> {throw new RuntimeException(throwable);})
        .setExecution(() -> System.out.println("An execution with uncaught exception"))
        .start();
```

#### Naming
```java
    ThreadBuilder
        .newBuilder()
        .setThreadNameSupplier(() -> "Thread name")
        .setExecution(() -> System.out.println("An execution with name"))
        .start();
```

#### Prioritization
```java
    ThreadBuilder
        .newBuilder()
        .setThreadPrioritySupplier(() -> 4)
        .setUncaughtExceptionConsumer(throwable -> {throw new RuntimeException(throwable);})
        .setExecution(() -> System.out.println("An execution with priority"))
        .start();
```

#### No schedule
```java
    ThreadBuilder
        .newBuilder()
        .setExecution(() -> System.out.println("An execution with priority"))
        .start();
```

#### Thread pool
```java
    ThreadBuilder
        .newBuilder(5)
        .setExecution(() -> System.out.println("Thread 1"))
        .startAndBuildOther()
        .setExecution(() -> System.out.println("Thread 2"))
        .startAndBuildOther()
        .setExecution(() -> System.out.println("Thread 3"))
        .startAndBuildOther()
        .setExecution(() -> System.out.println("Thread 4"))
        .startAndBuildOther()
        .setExecution(() -> System.out.println("Thread 5"))
        .start();
```
