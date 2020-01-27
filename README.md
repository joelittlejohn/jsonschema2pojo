# jsonschema2pojo [![Build Status](https://travis-ci.org/joelittlejohn/jsonschema2pojo.svg?branch=master)](https://travis-ci.org/joelittlejohn/jsonschema2pojo) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jsonschema2pojo/jsonschema2pojo/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.jsonschema2pojo%22)

_jsonschema2pojo_ generates Java types from JSON Schema (or example JSON) and can annotate those types for data-binding with Jackson 1.x, Jackson 2.x or Gson.

**_*Note:*_ there are breaking changes between 0.5.1 and 1.0.0. Check the [change log](https://github.com/joelittlejohn/jsonschema2pojo/blob/master/CHANGELOG.md). Anything marked in bold in the 1.0.0 alpha, beta and final release is a breaking change.**

### [Try jsonschema2pojo online](http://jsonschema2pojo.org/)<br>or `brew install jsonschema2pojo`

You can use jsonschema2pojo as a Maven plugin, an Ant task, a command line utility, a Gradle plugin or embedded within your own Java app. The [Getting Started](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started) guide will show you how.

A very simple Maven example:
```xml
<plugin>
    <groupId>org.jsonschema2pojo</groupId>
    <artifactId>jsonschema2pojo-maven-plugin</artifactId>
    <version>1.0.2</version>
    <configuration>
        <sourceDirectory>${basedir}/src/main/resources/schema</sourceDirectory>
        <targetPackage>com.example.types</targetPackage>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Useful pages:
  * **[Getting started](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started)**
  * **[How to contribute](https://github.com/joelittlejohn/jsonschema2pojo/blob/master/CONTRIBUTING.md)**
  * [Reference](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Reference)
  * [Latest Javadocs](https://joelittlejohn.github.io/jsonschema2pojo/javadocs/1.0.2/)
  * [Documentation for the Maven plugin](https://joelittlejohn.github.io/jsonschema2pojo/site/1.0.2/generate-mojo.html)
  * [Documentation for the Ant task](https://joelittlejohn.github.io/jsonschema2pojo/site/1.0.2/Jsonschema2PojoTask.html)

Project resources:
  * [Downloads](https://github.com/joelittlejohn/jsonschema2pojo/releases)
  * [Mailing list](https://groups.google.com/forum/#!forum/jsonschema2pojo-users)

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

<a href="https://www.yourkit.com"><img src="https://www.yourkit.com/images/yklogo.png" alt="YourKit" title="YourKit" align="right" width="185"/></a>

Special thanks to YourKit, who support this project through a free license for their full-featured [YourKit Java Profiler](https://www.yourkit.com/java/profiler).
