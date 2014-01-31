# Gradle jsonschema2pojo plugin

[jsonschema2pojo](http://www.jsonschema2pojo.org) generates a Java representation of your
json schema. The [schema reference](https://code.google.com/p/jsonschema2pojo/wiki/Reference)
describes the rules and their effect on generated Java types.

## Usage

This plugin is hosted on the Maven Central Repository. All actions are logged at the `info` level.

```groovy
apply plugin: 'jsonschema2pojo'

buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    // this plugin
    classpath 'org.jsonschema2pojo:jsonschema2pojo-gradle-plugin:${js2p.version}'
    // add additional dependencies here if you wish to reference instead of generate them (see example directory)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  // Required if generating equals, hashCode, or toString methods
  compile 'commons-lang:commons-lang:2.6'
  // Required if generating JSR-303 annotations
  compile 'javax.validation:validation-api:1.1.0.CR2'
  // Required if generating Jackson 2 annotations
  compile 'com.fasterxml.jackson.core:jackson-databind:2.1.4'
  // Required if generating JodaTime data types
  compile 'joda-time:joda-time:2.2'
}

// Each configuration is set to the default value
jsonSchema2Pojo {
  // Whether to generate builder-style methods of the form withXxx(value) (that return this),
  // alongside the standard, void-return setters.
  generateBuilders = false

  // Whether to use primitives (long, double, boolean) instead of wrapper types where possible
  // when generating bean properties (has the side-effect of making those properties non-null).
  usePrimitives = false

  // Location of the JSON Schema file(s). This may refer to a single file or a directory of files.
  source = files("${sourceSets.main.output.resourcesDir}/json")

  // Target directory for generated Java source files. The plugin will add this directory to the
  // java source set so the compiler will find and compile the newly generated source files.
  targetDirectory = file("${project.buildDir}/generated-sources/js2p")

  // Package name used for generated Java classes (for types where a fully qualified name has not
  // been supplied in the schema using the 'javaType' property).
  targetPackage = ''

  // The characters that should be considered as word delimiters when creating Java Bean property
  // names from JSON property names. If blank or not set, JSON properties will be considered to
  // contain a single word when creating Java Bean property names.
  propertyWordDelimiters = [] as char[]

  // Whether to use the java type long (or Long) instead of int (or Integer) when representing the
  // JSON Schema type 'integer'.
  useLongIntegers = false

  // Whether to use the java type double (or Double) instead of float (or Float) when representing
  // the JSON Schema type 'number'.
  useDoubleNumbers = false

  // Whether to include hashCode and equals methods in generated Java types.
  includeHashcodeAndEquals = true

  // Whether to include a toString method in generated Java types.
  includeToString = true

  // The style of annotations to use in the generated Java types. Supported values:
  //  - jackson (alias of jackson2)
  //  - jackson2 (apply annotations from the Jackson 2.x library)
  //  - jackson1 (apply annotations from the Jackson 1.x library)
  //  - gson (apply annotations from the Gson library)
  //  - none (apply no annotations at all)
  annotationStyle = 'jackson'

  // A fully qualified class name, referring to a custom annotator class that implements
  // com.googlecode.jsonschema2pojo.Annotator and will be used in addition to the one chosen
  // by annotationStyle. If you want to use the custom annotator alone, set annotationStyle to none.
  customAnnotator = 'com.googlecode.jsonschema2pojo.NoopAnnotator'

  // Whether to include JSR-303 annotations (for schema rules like minimum, maximum, etc) in
  // generated Java types. Schema rules and the annotation they produce:
  //  - maximum = @DecimalMax
  //  - minimum = @DecimalMin
  //  - minItems,maxItems = @Size
  //  - minLength,maxLength = @Size
  //  - pattern = @Pattern
  //  - required = @NotNull
  // Any Java fields which are an object or array of objects will be annotated with @Valid to
  // support validation of an entire document tree.
  includeJsr303Annotations = false

  // The type of input documents that will be read. Supported values:
  //  - jsonschema (schema documents, containing formal rules that describe the structure of json data)
  //  - json (documents that represent an example of the kind of json data that the generated Java types
  //          will be mapped to)
  sourceType = 'jsonschema'

  // Whether to empty the target directory before generation occurs, to clear out all source files
  // that have been generated previously. <strong>Be warned</strong>, when activated this option
  // will cause jsonschema2pojo to <strong>indiscriminately delete the entire contents of the target
  // directory (all files and folders)</strong> before it begins generating sources.
  boolean removeOldOutput = false

  // The character encoding that should be used when writing the generated Java source files
  String outputEncoding = 'UTF-8'

  // Whether to use {@link org.joda.time.DateTime} instead of {@link java.util.Date} when adding
  // date type fields to generated Java types.
  boolean useJodaDates = false
}
```

### Working with pre-existing java classes

jsonschema2pojo allows to reference any pre-existing java classes. In general, if the generator finds a
class already exists on the classpath, then it will not be generated but only referenced. To make this
work as expected with this gradle plugin, the dependencies in question must be added to the buildscript
classpath, the project classpath alone will not suffice. For a little example of how to do this have
a look at the `example` directory.

## Tasks

### `generateJsonSchema2Pojo`

This task will automatically run in a project where the `jsonSchema2Pojo` configuration closure is present.
It will invoke the jsonschema2pojo generator, make the compileJava task dependent of itself and add
the `targetDirectory` to the main/java source set so the java compiler will find and compile the newly
generated source files.
