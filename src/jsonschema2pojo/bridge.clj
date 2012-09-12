(ns jsonschema2pojo.bridge
  (:use [clojure.java.io]
        [clojure.string :only [upper-case]])
  (:import [java.io ByteArrayOutputStream]
           [com.googlecode.jsonschema2pojo Schema SchemaGenerator SchemaStore]
           [com.googlecode.jsonschema2pojo AnnotatorFactory AnnotationStyle GenerationConfig SourceType DefaultGenerationConfig]
           [com.googlecode.jsonschema2pojo.rules RuleFactory]
           [com.sun.codemodel JCodeModel]
           [com.sun.codemodel.writer SingleStreamCodeWriter ZipCodeWriter]))

(defn- output-to-zip [code-model]
  (let [zip-bytes (ByteArrayOutputStream.)]
    (with-open [writer (ZipCodeWriter. zip-bytes)]
      (.build code-model writer))
    (.toByteArray zip-bytes)))

(defn- annotator [config]
  (.. (AnnotatorFactory.)
      (getAnnotator (.getAnnotationStyle config))))

(defn- generate-java-types [schema classname config code-model]
  (let [package (._package code-model (.getTargetPackage config))]
    (.. (RuleFactory. config (annotator config) (SchemaStore.))
        (getSchemaRule)
        (apply classname schema package (proxy [Schema] [nil schema])))))

(def ^:private default-config
  (DefaultGenerationConfig.))

(defn json-based-config [node]
  (proxy [GenerationConfig] []
    (isGenerateBuilders []
      (if (.has node "generatebuilders")
        (.asBoolean (.get node "generatebuilders"))
        (.isGenerateBuilders default-config)))
    (isUsePrimitives []
      (if (.has node "useprimitives")
        (.asBoolean (.get node "useprimitives"))
        (.isUsePrimitives default-config)))
    (getTargetPackage []
      (.asText (.get node "targetpackage")))
    (getPropertyWordDelimiters []
      (if (.has node "useprimitives")
        (char-array (.asText (.get node "propertyworddelimiters")))
        (.getPropertyWordDelimiters default-config)))
    (isUseLongIntegers []
      (if (.has node "uselongintegers")
        (.asBoolean (.get node "uselongintegers"))
        (.isUseLongIntegers default-config)))
    (isIncludeHashcodeAndEquals []
      (if (.has node "includehashcodeandequals")
        (.asBoolean (.get node "includehashcodeandequals"))
        (.isIncludeHashcodeAndEquals default-config)))
    (isIncludeToString []
      (if (.has node "includetostring")
        (.asBoolean (.get node "includetostring"))
        (.isIncludeToString default-config)))
    (getAnnotationStyle []
      (if (.has node "annotationStyle")
        (AnnotationStyle/valueOf (upper-case (.asText (.get node "annotationstyle"))))
        (.getAnnotationStyle default-config)))
    (isIncludeJsr303Annotations []
      (if (.has node "includejsr303annotations")
        (.asBoolean (.get node "includejsr303annotations"))
        (.isIncludeJsr303Annotations default-config)))
    (getSourceType []
      (if (.has node "sourcetype")
        (SourceType/valueOf (upper-case (.asText (.get node "sourcetype"))))
        (.getSourceType default-config)))))

(defn generate
  ([schema-and-config]
     (generate (.get schema-and-config "schema")
               (.asText (.get schema-and-config "classname"))
               (json-based-config schema-and-config)))
  ([schema classname config]
     (let [code-model (JCodeModel.)]
       (generate-java-types schema classname config code-model)
       (output-to-zip code-model))))
