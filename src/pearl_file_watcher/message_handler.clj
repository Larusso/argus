(ns pearl-file-watcher.message-handler
  (:require [lamina.core :as lamina]
            [lamina.viz :as viz]
            [lamina.trace :as trace :refer (defn-instrumented)]
            [taoensso.timbre :as timbre :refer (debug info spy)]
            [digest]
            [clojure.pprint :refer :all]))

(def main-channel (lamina/channel))

(defn has-equal-hash?
  [path-hash [id]]
  (= id path-hash))

(defn-instrumented close-channel-handler
  {:probes {:return (lamina/sink->> pprint)} :capture :in-out}
  [ch]
  (lamina/close ch))

(defn-instrumented inform-change
  {:probes {:return (lamina/sink->> pprint)} :capture :in-out}
  [ch message]
  (lamina/enqueue ch message)
  (viz/view-graph ch main-channel))

(defn-instrumented client-message-handler
  {:probes {:return (lamina/sink->> pprint)} :capture :in-out}
  [ch [command path]]
  (let [filter-ch (->> main-channel
                       (lamina/filter* (partial has-equal-hash? (digest/md5 path))))]
    (lamina/ground main-channel)
    (lamina/receive-all filter-ch (partial inform-change ch))
    (lamina/on-closed ch (partial close-channel-handler filter-ch))))

(defn-instrumented channel-connect
  {:probes {:return (lamina/sink->> pprint)} :capture :in-out}
  [ch client-info]
  (lamina/receive-all ch
               (partial client-message-handler ch)))
