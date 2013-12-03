# Changelog

## 0.4.0
* Rename setAdditionalProperties to avoid confusing naive introspectors ([#136](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/136))
* ExtendedCharacters tests fail on command line, but pass in Eclipse (Windows) ([#131](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/131))
* Long integers become java.lang.Double when using JSON source type ([#130](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/130))
* Integration tests in GsonIT suite fail on Windows ([#129](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/129))
* JSON schema with enum member with a name starting with a capital letter, causes a generation of a code that doesn't compile ([#126](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/126))
* Contribute Gradle plugin ([#123](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/123))
* Corrected default annotationStyle to be jackson2 ([#122](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/122))
* Enable maven plugin to recurse subdirectories for schema to code generation ([#117](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/117))
* Migrate groupId to org.jsonschema2pojo ([#116](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/116))
* Migrate package structure to org.jsonschema2pojo ([#115](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/115))
* Allow system default charset to be overriden ([#113](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/113))
* Configuration option to use Joda types ([#110](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/110))
* Default propertyWordDelimiters to commonly used characters ([#109](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/109))

## 0.3.7
* Standalone enums have wrong package name ([#103](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/103))
* Type names are printed to stdout during code generation ([#101](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/101))
* @Valid is not being added to an arrays of objects that are defined using a $ref ([#97](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/97))
* ContentResolver.resolve throws a.lang.IllegalArgumentException: Unrecognised URI when path to schema contains space ([#94](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/94))
* Add 'removeOldOutput' option to clear all previously generated sources ([#92](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/92))
* Add support for generating Gson compatible types ([#70](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/70))
* Add extension to force generated classes to implement additional interface(s)  ([#60](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/60))

## 0.3.6
* Better inflection when making array names singular ([#96](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/96))
* Optimized deserialization for enum values ([#95](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/95))
* Elements with similar names are overwritten ([#93](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/93))
* Support for classpath in Ant target ([#89](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/89))
* Support for custom Annotator classes ([#86](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/86))

## 0.3.5
* Add support for http:// URLs when using $ref ([#87](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/87))
* Add support for resource: URLs when using $ref ([#85](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/85))
* JSON property called "class" creates a method called "getClass" which Java is not happy about ([#84](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/84))

## 0.3.4
* Support @Valid annotation for transitive validation ([#82](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/82))
* Add minLength/maxLength to JSR-303 support ([#78](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/78))
* Array properties use item type instead of collection type when using a ref to an array schema more than once ([#76](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/76))
* Array item type is not named well when array property name ends in 'ies' ([#75](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/75))
* Run a hosted/web version of jsonschema2pojo ([#66](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/66))

## 0.3.3
* Configuration propertyWordDelimiters are used for properties but ignored when naming new Java types ([#73](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/73))
* Reserved Java words are not transformed during object generation when propertyWordDelimiters=_ is used ([#72](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/72))
* Threadsafe Maven generate mojo ([#71](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/71))
* Support generating types 'by example', using example JSON document instead of a JSON Schema ([#42](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/42))

## 0.3.2
* Switch from commons-jci snapshot to jsr-199 compiler for integration tests ([#68](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/68))
* Support both Jackson 1.x and Jackson 2.x ([#64](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/64))
* Property name that is a java keyword generates java code with compile error ([#63](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/63))
* Some tests fail on Windows due to line.separator differences ([#56](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/56))
* Support for JSR-303 validation annotations ([#18](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/18))

## 0.3.1
* Add support for multiple source files/directories ([#62](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/62))
* Default null value causes NumberFormatException ([#61](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/61))
* Compile error when schema contains list with empty default value ([#59](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/59))
* Exception thrown for empty string enum value. ([#58](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/58))
* Add flag not to generate hashCode() and equals() to Maven plugin ([#53](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/53))
* Add flag to Maven plugin that allows Jackson annotations to be omitted ([#52](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/52))
* No source file in exception from parse error ([#50](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/50))

## 0.3.0
* Switch from Easymock to Mockito ([#49](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/49))
* IllegalArgumentException when using hyphen as a delimiter in the CLI ([#47](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/47))
* Remove Serializable from POJOs since they aren't (necessarily) ([#45](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/45))
* Migrate to Jackson 2.0 ([#44](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/44))
* Support for union types ([#17](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/17))

## 0.2.3
* Null pointer when invoking the maven plugin if propertyWordDelimiters not set ([#46](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/46))

## 0.2.2
* Add support for primitive types via the javaType property ([#41](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/41))
* generation yields unreproducible results for "additionalProperties" ([#40](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/40))
* Option for generating CamelCase names ([#39](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/39))

## 0.2.1
* Add 'skip' property to allow maven plugin to be easily switched off ([#37](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/37))
* Keep wiki example in line with code automatically ([#35](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/35))
* Add Eclipse m2e support ([#34](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/34))
* Ant task(s) for jsonschema2pojo ([#23](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/23))

## 0.2.0
* Migrate to git ([#33](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/33))
* Remove deprecated 'optional' rule ([#32](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/32))
* Properties should be nullable, use wrapper types not primitives ([#31](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/31))

## 0.1.10
* omitting targetPackage causes NullPointerException in maven plugin ([#30](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/30))
* Improve serialization ordering ([#27](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/27))

## 0.1.9
* Omit null valued properties when deserializing POJOs ([#29](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/29))
* Publish maven plugin documentation ([#26](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/26))
* Make the addition of output dir as a source root optional ([#25](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/25))
* Plugin does not respect types present on the project classpath ([#24](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/24))

## 0.1.8
* Add support for more format values ([#21](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/21))
* Pom files cause plugin version warnings in Maven 3 ([#20](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/20))
* Support for 'extends' schema rule ([#14](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/14))

## 0.1.7
* Copyright notices are currently out-of-date ([#19](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/19))
* Support for 'default' schema rule ([#15](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/15))
* Add acceptance test framework for better end-to-end testing ([#10](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/10))
* Create empty collection when mapping JSON array to Java List/Set ([#3](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/3))

## 0.1.6
* Properties with special characters in name are not marshalled/unmarshalled correctly ([#13](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/13))
* Format rule only applies to strings, spec says it is valid for any type ([#12](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/12))
* Type rule does not default to "any" or accept unrecognised types as per spec ([#11](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/11))
* Support for $ref ([#7](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/7))

## 0.1.5
* Jackson Enum annotations aren't applied correctly ([#9](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/9))
* Publish versioned javadocs & remove head javadocs from repo ([#6](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/6))

## 0.1.4
* Schema with non-complex type as root element produces extra/invalid pojos ([#8](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/8))

## 0.1.3
* Support additionalProperties from json schema ([#5](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/5))

## 0.1.2
* Create builder-style pojos ([#2](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/2))

## 0.1.1
* Automate all release tasks ([#48](https://api.github.com/repos/joelittlejohn/jsonschema2pojo/issues/48))

## 0.1.0
