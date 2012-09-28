(ns jsonschema2pojo.server
  (:use [jsonschema2pojo.bridge]
        [clojure.tools.logging :only [error]]
        [compojure.core :only [defroutes GET POST]]
        [compojure.route :only [not-found resources]]
        [ring.middleware.params]
        [ring.util.response :only [resource-response]]
        [ring.adapter.jetty :only [run-jetty]])
  (:import [java.io ByteArrayInputStream]
           [com.fasterxml.jackson.databind ObjectMapper])
  (:gen-class))

(def object-mapper (ObjectMapper.))

(defn parse [schema]
  (try
    (.readTree object-mapper schema)
    (catch Exception e (throw (IllegalArgumentException. (.getMessage e))))))

(defn not-blank [params name]
  (if (empty? (params name))
    (throw (IllegalArgumentException. (str name " cannot be blank")))
    (params name)))

(defroutes routes

  (POST "/generator/:name" {params :params}
        (try
          (let [schema (parse (params "schema"))
                classname (not-blank params "classname")
                targetpackage (not-blank params "targetpackage")
                config (post-params-based-config params)
                zip-bytes (generate schema classname config)]
            {:status 200
             :headers {"Content-Type" "application/zip"}
             :body (ByteArrayInputStream. zip-bytes)})
          (catch IllegalArgumentException e
            {:status 400
             :headers {"Content-Type" "text/html"}
             :body (str "<h1>Bad Request</h1>" (.getMessage e))})
          (catch Exception e
            (error "Failed to generate schema" e)
            {:status 500
             :headers {"Content-Type" "text/html"}
             :body "<h1>Internal Server Error</h1>"})))

  (GET "/" {} (resource-response "public/index.html"))

  (resources "/lib" {:root "public/lib"})

  (not-found "Not found"))

(def app (wrap-params routes))

(defn -main [port]
  (run-jetty app {:port (Integer. port)}))
