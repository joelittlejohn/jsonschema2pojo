# jsonschema2pojo [![Build Status](https://travis-ci.org/joelittlejohn/jsonschema2pojo.png)](https://travis-ci.org/joelittlejohn/jsonschema2pojo)

_jsonschema2pojo_ generates Java types from JSON Schema (or example JSON) and can fully annotate those types for data-binding with Jackson 1.x or 2.x.

If you want to play with some of the features of this project you can [try jsonschema2pojo online](http://jsonschema2pojo.org/).

Maven plugin example:
```xml
<plugin>
    <groupId>com.googlecode.jsonschema2pojo</groupId>
    <artifactId>jsonschema2pojo-maven-plugin</artifactId>
    <version>0.3.7</version>
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
  * **[Getting Started](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started)**
  * [Reference](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Reference)
  * [Latest Javadocs](http://wiki.jsonschema2pojo.googlecode.com/git/javadocs/0.3.7/index.html)
  * [Documentation for the Maven plugin](http://wiki.jsonschema2pojo.googlecode.com/git/site/0.3.7/plugin-info.html)
  * [Documentation for the Ant task](http://jsonschema2pojo.googlecode.com/git-history/jsonschema2pojo-0.3.7/jsonschema2pojo-ant/src/site/Jsonschema2PojoTask.html)

Project resources:
  * [Mailing list](https://groups.google.com/forum/#!forum/jsonschema2pojo-users)

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
