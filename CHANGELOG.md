# Changelog

## 0.5.0
* Array elements behavior controlling. ([#783](https://github.com/joelittlejohn/jsonschema2pojo/issues/783))
* Support YAML syntax for JSON Schema ([#778](https://github.com/joelittlejohn/jsonschema2pojo/issues/778))
* Getters and Setters can now be activated independently ([#759](https://github.com/joelittlejohn/jsonschema2pojo/pull/759))
* Generate toString() without reflection ([#753](https://github.com/joelittlejohn/jsonschema2pojo/pull/753))
* Intelligent Gradle plugin up-to-date check ([#748](https://github.com/joelittlejohn/jsonschema2pojo/pull/748))
* Scala case classes support ([#598](https://github.com/joelittlejohn/jsonschema2pojo/issues/598))
* Make the fields used in hashcode and equals generation configurable ([#530](https://github.com/joelittlejohn/jsonschema2pojo/pull/530))

## 0.4.37
* Two default constructors incorrectly added when types are parcelable and includeConstructors is active ([#742](https://github.com/joelittlejohn/jsonschema2pojo/issues/742))

## 0.4.36
* Add customTimePattern and formatTimes config options ([#740](https://github.com/joelittlejohn/jsonschema2pojo/pull/740))
* Schema with required[""] results in String index out of range: 0  ([#725](https://github.com/joelittlejohn/jsonschema2pojo/issues/725))
* [Android] Writing to /Reading from Parcelable ignores superclass  ([#602](https://github.com/joelittlejohn/jsonschema2pojo/issues/602))

## 0.4.35
* Add support for excluding fields from generated toString methods ([#720](https://github.com/joelittlejohn/jsonschema2pojo/pull/720))

## 0.4.34
* Add 'sourceSortOrder' to decide how source files are iterated ([#719](https://github.com/joelittlejohn/jsonschema2pojo/pull/719))

## 0.4.33
* Support customDatePattern and customDateTimePattern as global config options ([#716](https://github.com/joelittlejohn/jsonschema2pojo/issues/716))
* Support customPattern as an alias for customDatePattern and customDateTimePattern ([#715](https://github.com/joelittlejohn/jsonschema2pojo/issues/715))
* formatDateTimes default pattern should include timezone ([#714](https://github.com/joelittlejohn/jsonschema2pojo/issues/714))
* Fix compilation errors for non-string enums ([#713](https://github.com/joelittlejohn/jsonschema2pojo/pull/713))
* Add 'formatDates' config option to add JsonFormat annotation on format "date" fields ([#699](https://github.com/joelittlejohn/jsonschema2pojo/issues/699))

## 0.4.32
* org.jsonschema2pojo.ContentResolverTest require internet connection ([#704](https://github.com/joelittlejohn/jsonschema2pojo/issues/704))
* Add option to specify custom fragment path delimiters ([#697](https://github.com/joelittlejohn/jsonschema2pojo/pull/697))
* javaType doesn't work while setting  classNamePrefix = "SomePrefix" ([#650](https://github.com/joelittlejohn/jsonschema2pojo/issues/650))

## 0.4.31
* List property called "status" has items with type "Statu" ([#695](https://github.com/joelittlejohn/jsonschema2pojo/issues/695))

## 0.4.30
* Cannot use custom date,time types from external dependencies in maven/ant plugins ([#673](https://github.com/joelittlejohn/jsonschema2pojo/issues/673))
* Jackson inclusion option ([#671](https://github.com/joelittlejohn/jsonschema2pojo/pull/671))
* Support URI format properties with a default value using URI.create ([#664](https://github.com/joelittlejohn/jsonschema2pojo/issues/664))
* Support configurable serialization inclusion for Jackson ([#629](https://github.com/joelittlejohn/jsonschema2pojo/issues/629))

## 0.4.29
* Fix include additional properties set incorrectly by CLI ([#660](https://github.com/joelittlejohn/jsonschema2pojo/pull/660))
* Remove useless and empty Javadoc ([#659](https://github.com/joelittlejohn/jsonschema2pojo/issues/659))
* @JsonPropertyDescription annotation does not properly extract description from schema ([#655](https://github.com/joelittlejohn/jsonschema2pojo/issues/655))
* Support @JsonFormat annotation for date-time fields ([#643](https://github.com/joelittlejohn/jsonschema2pojo/pull/643))

## 0.4.28
* Adding support for JSR-305 annotations ([#645](https://github.com/joelittlejohn/jsonschema2pojo/pull/645))
* additionalProperties getter method is missing @Valid annotation. ([#503](https://github.com/joelittlejohn/jsonschema2pojo/issues/503))
* Zip of binary distribution of CLI ([#500](https://github.com/joelittlejohn/jsonschema2pojo/issues/500))

## 0.4.27
* Add ["-", " ", "_"] as default propertyWordDelimiters for Gradle plugin ([#625](https://github.com/joelittlejohn/jsonschema2pojo/pull/625))
* Allow setting custom annotator and custom rule factory fields directly in Gradle ([#617](https://github.com/joelittlejohn/jsonschema2pojo/pull/617))
* Deep-merge array items in example JSONs before generating schemas ([#423](https://github.com/joelittlejohn/jsonschema2pojo/issues/423))

## 0.4.26
* Remove javax.annotation.Generated as it doesn't ship with Android ([#577](https://github.com/joelittlejohn/jsonschema2pojo/issues/577))

## 0.4.25
* Add option to use BigInteger for JSON integer type ([#614](https://github.com/joelittlejohn/jsonschema2pojo/pull/614))
* Supporting other primitives as backing types for enums ([#612](https://github.com/joelittlejohn/jsonschema2pojo/pull/612))
* Generate hashCode and equals even without declared fields ([#611](https://github.com/joelittlejohn/jsonschema2pojo/pull/611))
* Use unique enum constant names ([#609](https://github.com/joelittlejohn/jsonschema2pojo/pull/609))

## 0.4.24
* Support "required" keyword draft04 array-style when generating constuctors ([#599](https://github.com/joelittlejohn/jsonschema2pojo/pull/599))
* Add feature to allow comments in schema files ([#589](https://github.com/joelittlejohn/jsonschema2pojo/issues/589))
* javaType with generics does not apply correct imports when "type": "object" is omitted ([#582](https://github.com/joelittlejohn/jsonschema2pojo/issues/582))
* Add @JsonPropertyDescription when "description" property present ([#571](https://github.com/joelittlejohn/jsonschema2pojo/pull/571))
* Constructors are incompatible with extends keyword ([#555](https://github.com/joelittlejohn/jsonschema2pojo/issues/555))

## 0.4.23
* Force lower camel case fields when using JSON ([#563](https://github.com/joelittlejohn/jsonschema2pojo/issues/563))
* Fix broken embedded refs ([#560](https://github.com/joelittlejohn/jsonschema2pojo/pull/560))
* Added file extensions config parameter ([#548](https://github.com/joelittlejohn/jsonschema2pojo/pull/548))
* Overridden builders are not added to empty child class ([#538](https://github.com/joelittlejohn/jsonschema2pojo/issues/538))
* Wrong default value for BigDecimal ([#535](https://github.com/joelittlejohn/jsonschema2pojo/issues/535))
* Add Moshi 1.x annotation style ([#529](https://github.com/joelittlejohn/jsonschema2pojo/pull/529))
* Add serializable as an optional/configurable option ([#404](https://github.com/joelittlejohn/jsonschema2pojo/issues/404))

## 0.4.22
* Adding date-time types to Maven plugin mojo configuration ([#531](https://github.com/joelittlejohn/jsonschema2pojo/pull/531))
* Fix javaName breaking required properties ([#528](https://github.com/joelittlejohn/jsonschema2pojo/pull/528))
* Add support for using BigDecimal to represent numbers ([#517](https://github.com/joelittlejohn/jsonschema2pojo/pull/517))
* Jsonschema2pojoRule report problems to STDERR ([#513](https://github.com/joelittlejohn/jsonschema2pojo/pull/513))

## 0.4.21
* Fix NullPointerException in Gradle plugin for Java (non-Android) projects ([#510](https://github.com/joelittlejohn/jsonschema2pojo/pull/510))

## 0.4.20
* Regression: when a extending a schema with a redefinition of the same field, with methods should be generated only once ([#507](https://github.com/joelittlejohn/jsonschema2pojo/issues/507))
* Unnecessary @SuppressWarnings java warnings after upgrade ([#498](https://github.com/joelittlejohn/jsonschema2pojo/issues/498))
* Improved Android support via Gradle plugin (NOTE: Changes behaviour regarding output directory) ([#495](https://github.com/joelittlejohn/jsonschema2pojo/pull/495))

## 0.4.19
* Add javaName schema property ([#499](https://github.com/joelittlejohn/jsonschema2pojo/pull/499))
* Declare an enum without that "static" ([#496](https://github.com/joelittlejohn/jsonschema2pojo/issues/496))
* includeDynamicAccessors should default to false (don't include dynamic accessors by default) ([#482](https://github.com/joelittlejohn/jsonschema2pojo/pull/482))
* Inflector - incorrect for word "specimen" ([#479](https://github.com/joelittlejohn/jsonschema2pojo/issues/479))
* Allow JSON to be passed as a String to SchemaMapper ([#475](https://github.com/joelittlejohn/jsonschema2pojo/pull/475))
* Prefix / Suffix support when javaType is not specified ([#465](https://github.com/joelittlejohn/jsonschema2pojo/pull/465))

## 0.4.18
* Added support for custom date-time, date, time classes. ([#469](https://github.com/joelittlejohn/jsonschema2pojo/pull/469))
* Databases element of type array generated class name Databasis instead of Database ([#461](https://github.com/joelittlejohn/jsonschema2pojo/issues/461))

## 0.4.17
* Covariant builders - copy builder methods from superclass, with narrow return type ([#455](https://github.com/joelittlejohn/jsonschema2pojo/issues/455))
* Specify target JVM version ([#454](https://github.com/joelittlejohn/jsonschema2pojo/pull/454))
* Nested refs not resolved correctly when schema supplied as string ([#446](https://github.com/joelittlejohn/jsonschema2pojo/issues/446))
* Make Inflector more amenable to thread safety. ([#435](https://github.com/joelittlejohn/jsonschema2pojo/pull/435))
* Add Dynamic Accessors ([#386](https://github.com/joelittlejohn/jsonschema2pojo/pull/386))

## 0.4.16
* Use minimum and maximum value of an integer property to determine java type long ([#434](https://github.com/joelittlejohn/jsonschema2pojo/pull/434))
* Fix path to jar in windows script for the case it is run not from home dir ([#432](https://github.com/joelittlejohn/jsonschema2pojo/pull/432))
* Make the quick lookup map 'final' ([#428](https://github.com/joelittlejohn/jsonschema2pojo/pull/428))
* "extends" schema resolution assumes current schema is at root of file ([#425](https://github.com/joelittlejohn/jsonschema2pojo/issues/425))
* Recursive $ref failed ([#250](https://github.com/joelittlejohn/jsonschema2pojo/issues/250))

## 0.4.15
* $ref doesn't work when ref'ed message also has extends ([#408](https://github.com/joelittlejohn/jsonschema2pojo/issues/408))
* Support direct class inheritance with 'extendsJavaType', without using 'extends' ([#402](https://github.com/joelittlejohn/jsonschema2pojo/issues/402))
* additionalProperties java.util.Map is not generated by default when embedding this library and extending DefaultGenerationConfig ([#398](https://github.com/joelittlejohn/jsonschema2pojo/issues/398))
* If a schema sits inside a directory with a hyphen in its name, plugin will generate an invalid Java package name ([#383](https://github.com/joelittlejohn/jsonschema2pojo/issues/383))
* Allow get/set methods to be disabled, use public fields instead ([#355](https://github.com/joelittlejohn/jsonschema2pojo/issues/355))

## 0.4.14
* Fix 'Unable to load class org.jsonschema2pojo.NoopAnnotator' from Gradle plugin ([#395](https://github.com/joelittlejohn/jsonschema2pojo/pull/395))
* Library projects should use android.libraryVariants instead of android.applicationVariants  ([#391](https://github.com/joelittlejohn/jsonschema2pojo/pull/391))
* Allow nullable type (instead of Object) when JSON type is an array with ["string", "null"] ([#390](https://github.com/joelittlejohn/jsonschema2pojo/issues/390))
* Allow custom names to be supplied when creating an enum ([#385](https://github.com/joelittlejohn/jsonschema2pojo/issues/385))
* Allow disabling additionalProperties generation with a global config option ([#376](https://github.com/joelittlejohn/jsonschema2pojo/issues/376))
* 'javaInterfaces' which contain generics produce syntactically incorrect imports  ([#317](https://github.com/joelittlejohn/jsonschema2pojo/issues/317))

## 0.4.13
* RequiredArrayRule not working for properties with _ or -. ([#377](https://github.com/joelittlejohn/jsonschema2pojo/issues/377))
* Maven plugin shows sourcePaths error for includes/excludes when sourcePaths is not defined ([#374](https://github.com/joelittlejohn/jsonschema2pojo/issues/374))

## 0.4.12
* Didn't work in Android Studio with with current master branch '040e3ae'  ([#370](https://github.com/joelittlejohn/jsonschema2pojo/issues/370))
* Use the correct ClassLoader in createFromParcel for lists ([#356](https://github.com/joelittlejohn/jsonschema2pojo/pull/356))
* Find jar when jsonschema2pojo sh script is not running in script folder ([#351](https://github.com/joelittlejohn/jsonschema2pojo/pull/351))
* Fix parameter description of --class-prefix and --class-suffix. ([#350](https://github.com/joelittlejohn/jsonschema2pojo/pull/350))
* Multiple enum array properties with same name cause JClassAlreadyExistsException ([#349](https://github.com/joelittlejohn/jsonschema2pojo/pull/349))
* Allow empty package names ([#348](https://github.com/joelittlejohn/jsonschema2pojo/pull/348))
* Corrected Exclude pattern behavior with Maven to avoid excluding all files ([#340](https://github.com/joelittlejohn/jsonschema2pojo/pull/340))

## 0.4.11
* Add super() to the equals/hashcode impl ([#333](https://github.com/joelittlejohn/jsonschema2pojo/pull/333))
* Use Gson @SerializedName annotation on ALL fields, even when JSON name matches Java name ([#327](https://github.com/joelittlejohn/jsonschema2pojo/issues/327))
* Adding support for required array from http://tools.ietf.org/html/draft-... ([#325](https://github.com/joelittlejohn/jsonschema2pojo/pull/325))
* default tag doesn't work for empty string ([#320](https://github.com/joelittlejohn/jsonschema2pojo/issues/320))
* **EXPERIMENTAL** Support Parcelable types for Android ([#127](https://github.com/joelittlejohn/jsonschema2pojo/issues/127))

## 0.4.10
* Initial class name is truncated after first . char, instead of last . char ([#313](https://github.com/joelittlejohn/jsonschema2pojo/issues/313))
* Add support for android library plugin ([#305](https://github.com/joelittlejohn/jsonschema2pojo/issues/305))
* Support "javaType" for things that are not "type": "object" ([#223](https://github.com/joelittlejohn/jsonschema2pojo/issues/223))

## 0.4.9
* Array types are ignored inside generics in javaType ([#299](https://github.com/joelittlejohn/jsonschema2pojo/issues/299))
* Add config options to generate Joda LocalDate and LocalTime ([#298](https://github.com/joelittlejohn/jsonschema2pojo/issues/298))
* Allow URLs as source locations ([#293](https://github.com/joelittlejohn/jsonschema2pojo/issues/293))

## 0.4.8
* Android support in gradle plugin ([#292](https://github.com/joelittlejohn/jsonschema2pojo/pull/292))
* Add support for Jackson's JsonView ([#291](https://github.com/joelittlejohn/jsonschema2pojo/pull/291))
* Added support for javaType on integer and number properties. ([#287](https://github.com/joelittlejohn/jsonschema2pojo/pull/287))
* Option to create constructors (with all fields, or with required fields) ([#231](https://github.com/joelittlejohn/jsonschema2pojo/issues/231))
* Support nested generic type arguments in javaType ([#196](https://github.com/joelittlejohn/jsonschema2pojo/issues/196))

## 0.4.7
* Adding UUID support to FormatRule ([#270](https://github.com/joelittlejohn/jsonschema2pojo/pull/270))
* Add GSON annotation '@SerializedName' to Enums ([#267](https://github.com/joelittlejohn/jsonschema2pojo/issues/267))
* Add ability to set prefixes/suffixes for generated classes ([#258](https://github.com/joelittlejohn/jsonschema2pojo/pull/258))

## 0.4.6
* Support for @JsonTypeInfo in generated classes using deserializationClassProperty ([#235](https://github.com/joelittlejohn/jsonschema2pojo/issues/235)
* ClassNotFoundException when attempting to use a custom annotator from a project dependency ([#256](https://github.com/joelittlejohn/jsonschema2pojo/issues/256))
* Inflector incorrectly singularizes Address to Addres  ([#244](https://github.com/joelittlejohn/jsonschema2pojo/issues/244))
* propertyWordDelimiters config is ignored ([#243](https://github.com/joelittlejohn/jsonschema2pojo/issues/243))
* Improve the way Equals and Hashcode are generated ([#241](https://github.com/joelittlejohn/jsonschema2pojo/pull/241))

## 0.4.5
* Allow sets to have an empty default value ([#232](https://github.com/joelittlejohn/jsonschema2pojo/pull/232))
* Use LinkedHashSet when deserializing Set to preserve order ([#227](https://github.com/joelittlejohn/jsonschema2pojo/pull/227))
* Allow SchemaMapper to generate from String and not only URL ([#221](https://github.com/joelittlejohn/jsonschema2pojo/pull/221))
* Generate @Param and @Returns javadoc ([#219](https://github.com/joelittlejohn/jsonschema2pojo/pull/219))
* Creating a list of objects with class name as “S” ([#213](https://github.com/joelittlejohn/jsonschema2pojo/issues/213))
* Add 'customRuleFactory' config option for pluggable RuleFactory ([#211](https://github.com/joelittlejohn/jsonschema2pojo/pull/211))

## 0.4.4
* Added ')' to jdk unbounded range for Maven 2.X ([#205](https://github.com/joelittlejohn/jsonschema2pojo/pull/205))
* Allow Sets and Lists to be initialized to null (instead of an empty collection) ([#203](https://github.com/joelittlejohn/jsonschema2pojo/pull/203))

## 0.4.3
* Add includes & excludes Maven config options for source file filtering ([#200](https://github.com/joelittlejohn/jsonschema2pojo/pull/200))
* Reorganise module dependencies, pull CLI out from under other plugins ([#198](https://github.com/joelittlejohn/jsonschema2pojo/pull/198))
* "properties" property implies "type": "object" ([#192](https://github.com/joelittlejohn/jsonschema2pojo/issues/192))
* Preserve casing of field names ([#187](https://github.com/joelittlejohn/jsonschema2pojo/issues/187))
* sourceType = 'json' is ignored by Gradle plugin ([#184](https://github.com/joelittlejohn/jsonschema2pojo/issues/184))
* Handling local references on jsonschema2pojo.org ([#183](https://github.com/joelittlejohn/jsonschema2pojo/issues/183))

## 0.4.2
* Builder Method for Additional Properties ([#175](https://github.com/joelittlejohn/jsonschema2pojo/pull/175))
* javaInterfaces extension property is ignored for Enum types ([#172](https://github.com/joelittlejohn/jsonschema2pojo/issues/172))
* Binary property support ([#171](https://github.com/joelittlejohn/jsonschema2pojo/pull/171))
* Support annotating additionalProperties field. ([#170](https://github.com/joelittlejohn/jsonschema2pojo/pull/170))
* customAnnotator shows 'dynamic properties deprecated' warning when using Gradle plugin ([#163](https://github.com/joelittlejohn/jsonschema2pojo/issues/163))

## 0.4.1
* Switch from HashSet to LinkedHashSet for uniqueItems arrays to preserve order ([#159](https://github.com/joelittlejohn/jsonschema2pojo/issues/159))
* Add @Valid on all collections, not just those with items type "object" ([#158](https://github.com/joelittlejohn/jsonschema2pojo/issues/158))
* Allow collections to be null by default ([#156](https://github.com/joelittlejohn/jsonschema2pojo/issues/156))
* Add support for generic type arguments to javaType ([#151](https://github.com/joelittlejohn/jsonschema2pojo/issues/151))
* sourceType config option is ignored by the gradle plugin ([#144](https://github.com/joelittlejohn/jsonschema2pojo/issues/144))
* Array properties that include the word "men" in the name result in incorrectly named item class ([#142](https://github.com/joelittlejohn/jsonschema2pojo/issues/142))
* Type "Number" is generating Floats instead of Double ([#141](https://github.com/joelittlejohn/jsonschema2pojo/issues/141))
* Add config option to choose commons-lang3 for hashCode/toString implementation ([#140](https://github.com/joelittlejohn/jsonschema2pojo/issues/140))
* Return JType from SchemaMapper.generate(...) ([#137](https://github.com/joelittlejohn/jsonschema2pojo/issues/137))

## 0.4.0
* Rename setAdditionalProperties to avoid confusing naive introspectors ([#136](https://github.com/joelittlejohn/jsonschema2pojo/issues/136))
* ExtendedCharacters tests fail on command line, but pass in Eclipse (Windows) ([#131](https://github.com/joelittlejohn/jsonschema2pojo/issues/131))
* Long integers become java.lang.Double when using JSON source type ([#130](https://github.com/joelittlejohn/jsonschema2pojo/issues/130))
* Integration tests in GsonIT suite fail on Windows ([#129](https://github.com/joelittlejohn/jsonschema2pojo/issues/129))
* JSON schema with enum member with a name starting with a capital letter, causes a generation of a code that doesn't compile ([#126](https://github.com/joelittlejohn/jsonschema2pojo/issues/126))
* Contribute Gradle plugin ([#123](https://github.com/joelittlejohn/jsonschema2pojo/pull/123))
* Corrected default annotationStyle to be jackson2 ([#122](https://github.com/joelittlejohn/jsonschema2pojo/pull/122))
* Enable maven plugin to recurse subdirectories for schema to code generation ([#117](https://github.com/joelittlejohn/jsonschema2pojo/issues/117))
* Migrate groupId to org.jsonschema2pojo ([#116](https://github.com/joelittlejohn/jsonschema2pojo/issues/116))
* Migrate package structure to org.jsonschema2pojo ([#115](https://github.com/joelittlejohn/jsonschema2pojo/issues/115))
* Allow system default charset to be overriden ([#113](https://github.com/joelittlejohn/jsonschema2pojo/issues/113))
* Configuration option to use Joda types ([#110](https://github.com/joelittlejohn/jsonschema2pojo/issues/110))
* Default propertyWordDelimiters to commonly used characters ([#109](https://github.com/joelittlejohn/jsonschema2pojo/issues/109))

## 0.3.7
* Standalone enums have wrong package name ([#103](https://github.com/joelittlejohn/jsonschema2pojo/issues/103))
* Type names are printed to stdout during code generation ([#101](https://github.com/joelittlejohn/jsonschema2pojo/issues/101))
* @Valid is not being added to an arrays of objects that are defined using a $ref ([#97](https://github.com/joelittlejohn/jsonschema2pojo/issues/97))
* ContentResolver.resolve throws a.lang.IllegalArgumentException: Unrecognised URI when path to schema contains space ([#94](https://github.com/joelittlejohn/jsonschema2pojo/issues/94))
* Add 'removeOldOutput' option to clear all previously generated sources ([#92](https://github.com/joelittlejohn/jsonschema2pojo/issues/92))
* Add support for generating Gson compatible types ([#70](https://github.com/joelittlejohn/jsonschema2pojo/issues/70))
* Add extension to force generated classes to implement additional interface(s)  ([#60](https://github.com/joelittlejohn/jsonschema2pojo/issues/60))

## 0.3.6
* Better inflection when making array names singular ([#96](https://github.com/joelittlejohn/jsonschema2pojo/issues/96))
* Optimized deserialization for enum values ([#95](https://github.com/joelittlejohn/jsonschema2pojo/issues/95))
* Elements with similar names are overwritten ([#93](https://github.com/joelittlejohn/jsonschema2pojo/issues/93))
* Support for classpath in Ant target ([#89](https://github.com/joelittlejohn/jsonschema2pojo/issues/89))
* Support for custom Annotator classes ([#86](https://github.com/joelittlejohn/jsonschema2pojo/issues/86))

## 0.3.5
* Add support for http:// URLs when using $ref ([#87](https://github.com/joelittlejohn/jsonschema2pojo/issues/87))
* Add support for resource: URLs when using $ref ([#85](https://github.com/joelittlejohn/jsonschema2pojo/issues/85))
* JSON property called "class" creates a method called "getClass" which Java is not happy about ([#84](https://github.com/joelittlejohn/jsonschema2pojo/issues/84))

## 0.3.4
* Support @Valid annotation for transitive validation ([#82](https://github.com/joelittlejohn/jsonschema2pojo/issues/82))
* Add minLength/maxLength to JSR-303 support ([#78](https://github.com/joelittlejohn/jsonschema2pojo/issues/78))
* Array properties use item type instead of collection type when using a ref to an array schema more than once ([#76](https://github.com/joelittlejohn/jsonschema2pojo/issues/76))
* Array item type is not named well when array property name ends in 'ies' ([#75](https://github.com/joelittlejohn/jsonschema2pojo/issues/75))
* Run a hosted/web version of jsonschema2pojo ([#66](https://github.com/joelittlejohn/jsonschema2pojo/issues/66))

## 0.3.3
* Configuration propertyWordDelimiters are used for properties but ignored when naming new Java types ([#73](https://github.com/joelittlejohn/jsonschema2pojo/issues/73))
* Reserved Java words are not transformed during object generation when propertyWordDelimiters=_ is used ([#72](https://github.com/joelittlejohn/jsonschema2pojo/issues/72))
* Threadsafe Maven generate mojo ([#71](https://github.com/joelittlejohn/jsonschema2pojo/issues/71))
* Support generating types 'by example', using example JSON document instead of a JSON Schema ([#42](https://github.com/joelittlejohn/jsonschema2pojo/issues/42))

## 0.3.2
* Switch from commons-jci snapshot to jsr-199 compiler for integration tests ([#68](https://github.com/joelittlejohn/jsonschema2pojo/issues/68))
* Support both Jackson 1.x and Jackson 2.x ([#64](https://github.com/joelittlejohn/jsonschema2pojo/issues/64))
* Property name that is a java keyword generates java code with compile error ([#63](https://github.com/joelittlejohn/jsonschema2pojo/issues/63))
* Some tests fail on Windows due to line.separator differences ([#56](https://github.com/joelittlejohn/jsonschema2pojo/issues/56))
* Support for JSR-303 validation annotations ([#18](https://github.com/joelittlejohn/jsonschema2pojo/issues/18))

## 0.3.1
* Add support for multiple source files/directories ([#62](https://github.com/joelittlejohn/jsonschema2pojo/issues/62))
* Default null value causes NumberFormatException ([#61](https://github.com/joelittlejohn/jsonschema2pojo/issues/61))
* Compile error when schema contains list with empty default value ([#59](https://github.com/joelittlejohn/jsonschema2pojo/issues/59))
* Exception thrown for empty string enum value. ([#58](https://github.com/joelittlejohn/jsonschema2pojo/issues/58))
* Add flag not to generate hashCode() and equals() to Maven plugin ([#53](https://github.com/joelittlejohn/jsonschema2pojo/issues/53))
* Add flag to Maven plugin that allows Jackson annotations to be omitted ([#52](https://github.com/joelittlejohn/jsonschema2pojo/issues/52))
* No source file in exception from parse error ([#50](https://github.com/joelittlejohn/jsonschema2pojo/issues/50))

## 0.3.0
* Switch from Easymock to Mockito ([#49](https://github.com/joelittlejohn/jsonschema2pojo/issues/49))
* IllegalArgumentException when using hyphen as a delimiter in the CLI ([#47](https://github.com/joelittlejohn/jsonschema2pojo/issues/47))
* Remove Serializable from POJOs since they aren't (necessarily) ([#45](https://github.com/joelittlejohn/jsonschema2pojo/issues/45))
* Migrate to Jackson 2.0 ([#44](https://github.com/joelittlejohn/jsonschema2pojo/issues/44))
* Support for union types ([#17](https://github.com/joelittlejohn/jsonschema2pojo/issues/17))

## 0.2.3
* Null pointer when invoking the maven plugin if propertyWordDelimiters not set ([#46](https://github.com/joelittlejohn/jsonschema2pojo/issues/46))

## 0.2.2
* Add support for primitive types via the javaType property ([#41](https://github.com/joelittlejohn/jsonschema2pojo/issues/41))
* generation yields unreproducible results for "additionalProperties" ([#40](https://github.com/joelittlejohn/jsonschema2pojo/issues/40))
* Option for generating CamelCase names ([#39](https://github.com/joelittlejohn/jsonschema2pojo/issues/39))

## 0.2.1
* Add 'skip' property to allow maven plugin to be easily switched off ([#37](https://github.com/joelittlejohn/jsonschema2pojo/issues/37))
* Keep wiki example in line with code automatically ([#35](https://github.com/joelittlejohn/jsonschema2pojo/issues/35))
* Add Eclipse m2e support ([#34](https://github.com/joelittlejohn/jsonschema2pojo/issues/34))
* Ant task(s) for jsonschema2pojo ([#23](https://github.com/joelittlejohn/jsonschema2pojo/issues/23))

## 0.2.0
* Migrate to git ([#33](https://github.com/joelittlejohn/jsonschema2pojo/issues/33))
* Remove deprecated 'optional' rule ([#32](https://github.com/joelittlejohn/jsonschema2pojo/issues/32))
* Properties should be nullable, use wrapper types not primitives ([#31](https://github.com/joelittlejohn/jsonschema2pojo/issues/31))

## 0.1.10
* omitting targetPackage causes NullPointerException in maven plugin ([#30](https://github.com/joelittlejohn/jsonschema2pojo/issues/30))
* Improve serialization ordering ([#27](https://github.com/joelittlejohn/jsonschema2pojo/issues/27))

## 0.1.9
* Omit null valued properties when deserializing POJOs ([#29](https://github.com/joelittlejohn/jsonschema2pojo/issues/29))
* Publish maven plugin documentation ([#26](https://github.com/joelittlejohn/jsonschema2pojo/issues/26))
* Make the addition of output dir as a source root optional ([#25](https://github.com/joelittlejohn/jsonschema2pojo/issues/25))
* Plugin does not respect types present on the project classpath ([#24](https://github.com/joelittlejohn/jsonschema2pojo/issues/24))

## 0.1.8
* Add support for more format values ([#21](https://github.com/joelittlejohn/jsonschema2pojo/issues/21))
* Pom files cause plugin version warnings in Maven 3 ([#20](https://github.com/joelittlejohn/jsonschema2pojo/issues/20))
* Support for 'extends' schema rule ([#14](https://github.com/joelittlejohn/jsonschema2pojo/issues/14))

## 0.1.7
* Copyright notices are currently out-of-date ([#19](https://github.com/joelittlejohn/jsonschema2pojo/issues/19))
* Support for 'default' schema rule ([#15](https://github.com/joelittlejohn/jsonschema2pojo/issues/15))
* Add acceptance test framework for better end-to-end testing ([#10](https://github.com/joelittlejohn/jsonschema2pojo/issues/10))
* Create empty collection when mapping JSON array to Java List/Set ([#3](https://github.com/joelittlejohn/jsonschema2pojo/issues/3))

## 0.1.6
* Properties with special characters in name are not marshalled/unmarshalled correctly ([#13](https://github.com/joelittlejohn/jsonschema2pojo/issues/13))
* Format rule only applies to strings, spec says it is valid for any type ([#12](https://github.com/joelittlejohn/jsonschema2pojo/issues/12))
* Type rule does not default to "any" or accept unrecognised types as per spec ([#11](https://github.com/joelittlejohn/jsonschema2pojo/issues/11))
* Support for $ref ([#7](https://github.com/joelittlejohn/jsonschema2pojo/issues/7))

## 0.1.5
* Jackson Enum annotations aren't applied correctly ([#9](https://github.com/joelittlejohn/jsonschema2pojo/issues/9))
* Publish versioned javadocs & remove head javadocs from repo ([#6](https://github.com/joelittlejohn/jsonschema2pojo/issues/6))

## 0.1.4
* Schema with non-complex type as root element produces extra/invalid pojos ([#8](https://github.com/joelittlejohn/jsonschema2pojo/issues/8))

## 0.1.3
* Support additionalProperties from json schema ([#5](https://github.com/joelittlejohn/jsonschema2pojo/issues/5))

## 0.1.2
* Create builder-style pojos ([#2](https://github.com/joelittlejohn/jsonschema2pojo/issues/2))

## 0.1.1
* Automate all release tasks ([#48](https://github.com/joelittlejohn/jsonschema2pojo/issues/48))

## 0.1.0
