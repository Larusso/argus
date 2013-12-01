(ns argus.main
  (:require [argus.deamon :as deamon]
            [argus.core :as core]
            [clojure.tools.cli :refer (cli)]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn initLogger
  [path]
  (timbre/set-config! [:appenders :spit :enabled?] true)
  (timbre/set-config! [:shared-appender-config :spit-filename] path))

(defn -main
  [& args]
  (let [[options args banner] (cli args "pearl file watcher"
    ["--host" "The hostname" :default "localhost"]
    ["-p" "--port" "Listen on this port" :parse-fn #(Integer. %) :default 9000]
    ["-r" "--rate" "Delay between samples" :parse-fn #(Integer. %) :default 100]
    ["-h" "--help" "Show help" :default false :flag true]
    ["-d" "--deamon" "Start server as deamon" :default false :flag true]
    ["--log" "path for log output" :default (str (System/getenv "HOME") "/Library/Logs/pearl_watcher.log")]
    ["-a" "--admin" "Start admin http server along with socket server" :default false :flag true]
    ["-ap" "--admin-port" "port for http admin server" :default 8080]
    ["-ah" "--admin-host" "host to server http admin server" :default "localhost"])
        usage (clojure.string/replace banner #"Usage:" "usage: modler [options] [model PATH]")]

    (when (:help options)
      (println banner)
      (System/exit 0))

    (if (:deamon options)
      (deamon/daemonize))

    (initLogger (:log options))
    (core/start-socket options)
    (core/start-watcher (:rate options) args)

    (if (:admin options)
      (core/start-status options))))
