(ns jsonschema2pojo.server
  (:gen-class)
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.string :refer [split-lines]]
            [clojure.tools.logging :refer [error]]
            [compojure
             [core :refer [defroutes GET POST]]
             [route :refer [not-found resources]]]
            [jsonschema2pojo.bridge :as j2p]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware
             [cookies :refer [wrap-cookies]]
             [params :refer [wrap-params]]]
            [ring.util.response :refer [resource-response]])
  (:import com.fasterxml.jackson.core.JsonProcessingException
           com.fasterxml.jackson.databind.ObjectMapper
           java.io.ByteArrayInputStream))

(def object-mapper (ObjectMapper.))

(defn- format-parse-error
  [e]
  (let [message  (-> (.getMessage e) split-lines first)
        location (.getLocation e)
        line (.getLineNr location)
        column (.getColumnNr location)]
    (str message " (line " line ", column " column ")")))

(defn- parse
  [schema]
  (try
    (.readTree object-mapper schema)
    (catch JsonProcessingException e
      (throw (IllegalArgumentException. (format-parse-error e))))))

(defn- not-blank
  [params k name]
  (if (empty? (params k))
    (throw (IllegalArgumentException. (str name " can't be blank, try adding some text")))
    (params k)))

(defn- size-limit
  [limit s]
  (if (> (.length s) limit)
    (throw (IllegalArgumentException. (str "Your input was larger than " limit " characters, try making this a bit smaller")))
    s))

(defn- generate-response
  [params generator content-type]
  (try
    (let [schema (parse (size-limit 51200 (not-blank params "schema" "JSON Schema (or example JSON)")))
          classname (size-limit 128 (not-blank params "classname" "Class name"))
          targetpackage (size-limit 256 (not-blank params "targetpackage" "Package"))
          config (j2p/params-based-config params)
          code-bytes (generator schema classname config)]
      {:status 200
       :headers {"Content-Type" content-type}
       :body (ByteArrayInputStream. code-bytes)})
    (catch IllegalArgumentException e
      {:status 400
       :headers {"Content-Type" "text/html"}
       :body (str (.getMessage e))})
    (catch Exception e
      (error e "Failed to generate schema")
      {:status 500
       :headers {"Content-Type" "text/html"}
       :body "Internal server error :("})))

(defroutes routes

  (POST "/generator" {params :params}
        (generate-response params (comp b64/encode j2p/generate) "application/octet-stream"))

  (POST "/generator/preview" {params :params}
        (generate-response params j2p/preview "text/java"))

  (GET "/" {}
       (resource-response "public/index.html"))

  (resources "/")

  (not-found "Not found"))

(def app (wrap-params routes))

(defn -main
  [port]
  (run-jetty app {:port (Integer. port)}))
