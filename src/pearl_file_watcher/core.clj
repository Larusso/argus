(ns pearl-file-watcher.core
  (:require [pearl-file-watcher.message-encoder :as m-encoder]
            [watchtower.core :refer :all]
            [lamina.core :refer :all]
            [aleph.tcp :refer :all]
            [gloss.core :refer :all]
            [clojure.tools.cli :refer (cli)]
            [clojure.string :refer (replace-first)]
            [taoensso.timbre :as timbre :refer (trace debug info warn fatal spy with-log-level)])
  (:gen-class))

(def main-channel (channel))
(def server-port 9000)

(defn starts-with?
  [name check]
  (.startsWith name check))

(defn get-relative-path
  [root-paths changed-file]
  (when-let [root (filter (partial starts-with? changed-file) root-paths)]
    (replace-first changed-file (first root) "")))

(defn files-changed [root-files changed-files]
  (info "files changed" changed-files)
  ;;(enqueue main-channel m-encoder/prepare-message 12 ))
  (apply enqueue main-channel (map #(m-encoder/prepare-message 12 (.getAbsolutePath %)) changed-files)))

(defn handler [channel client-info]
  (info "channel connected")
  (receive-all channel println)
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
  (spy :debug files)
  (start-tcp-server handler {:host host :port port :encoder [:uint32 (repeated :byte :prefix :int32)] :decoder (string :utf-8 :delimiters ["\r\n"])})
  (watcher files
           (rate check-rate)
           (file-filter ignore-dotfiles)
           (file-filter (extensions :json :txt :swf))
           (on-change (partial files-changed files))))

(defn get-pid-file
  []
  (clojure.java.io/file (System/getProperty "daemon.pidfile")))

(defn daemonize
  []
  (when-let [pid-file (get-pid-file)]
    (.deleteOnExit pid-file))
  (.close System/out)
  (.close System/err))

(defn -main
  [& args]
  (let [[options args banner] (cli args "pearl file watcher"
    ["--host" "The hostname" :default "localhost"]
    ["-p" "--port" "Listen on this port" :parse-fn #(Integer. %) :default 9000]
    ["-r" "--rate" "Delay between samples" :parse-fn #(Integer. %) :default 100]
    ["-h" "--help" "Show help" :default false :flag true]
    ["-d" "--deamon" "Start server as deamon" :default false :flag true]
    ["--log" "path for log output" :default (str (System/getenv "HOME") "/Library/Logs/pearl_watcher.log")])
        usage (clojure.string/replace banner #"Usage:" "usage: modler [options] [model PATH]")]

    (when (:help options)
      (println banner)
      (System/exit 0))

    (if (:deamon options)
      (daemonize))
    (initLogger (:log options))
    (init-watcher options args)))

