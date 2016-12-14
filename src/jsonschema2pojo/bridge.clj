(ns jsonschema2pojo.bridge
  (:use [clojure.java.io]
        [clojure.string :only [upper-case]])
  (:import [java.io ByteArrayOutputStream]
           [org.jsonschema2pojo
            Schema SchemaGenerator SchemaStore
            GenerationConfig DefaultGenerationConfig
            AnnotatorFactory AnnotationStyle SourceType ]
           [org.jsonschema2pojo.rules RuleFactory]
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
  (.. (AnnotatorFactory. config)
      (getAnnotator (.getAnnotationStyle config))))

(defn- generate-java-types [input classname config code-model]
  (let [package (._package code-model (.getTargetPackage config))
        schema (if (= (.getSourceType config) (SourceType/JSON)) (.schemaFromExample schema-generator input) input)]
    (.. (RuleFactory. config (annotator config) (SchemaStore.))
        (getSchemaRule)
        (apply classname schema package (proxy [Schema] [nil schema schema])))))

(defn params-based-config [params]
  (proxy [DefaultGenerationConfig] []
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
    (isUseJodaDates []
      (boolean (Boolean/valueOf (params "usejodadates"))))
    (isUseDoubleNumbers []
      (boolean (Boolean/valueOf (params "usedoublenumbers"))))
    (isUseCommonsLang3 []
      (boolean (Boolean/valueOf (params "usecommonslang3"))))
    (isIncludeAccessors []
      (boolean (Boolean/valueOf (params "includeaccessors"))))
    (isIncludeAdditionalProperties []
      (boolean (Boolean/valueOf (params "includeadditionalproperties"))))
    (isIncludeConstructors []
      (boolean (Boolean/valueOf (params "includeconstructors"))))
    (isIncludeDynamicAccessors []
      (boolean (Boolean/valueOf (params "includedynamicaccessors"))))
    (isParcelable []
      (boolean (Boolean/valueOf (params "parcelable"))))
    (isSerializable []
      (boolean (Boolean/valueOf (params "serializable"))))
    (isInitializeCollections []
      (boolean (Boolean/valueOf (params "initializecollections"))))
    (getPropertyWordDelimiters []
      (char-array (params "propertyworddelimiters")))
    (getAnnotationStyle []
      (if (contains? params "annotationstyle")
        (AnnotationStyle/valueOf (upper-case (params "annotationstyle")))
        (proxy-super getAnnotationStyle)))
    (getSourceType []
      (if (contains? params "sourcetype")
        (SourceType/valueOf (upper-case (params "sourcetype")))
        (proxy-super getSourceType)))))

(defn generate [schema classname config]
  (let [code-model (JCodeModel.)]
    (generate-java-types schema classname config code-model)
    (output-to-zip code-model)))

(defn preview [schema classname config]
  (let [code-model (JCodeModel.)]
    (generate-java-types schema classname config code-model)
    (output-to-string code-model)))
