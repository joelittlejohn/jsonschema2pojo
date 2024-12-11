# Gradle jsonschema2pojo plugin

[jsonschema2pojo](http://www.jsonschema2pojo.org) generates a Java representation of your
JSON schema. The [schema reference](https://github.com/joelittlejohn/jsonschema2pojo/wiki/Reference)
describes the rules and their effect on generated Java types.

This Gradle plugin is hosted on both Maven Central and the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.jsonschema2pojo).

## Usage

For Gradle 7.3 and later, you can use the plugins DSL:

With Groovy:

```groovy
plugins {
  id "java"
  id "org.jsonschema2pojo" version "1.2.2"
}

jsonSchema2Pojo {
  ...
}
```

With Kotlin:

```kotlin
plugins {
  id("java")
  id("org.jsonschema2pojo") version "1.2.2"
}

jsonSchema2Pojo {
  ...
}
```

For Gradle 7.2.x and older, there is a [bug related to the application order of plugins](https://github.com/gradle/gradle/issues/15664) so you must use [legacy plugin application](https://plugins.gradle.org/plugin/org.jsonschema2pojo).


Below we have a full build.gradle example, showing all available configuration options:

```groovy
plugins {
  id "java"
  id "org.jsonschema2pojo" version "1.2.2"
}

repositories {
  mavenCentral()
}

dependencies {
  // Required if generating JSR-303 annotations
  implementation 'javax.validation:validation-api:1.1.0.CR2'
  implementation 'jakarta.validation:jakarta.validation-api:3.0.0'
  // Required if generating Jackson 2 annotations
  implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
  // Required if generating JodaTime data types
  implementation 'joda-time:joda-time:2.2'
}

jsonSchema2Pojo {

  // Location of the JSON Schema file(s). This may refer to a single file or a directory of files.
  source = files("${sourceSets.main.output.resourcesDir}/json")

  // Target directory for generated Java source files. The plugin will add this directory to the
  // java source set so the compiler will find and compile the newly generated source files.
  targetDirectory = file("${project.buildDir}/generated-sources/js2p")

  // Package name used for generated Java classes (for types where a fully qualified name has not
  // been supplied in the schema using the 'javaType' property).
  targetPackage = ''

  // Whether to allow 'additional' properties to be supported in classes by adding a map to
  // hold these. This is true by default, meaning that the schema rule 'additionalProperties'
  // controls whether the map is added. Set this to false to globally disable additional properties.
  includeAdditionalProperties = false

  // Whether to include a javax.annotation.Generated (Java 8 and lower) or
  // javax.annotation.processing.Generated (Java 9+) in on generated types (default true).
  // See also: targetVersion.
  includeGeneratedAnnotation = true

  // Whether to generate builder-style methods of the form withXxx(value) (that return this),
  // alongside the standard, void-return setters.
  generateBuilders = false

  // If set to true, then the gang of four builder pattern will be used to generate builders on
  // generated classes. Note: This property works in collaboration with generateBuilders.
  // If generateBuilders is false then this property will not do anything.
  useInnerClassBuilders = false

  // Whether to use primitives (long, double, boolean) instead of wrapper types where possible
  // when generating bean properties (has the side-effect of making those properties non-null).
  usePrimitives = false

  // The characters that should be considered as word delimiters when creating Java Bean property
  // names from JSON property names. If blank or not set, JSON properties will be considered to
  // contain a single word when creating Java Bean property names.
  propertyWordDelimiters = [] as char[]

  // Whether to use the java type long (or Long) instead of int (or Integer) when representing the
  // JSON Schema type 'integer'.
  useLongIntegers = false

  // Whether to use the java type BigInteger when representing the JSON Schema type 'integer'. Note
  // that this configuration overrides useLongIntegers
  useBigIntegers = false

  // Whether to use the java type double (or Double) instead of float (or Float) when representing
  // the JSON Schema type 'number'.
  useDoubleNumbers = true

  // Whether to use the java type BigDecimal when representing the JSON Schema type 'number'. Note
  // that this configuration overrides useDoubleNumbers
  useBigDecimals = false

  // Whether to include hashCode and equals methods in generated Java types.
  includeHashcodeAndEquals = true

  // Whether to include a toString method in generated Java types.
  includeToString = true

  // The style of annotations to use in the generated Java types. Supported values:
  //  - jackson (alias of jackson2)
  //  - jackson2 (apply annotations from the Jackson 2.x library)
  //  - jsonb (apply annotations from the JSON-B 1 library)
  //  - jsonb2 (apply annotations from the JSON-B 2 library)
  //  - gson (apply annotations from the Gson library)
  //  - moshi1 (apply annotations from the Moshi 1.x library)
  //  - none (apply no annotations at all)
  annotationStyle = 'jackson'

  // A fully qualified class name, referring to a custom annotator class that implements
  // org.jsonschema2pojo.Annotator and will be used in addition to the one chosen
  // by annotationStyle. If you want to use the custom annotator alone, set annotationStyle to none.
  customAnnotator = 'org.jsonschema2pojo.NoopAnnotator'

  // Whether to include JSR-303/349 annotations (for schema rules like minimum, maximum, etc) in
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

  // Whether to include JSR-305 annotations, for schema rules like Nullable, NonNull, etc
  includeJsr305Annotations = false

  // The Level of inclusion to set in the generated Java types (for Jackson serializers)
  inclusionLevel = InclusionLevel.NON_NULL

  // Whether to use the 'title' property of the schema to decide the class name (if not
  // set to true, the filename and property names are used).
  useTitleAsClassname = false

  // The type of input documents that will be read. Supported values:
  //  - jsonschema (schema documents, containing formal rules that describe the structure of JSON data)
  //  - json (documents that represent an example of the kind of JSON data that the generated Java types
  //          will be mapped to)
  //  - yamlschema (JSON schema documents, represented as YAML)
  //  - yaml (documents that represent an example of the kind of YAML (or JSON) data that the generated Java types
  //          will be mapped to)
  sourceType = 'jsonschema'

  // Whether to empty the target directory before generation occurs, to clear out all source files
  // that have been generated previously. <strong>Be warned</strong>, when activated this option
  // will cause jsonschema2pojo to <strong>indiscriminately delete the entire contents of the target
  // directory (all files and folders)</strong> before it begins generating sources.
  removeOldOutput = false

  // A class that extends org.jsonschema2pojo.rules.RuleFactory and will be used to
  // create instances of Rules used for code generation.
  customRuleFactory = com.MyCustomRuleFactory

  // The character encoding that should be used when writing the generated Java source files
  outputEncoding = 'UTF-8'

  // Whether to use {@link org.joda.time.DateTime} instead of {@link java.util.Date} when adding
  // date type fields to generated Java types.
  useJodaDates = false

  // Whether to add JsonFormat annotations when using Jackson 2 that cause format "date", "time", and "date-time"
  // fields to be formatted as yyyy-MM-dd, HH:mm:ss.SSS and yyyy-MM-dd'T'HH:mm:ss.SSSZ respectively. To customize these
  // patterns, use customDatePattern, customTimePattern, and customDateTimePattern config options or add these inside a
  // schema to affect an individual field
  formatDateTimes = true
  formatDates = true
  formatTimes = true

  // Whether to initialize Set and List fields as empty collections, or leave them as null.
  initializeCollections = true

  // Whether to add a prefix to generated classes.
  classNamePrefix = ""

  // Whether to add a suffix to generated classes.
  classNameSuffix = ""

  // An array of strings that should be considered as file extensions and therefore not included in class names.
  fileExtensions = [] as String[]

  // Whether to generate constructors or not.
  includeConstructors = false

  // Whether to include java.beans.ConstructorProperties on generated constructors
  includeConstructorPropertiesAnnotation = false

  // Whether to include only 'required' fields in generated constructors
  constructorsRequiredPropertiesOnly = false

  // Whether to *add* a constructor that includes only 'required' fields, alongside other constructors.
  // This property is irrelevant if constructorsRequiredPropertiesOnly = true
  includeRequiredPropertiesConstructor = false

  // Whether to *add* a constructor that includes all fields, alongside other constructors.
  // This property is irrelevant if constructorsRequiredPropertiesOnly = true
  includeAllPropertiesConstructor = false

  // Include a constructor with the class itself as a parameter, with the expectation that all properties
  // from the originating class will assigned to the new class.
  // This property is irrelevant if constructorsRequiredPropertiesOnly = true
  includeCopyConstructor = false

  // Whether to make the generated types Parcelable for Android
  parcelable = false

  // Whether to make the generated types Serializable
  serializable = false

  // Whether to include getters or to omit these accessor methods and create public fields instead.
  includeGetters = true

  // Whether to include setters or to omit these accessor methods and create public fields instead.
  includeSetters = true

  // Whether to include dynamic getters, setters, and builders or to omit these methods.
  includeDynamicAccessors = false

  // Whether to include dynamic getters or to omit these methods.
  includeDynamicGetters = false

  // Whether to include dynamic setters or to omit these methods.
  includeDynamicSetters = false

  // Whether to include dynamic builders or to omit these methods.
  includeDynamicBuilders = false

  // Whether to use org.joda.time.LocalTime for format: date-time. For full control see dateType
  useJodaLocalDates = false

  // Whether to use org.joda.time.LocalDate for format: date
  useJodaLocalTimes = false

  // What type to use instead of string when adding string properties of format "date" to Java types
  dateType = "java.time.LocalDate"

  // What type to use instead of string when adding string properties of format "date-time" to Java types
  dateTimeType = "java.time.LocalDateTime"

  // What type to use instead of string when adding string properties of format "time" to Java types
  timeType = "java.time.LocalTime"

  // A custom pattern to use when formatting date fields during serialization. Requires support from
  // your JSON binding library.
  customDatePattern = "yyyy-MM-dd"

  // A custom pattern to use when formatting date-time fields during serialization. Requires support from
  // your JSON binding library.
  customDateTimePattern = "yyyy-MM-dd HH:mm"

  // A custom pattern to use when formatting time fields during serialization. Requires support from
  // your JSON binding library.
  customTimePattern = "HH:mm"

  // A map offering full control over which Java type will be used for each JSON Schema 'format' value
  formatTypeMapping = [...]

  // Which characters to use as 'path fragment delimiters' when trying to resolve a ref
  refFragmentPathDelimiters = "#/."

  // Whether to include json type information; often required to support polymorphic type handling.
  // By default the type information is stored in the @class property, this can be overridden using
  // deserializationClassProperty in the schema
  includeJsonTypeInfoAnnotation = false

  // Whether to use java.util.Optional for getters on properties that are not required
  useOptionalForGetters = false

  // properties to exclude from generated toString
  toStringExcludes = ["someProperty"]

    // What Java version to target with generated source code (1.6, 1.8, 9, 11, etc).
    // By default, the version will be taken from the Gradle Java plugin's 'sourceCompatibility',
    // which (if unset) itself defaults to the current JVM version
  targetVersion = "1.8"

  // deprecated, since we no longer use commons-lang for equals, hashCode, toString
  useCommonsLang3 = false

  // A customer file filter to allow input files to be filtered/ignored
  fileFilter = new AllFileFilter()

  // A sort order to use when reading input files, one of SourceSortOrder.OS (allow the OS to decide sort
  // order), SourceSortOrder.FILES_FIRST or SourceSortOrder.SUBDIRS_FIRST
  sourceSortOrder = SourceSortOrder.OS

  // Whether to use annotations from jakarta.validation package instead of javax.validation package
  // when adding JSR-303 annotations to generated Java types
  useJakartaValidation = false

  // Deduplicates Java classes and enums if they have identical schemas.
  useDeduplication = false
}
```

## Tasks

### `generateJsonSchema2Pojo`

This task will automatically run in a project where the `jsonSchema2Pojo` configuration closure is present.
It will invoke the jsonschema2pojo generator, make the compileJava task dependent of itself and add
the `targetDirectory` to the main/java source set so the java compiler will find and compile the newly
generated source files.

## Developers

It can be useful to build this project and try out changes in your existing gradle project.

1. From the root, run `mvn clean install`. This will install jsonschema2pojo in your local maven repository.
2. Include the local repo in your build.gradle, and change your dependency to use the `latest.integration` version e.g.:

```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'org.jsonschema2pojo:jsonschema2pojo-gradle-plugin:latest.integration'
    }
}

repositories {
    mavenLocal()
}
```
