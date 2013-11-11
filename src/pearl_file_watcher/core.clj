(ns pearl-file-watcher.core
  (:require [watchtower.core :refer :all]
            [lamina.core :refer :all]
            [aleph.tcp :refer :all]
            [gloss.core :refer :all]
            [clojure.tools.cli :refer (cli)]
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

(defn initLogger
  [path]
  (timbre/set-config! [:appenders :spit :enabled?] true)
  (timbre/set-config! [:shared-appender-config :spit-filename] path))

(defn init-watcher
  [{host :host
    port :port
    check-rate :rate :as p} files]
  (info "start watcher")
  (spy :debug p)
  (start-tcp-server handler {:host host :port port, :frame (string :utf-8 :delimiters ["\r\n"])})
  (watcher files
           (rate check-rate)
           (file-filter ignore-dotfiles)
           (file-filter (extensions :json :txt :swf))
           (on-change files-changed)))


(defn -main
  [& args]
  (let [[options args banner] (cli args "pearl file watcher"
    ["--host" "The hostname" :default "localhost"]
    ["-p" "--port" "Listen on this port" :parse-fn #(Integer. %) :default 9000]
    ["-r" "--rate" "Delay between samples" :parse-fn #(Integer. %) :default 100]
    ["-h" "--help" "Show help" :default false :flag true]
    ["--log" "path for log output" :default (str (System/getenv "HOME") "/Library/Logs/pearl_watcher.log")])
        usage (clojure.string/replace banner #"Usage:" "usage: modler [options] [model PATH]")]

    (when (:help options)
      (println banner)
      (System/exit 0))
    (initLogger (:log options))
    (init-watcher options args)))

