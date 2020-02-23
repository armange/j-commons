# Commons-spi [:rewind:](https://github.com/armange/j-commons)

This release was created based on the need to simplify daily software development tasks. It contains a framework that simplifies [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) handling that may be routine for some types of software projects.

<a name="summary"></a>

##### The tasks simplified by this lib thread are:
- [Single service loagind](#single).
- [Multiple service loaging](#multiple).
- [Service's loading as stream.](#stream).
- [Exception handling](#excepton).

###### See [Javadoc](https://armange.github.io/j-commons/commons-spi/javadoc)
###### See [Tests report](https://armange.github.io/j-commons/commons-thread/test)

[![Build][buildbadge]](https://github.com/armange/j-commons/commits/development) 
[![Coverage][coveragebadge]](https://sonarcloud.io/dashboard?id=armange_j-commons-commons-spi) 
[![Quality][qualitybadge]](https://sonarcloud.io/dashboard?id=armange_j-commons-commons-spi) 
[![License][licensebadge]](https://github.com/armange/j-commons/blob/development/LICENSE)

[buildbadge]: https://img.shields.io/github/workflow/status/armange/j-commons/Java%20CI?style=for-the-badge "Build Status"
[coveragebadge]: https://img.shields.io/sonar/coverage/armange_j-commons-commons-spi?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge 
[qualitybadge]: https://img.shields.io/sonar/quality_gate/armange_j-commons-commons-spi?server=https%3A%2F%2Fsonarcloud.io&style=for-the-badge
[licensebadge]: https://img.shields.io/github/license/armange/j-commons?style=for-the-badge

##### Available package repository

 - [Github](https://github.com/armange/j-commons/packages/135453?version=1.0.0)
     - [Configuring Apache Maven for use with GitHub Packages](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages)
     - [Configuring Gradle for use with GitHub Packages](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-gradle-for-use-with-github-packages)

##### Maven import

```xml
<dependency>
    <groupId>br.com.armange</groupId>
    <artifactId>commons-spi</artifactId>
    <version>1.0.0</version>
</dependency>
```

##### Gradle import

```
compile group: 'br.com.armange', name: 'commons-spi', version: '1.0.0'
```

### Samples

<a name="single"></a>

#### Single service [:arrow_double_up:](#summary)
```java
final Service loadService = Loader.loadService(Service.class);
```

<a name="multiple"></a>

#### Multiple services [:arrow_double_up:](#summary)
```java
final List<MultipleServices> services = Loader.loadServices(MultipleServices.class);
```

<a name="stream"></a>

#### Services as stream [:arrow_double_up:](#summary)
```java
final Stream<MultipleServices> serviceStream = Loader.loadServices(MultipleServices.class, false);
```

<a name="exception"></a>

#### Services as stream [:arrow_double_up:](#summary)
```java
try {
    final List<Service> services = Loader.loadServices(Service.class);
} catch (final NoImplementationFoundException e) {
    // TODO: handle exception
}
```