(defproject jsonschema2pojo "0.1.0-SNAPSHOT"
            :description "Web front-end for jsonschema2pojo"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [compojure "1.1.3"]
                           [com.googlecode.jsonschema2pojo/jsonschema2pojo-core "0.3.3"]]
            :plugins [[lein-ring "0.7.5"]]
            :ring {:handler jsonschema2pojo.server/app}
            :main jsonschema2pojo.server)
