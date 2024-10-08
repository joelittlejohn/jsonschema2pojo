(defproject jsonschema2pojo "0.1.0-SNAPSHOT"
  :description "Web front-end for jsonschema2pojo"
  :dependencies [[compojure "1.6.2"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/data.codec "0.1.1"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.jsonschema2pojo/jsonschema2pojo-core "1.2.2"]
                 [ring/ring-jetty-adapter "1.9.2"]
                 [ring/ring-ssl "0.3.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler jsonschema2pojo.server/app}
  :main jsonschema2pojo.server
  :aot [jsonschema2pojo.server]
  :min-lein-version "2.0.0")
