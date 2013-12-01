(ns argus.core
  (:require [argus.messaging :as messaging]
            [argus.admin.handler :as admin]
            [watchtower.core :as watchtower]
            [aleph.tcp :refer :all]
            [gloss.core :refer :all]
            [ring.adapter.jetty :refer :all]
            [taoensso.timbre :as timbre :refer (trace debug info warn fatal spy with-log-level)])
  (:gen-class))

(def encoder [(string :utf-8 :length 32) (repeated :byte :prefix :int32)])
(def decoder [(string :utf-8 :delimiters [":"]) (string :utf-8 :delimiters ["\r\n"])])

(defn start-socket
  [{host :host
    port :port :as p}]
  (info "start socket")
  (start-tcp-server messaging/channel-connect {:host host :port port :encoder encoder  :decoder decoder}))

(defn start-watcher
  [check-rate files]
  (info "start-watcher" [check-rate files])
  (watchtower/watcher files
           (watchtower/rate check-rate)
           (watchtower/file-filter watchtower/ignore-dotfiles)
           (watchtower/file-filter (watchtower/extensions :json :txt :swf))
           (watchtower/on-change #(messaging/files-changed files %))))

(defn start-status
  [{host :admin-host
    port :admin-port}]
  (info "start status server")
  (run-jetty (var admin/app) {:port port :host host :join? false}))
