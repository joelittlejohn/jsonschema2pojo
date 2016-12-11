# jsonschema2pojo [![Build Status](https://travis-ci.org/joelittlejohn/jsonschema2pojo.svg?branch=master)](https://travis-ci.org/joelittlejohn/jsonschema2pojo) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jsonschema2pojo/jsonschema2pojo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jsonschema2pojo/jsonschema2pojo)

_jsonschema2pojo_ generates Java types from JSON Schema (or example JSON) and can annotate those types for data-binding with Jackson 1.x, Jackson 2.x or Gson.

### [Try jsonschema2pojo online](http://jsonschema2pojo.org/)<br>or `brew install jsonschema2pojo`

You can use jsonschema2pojo as a Maven plugin, an Ant task, a command line utility, a Gradle plugin or embedded within your own Java app. The [Getting Started](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started) guide will show you how.

A very simple Maven example:
```xml
<plugin>
    <groupId>org.jsonschema2pojo</groupId>
    <artifactId>jsonschema2pojo-maven-plugin</artifactId>
    <version>0.4.28</version>
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
  * **[How to contribute](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Contribute)**
  * [Reference](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Reference)
  * [Latest Javadocs](https://joelittlejohn.github.io/jsonschema2pojo/javadocs/0.4.28/)
  * [Documentation for the Maven plugin](https://joelittlejohn.github.io/jsonschema2pojo/site/0.4.28/generate-mojo.html)
  * [Documentation for the Ant task](https://joelittlejohn.github.io/jsonschema2pojo/site/0.4.28/Jsonschema2PojoTask.html)

Project resources:
  * [Downloads](https://github.com/joelittlejohn/jsonschema2pojo/releases)
  * [Mailing list](https://groups.google.com/forum/#!forum/jsonschema2pojo-users)

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
