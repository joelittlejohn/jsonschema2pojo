(defproject jsonschema2pojo "0.1.0-SNAPSHOT"
  :description "Web front-end for jsonschema2pojo"
  :dependencies [[compojure "1.6.2"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/data.codec "0.1.1"]
                 [org.clojure/tools.logging "0.6.0"]
                 [org.jsonschema2pojo/jsonschema2pojo-core "0.5.1"]
                 [ring/ring-jetty-adapter "1.8.2"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler jsonschema2pojo.server/app}
  :main jsonschema2pojo.server
  :aot [jsonschema2pojo.server]
  :min-lein-version "2.0.0")
