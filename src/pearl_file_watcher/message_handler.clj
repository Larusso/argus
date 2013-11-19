(ns pearl-file-watcher.message-handler
  (:require [lamina.core :as lamina]
            [lamina.viz :as viz]
            [taoensso.timbre :as timbre :refer (trace debug info warn fatal spy)]
            [digest]))

(def main-channel (lamina/channel))

(def client-1-in (lamina/channel))
(def client-1-out (lamina/channel))
(def client-2-in (lamina/channel))
(def client-2-out (lamina/channel))


(def path-1 "/User/larusso/work/home/lulu")
(def path-2 "/User/larusso/work/")

(defn has-equal-hash?
  [path-hash [id]]
  (spy :debug [path-hash id])
  (= id path-hash))

(defn close-channel-handler
  [ch]
  (info "channel closed")
  (lamina/ground ch))

(defn client-message-handler
  [ch [command path]]
  (let [filter-ch (->> main-channel
                       (lamina/fork)
                       (lamina/filter* (partial has-equal-hash? (digest/md5 path))))]
    (lamina/receive-all filter-ch #(lamina/enqueue client-1-out %))
    (lamina/on-closed ch (partial close-channel-handler filter-ch))))

(defn channel-connect
  [ch client-info]
  (lamina/receive-all ch
               (partial client-message-handler ch)))


(channel-connect client-1-in {})

(lamina/enqueue client-1-in ["r" path-2])


(lamina/enqueue main-channel [(digest/md5 path-2) "test-2"])
(lamina/enqueue main-channel [(digest/md5 path-1) "test-1"])

;;(lamina/enqueue client-1-in ["r" path-1])

(lamina/close client-1-in)

(lamina/enqueue main-channel [(digest/md5 path-2) "test-2"])
(lamina/enqueue main-channel [(digest/md5 path-1) "test-1"])
(lamina/enqueue main-channel [(digest/md5 path-2) "test-2"])

(viz/view-graph main-channel client-1-in client-1-out)

