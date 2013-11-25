(ns argus.admin.controller
  (:require [argus.admin.view :as view]
            [argus.messaging :as messaging]
            [compojure.core :refer (defroutes GET POST)]))

(defn index
  []
  (messaging/export-graph)
  (view/view-graph))

(defn graph
  []
  (messaging/export-graph)
  (view/view-graph))

(defn p
  [])

(defroutes routes
  (GET "/" [] (index)))
