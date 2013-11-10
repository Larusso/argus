(ns pearl-file-watcher.core
  (:require [watchtower.core :refer :all]
            [lamina.core :refer :all]
            [aleph.tcp :refer :all]
            [gloss.core :refer :all]
            [taoensso.timbre :as timbre :refer (trace debug info warn fatal spy with-log-level)])
  (:gen-class))

(def main-channel (channel))
(def server-port 9000)

(defn files-changed [files]
  (info "files changed")
  (spy :info files)
  (doall
   (map #(enqueue main-channel (.getPath %)) files)))

(defn handler [channel client-info]
  (info "channel connected")
  (siphon main-channel channel))

(def log-file (str (System/getenv "HOME") "/Library/Logs/pearl_watcher.log"))

(defn initLogger
  []
  (timbre/set-config! [:appenders :spit :enabled?] true)
  (timbre/set-config! [:shared-appender-config :spit-filename] log-file))


(defn -main
  [& args]
  (initLogger)
  (info "start watcher")
  (start-tcp-server handler {:port server-port, :frame (string :utf-8 :delimiters ["\r\n"])})
  (watcher ["resources/"]
           (rate 200)
           (file-filter ignore-dotfiles)
           (file-filter (extensions :json))
           (on-change files-changed)))

