(ns jsonschema2pojo.bridge
  (:require [clojure.string :as str])
  (:import com.sun.codemodel.JCodeModel
           [com.sun.codemodel.writer SingleStreamCodeWriter ZipCodeWriter]
           java.io.ByteArrayOutputStream
           [org.jsonschema2pojo AnnotationStyle AnnotatorFactory DefaultGenerationConfig Schema SchemaGenerator ScalaSingleStreamCodeWriter ScalaZipCodeWriter SchemaStore SourceType Language]
           com.fasterxml.jackson.dataformat.yaml.YAMLFactory
           org.jsonschema2pojo.ContentResolver
           org.jsonschema2pojo.rules.RuleFactory))

(defn- content-resolver
  [config]
  (if (= (.getSourceType config) SourceType/YAMLSCHEMA)
    (ContentResolver. (YAMLFactory.))
    (ContentResolver.)))

(defn- output-to-zip
  [config code-model]
  (let [zip-bytes (ByteArrayOutputStream.)]
    (with-open [writer (if (= Language/SCALA (.getTargetLanguage config)) (ScalaZipCodeWriter. zip-bytes) (ZipCodeWriter. zip-bytes))]
      (.build code-model writer))
    (.toByteArray zip-bytes)))

(defn- output-to-string
  [config code-model]
  (let [code-as-bytes (ByteArrayOutputStream.)]
    (with-open [writer (if (= Language/SCALA (.getTargetLanguage config)) (ScalaSingleStreamCodeWriter. code-as-bytes) (SingleStreamCodeWriter. code-as-bytes))]
      (.build code-model writer))
    (.toByteArray code-as-bytes)))

(defn- annotator
  [config]
  (.. (AnnotatorFactory. config)
      (getAnnotator (.getAnnotationStyle config))))

(defn- generate-java-types
  [input classname config code-model]
  (let [package (._package code-model (.getTargetPackage config))
        schema (if (or (= (.getSourceType config) SourceType/JSON) (= (.getSourceType config) SourceType/YAML))
                 (.schemaFromExample (SchemaGenerator.) input)
                 input)]
    (.. (RuleFactory. config (annotator config) (SchemaStore. (content-resolver config)))
        (getSchemaRule)
        (apply classname schema package (proxy [Schema] [nil schema schema])))))

(defn params-based-config
  [params]
  (proxy [DefaultGenerationConfig] []
    (getTargetPackage []
      (params "targetpackage"))
    (getTargetLanguage []
      (if (contains? params "targetlanguage")
        (Language/valueOf (str/upper-case (params "targetlanguage")))
        (proxy-super getTargetLanguage)))
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
        (AnnotationStyle/valueOf (str/upper-case (params "annotationstyle")))
        (proxy-super getAnnotationStyle)))
    (getSourceType []
      (if (contains? params "sourcetype")
        (SourceType/valueOf (str/upper-case (params "sourcetype")))
        (proxy-super getSourceType)))))

(defn generate
  [schema classname config]
  (let [code-model (JCodeModel.)]
    (generate-java-types schema classname config code-model)
    (output-to-zip config code-model)))

(defn preview
  [schema classname config]
  (let [code-model (JCodeModel.)]
    (generate-java-types schema classname config code-model)
    (output-to-string config code-model)))
