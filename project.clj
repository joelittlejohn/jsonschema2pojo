(defproject jsonschema2pojo "0.1.0-SNAPSHOT"
  :description "Web front-end for jsonschema2pojo"
  :dependencies [[compojure "1.5.2"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.jsonschema2pojo/jsonschema2pojo-core "0.4.33"]
                 [ring/ring-jetty-adapter "1.5.1"]]
  :plugins [[lein-ring "0.11.0"]]
  :ring {:handler jsonschema2pojo.server/app}
  :main jsonschema2pojo.server
  :aot [jsonschema2pojo.server]
  :min-lein-version "2.0.0")
