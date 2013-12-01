(ns argus.deamon)

(defn get-pid-file
  []
  (clojure.java.io/file (System/getProperty "daemon.pidfile")))

(defn daemonize
  []
  (when-let [pid-file (get-pid-file)]
    (.deleteOnExit pid-file))
  (.close System/out)
  (.close System/err))
