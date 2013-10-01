(ns jsonschema2pojo.bridge
  (:use [clojure.java.io]
        [clojure.string :only [upper-case]])
  (:import [java.io ByteArrayOutputStream]
           [com.googlecode.jsonschema2pojo
            Schema SchemaGenerator SchemaStore
            GenerationConfig DefaultGenerationConfig
            AnnotatorFactory AnnotationStyle SourceType ]
           [com.googlecode.jsonschema2pojo.rules RuleFactory]
           [com.sun.codemodel JCodeModel]
           [com.sun.codemodel.writer SingleStreamCodeWriter ZipCodeWriter]))

(def ^:private schema-generator (SchemaGenerator.))

(defn- output-to-zip [code-model]
  (let [zip-bytes (ByteArrayOutputStream.)]
    (with-open [writer (ZipCodeWriter. zip-bytes)]
      (.build code-model writer))
    (.toByteArray zip-bytes)))

(defn- output-to-string [code-model]
  (let [code-as-bytes (ByteArrayOutputStream.)]
    (with-open [writer (SingleStreamCodeWriter. code-as-bytes)]
      (.build code-model writer))
    (.toByteArray code-as-bytes)))

(defn- annotator [config]
  (.. (AnnotatorFactory.)
      (getAnnotator (.getAnnotationStyle config))))

(defn- generate-java-types [input classname config code-model]
  (let [package (._package code-model (.getTargetPackage config))
        schema (if (= (.getSourceType config) (SourceType/JSON)) (.schemaFromExample schema-generator input) input)]
    (.. (RuleFactory. config (annotator config) (SchemaStore.))
        (getSchemaRule)
        (apply classname schema package (proxy [Schema] [nil schema])))))

(def ^:private default-config
  (DefaultGenerationConfig.))

(defn params-based-config [params]
  (proxy [GenerationConfig] []
    (getTargetPackage []
      (params "targetpackage"))
    (isGenerateBuilders []
      (boolean (Boolean/valueOf (params "generatebuilders"))))
    (isUsePrimitives []
      (boolean (Boolean/valueOf (params "useprimitives"))))
    (isUseLongIntegers []
      (boolean (Boolean/valueOf (params "uselongintegers"))))
    (isIncludeHashcodeAndEquals []
      (boolean (Boolean/valueOf (params "includehashcodeandequals"))))
    (isIncludeToString []
      (boolean (Boolean/valueOf (params "includetostring"))))
    (isIncludeJsr303Annotations []
      (boolean (Boolean/valueOf (params "includejsr303annotations"))))
    (getPropertyWordDelimiters []
      (char-array (params "propertyworddelimiters")))
    (getAnnotationStyle []
      (if (contains? params "annotationstyle")
        (AnnotationStyle/valueOf (upper-case (params "annotationstyle")))
        (.getAnnotationStyle default-config)))
    (getSourceType []
      (if (contains? params "sourcetype")
        (SourceType/valueOf (upper-case (params "sourcetype")))
        (.getSourceType default-config)))))

(defn generate [schema classname config]
  (let [code-model (JCodeModel.)]
    (generate-java-types schema classname config code-model)
    (output-to-zip code-model)))

(defn preview [schema classname config]
  (let [code-model (JCodeModel.)]
    (generate-java-types schema classname config code-model)
    (output-to-string code-model)))
