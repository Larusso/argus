(ns argus.admin.controller
  (:require [argus.admin.view :as view]
            [argus.messaging :as messaging]
            [compojure.core :refer :all]))

(defn index
  []
  (view/view-graph))

(defroutes status-routes
  (GET "/" [] (index)))
