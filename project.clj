(defproject jsonschema2pojo "0.1.0-SNAPSHOT"
            :description "Web front-end for jsonschema2pojo"
            :dependencies [[org.clojure/clojure "1.7.0"]
                           [org.clojure/data.codec "0.1.0"]
                           [org.clojure/tools.logging "0.3.1"]
                           [compojure "1.5.1"]
                           [ring/ring-jetty-adapter "1.5.0"]
                           [org.jsonschema2pojo/jsonschema2pojo-core "0.4.27"]]
            :plugins [[lein-ring "0.10.0"]]
            :ring {:handler jsonschema2pojo.server/app}
            :main jsonschema2pojo.server
            :min-lein-version "2.0.0")
