# Commons-thread

This release was created based on the need to simplify daily software development tasks. It contains a framework that simplifies thread creation for some specific scenarios that may be routine for some types of software projects.

##### The tasks simplified by this lib thread are:
- Thread delayed.
- Thread timed out.
- Thread loop.
- Exception handling.
- The combination of these factors together.

###### See [Javadoc](https://armange.github.io/j-commons/commons-thread/)

[![Build][buildbadge]](https://github.com/armange/j-commons/commits/master) [![Nexus][nexusbadge]](https://search.maven.org/artifact/br.com.armange/commons-thread) [![Coverage][coveragebadge]](https://sonarcloud.io/dashboard?id=armange_j-commons) [![Quality][qualitybadge]](https://sonarcloud.io/dashboard?id=armange_j-commons) [![License][licensebadge]](https://github.com/armange/j-commons/blob/development/LICENSE)

[buildbadge]: https://img.shields.io/github/workflow/status/armange/j-commons/Java%20CI?style=for-the-badge "Build Status"
[nexusbadge]: https://img.shields.io/nexus/r/br.com.armange/commons-thread?server=https%3A%2F%2Foss.sonatype.org&style=for-the-badge 
[coveragebadge]: https://img.shields.io/sonar/coverage/armange_j-commons?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge 
[qualitybadge]: https://img.shields.io/sonar/quality_gate/armange_j-commons?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge
[licensebadge]: https://img.shields.io/github/license/armange/j-commons?style=for-the-badge

##### [Maven import](https://search.maven.org/artifact/br.com.armange/commons-thread)

```xml
<dependency>
    <groupId>br.com.armange</groupId>
    <artifactId>commons-thread</artifactId>
    <version>1.0.0</version>
</dependency>
```

##### [Gradle import](https://search.maven.org/artifact/br.com.armange/commons-thread)

```
compile group: 'br.com.armange', name: 'commons-thread', version: '1.0.0'
```

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

#### After execution consumer
```java
    ThreadBuilder
        .newBuilder()
        .setExecution(() -> System.out.println("An execution with am after-execution consumer"))
        .setAfterExecuteConsumer((runnable, throwable) -> System.out.println("The thread has already been finished"))
        .start()
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