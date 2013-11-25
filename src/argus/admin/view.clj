(ns argus.admin.view
  (:require [hiccup.core :refer (html)]
            [hiccup.page :refer (html5 include-css include-js)]))


(defn common [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:title title]
    [:script {:type "text/javascript", :src "http://localhost:55445/socket.io/lighttable/ws.js" :id "lt_ws"}]
    (include-css "css/bootstrap.min.css")]
   [:body
    [:div {:id "content" :class "container"} body]
    (include-js "js/bootstrap.min.js")]))

(defn view-graph
  []
  (common "Graph" [:img {:src "images/graph.png"}]))

(view-graph)
