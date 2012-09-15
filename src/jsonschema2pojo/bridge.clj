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

(defn post-params-based-config [params]
  (proxy [GenerationConfig] []
    (isGenerateBuilders []
      (if (contains? params "generatebuilders")
        (boolean (Boolean/valueOf (params "generatebuilders")))
        (.isGenerateBuilders default-config)))
    (isUsePrimitives []
      (if (contains? params "useprimitives")
        (boolean (Boolean/valueOf (params "useprimitives")))
        (.isUsePrimitives default-config)))
    (getTargetPackage []
      (params "targetpackage"))
    (getPropertyWordDelimiters []
      (if (contains? params "propertyworddelimiters")
        (char-array (params "propertyworddelimiters"))
        (.getPropertyWordDelimiters default-config)))
    (isUseLongIntegers []
      (if (contains? params "uselongintegers")
        (boolean (Boolean/valueOf (params "uselongintegers")))
        (.isUseLongIntegers default-config)))
    (isIncludeHashcodeAndEquals []
      (if (contains? params "includehashcodeandequals")
        (boolean (Boolean/valueOf (params "includehashcodeandequals")))
        (.isIncludeHashcodeAndEquals default-config)))
    (isIncludeToString []
      (if (contains? params "includetostring")
        (boolean (Boolean/valueOf (params "includetostring")))
        (.isIncludeToString default-config)))
    (getAnnotationStyle []
      (if (contains? params "annotationStyle")
        (AnnotationStyle/valueOf (upper-case (params "annotationstyle")))
        (.getAnnotationStyle default-config)))
    (isIncludeJsr303Annotations []
      (if (contains? "includejsr303annotations")
        (boolean (Boolean/valueOf (params "includejsr303annotations")))
        (.isIncludeJsr303Annotations default-config)))
    (getSourceType []
      (if (contains? params "sourcetype")
        (SourceType/valueOf (upper-case (params "sourcetype")))
        (.getSourceType default-config)))))

(defn generate
  ([schema classname config]
     (let [code-model (JCodeModel.)]
       (generate-java-types schema classname config code-model)
       (output-to-zip code-model))))
