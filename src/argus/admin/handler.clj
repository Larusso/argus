(ns argus.admin.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [argus.admin.controller :as controller]))

(defroutes app-routes
   controller/status-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
