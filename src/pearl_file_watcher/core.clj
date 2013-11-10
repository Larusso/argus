(ns pearl-file-watcher.core
  (:require [watchtower.core :refer :all]
            [lamina.core :refer :all]
            [aleph.tcp :refer :all]
            [gloss.core :refer :all]))

(def main-channel (channel))
(def server-port 9000)

(defn files-changed [files]
  (doall
   (map #(enqueue main-channel (.getAbsolutePath %)) files)))

(defn handler [channel client-info]
  (siphon main-channel channel))


(defn -main []
  (start-tcp-server handler {:port server-port, :frame (string :utf-8 :delimiters ["\r\n"])})
  (watcher ["resources/"]
           (rate 200)
           (file-filter ignore-dotfiles)
           (file-filter (extensions :json))
           (on-change files-changed)))

