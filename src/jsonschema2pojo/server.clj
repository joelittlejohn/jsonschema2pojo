(ns jsonschema2pojo.server
  (:use [jsonschema2pojo.bridge]
        [compojure.core]
        [compojure.route :as route])
  (:import [java.io ByteArrayInputStream]
           [com.fasterxml.jackson.databind ObjectMapper]
           )
  (:gen-class))

(def object-mapper (ObjectMapper.))

(defroutes app

  (POST "/generator" {body :body}
        (try
          (let [zip-bytes (generate (.readTree object-mapper body))]
            {:status 200
             :headers {"Content-Type" "application/zip"}
             :body (ByteArrayInputStream. zip-bytes)})
          (catch Exception e
            {:status 500
             :headers {"Content-Type" "text/html"}
             :body "<h1>Internal Server Error</h1>"})))

  (route/not-found "<h1>Not Found</h1>"))
