(ns argus.messaging
  (:require [lamina.core :as lamina :refer (map* filter* close ground receive-all)]
            [lamina.viz :as viz]
            [lamina.trace :as trace :refer (defn-instrumented)]
            [taoensso.timbre :as timbre :refer (debug info spy)]
            [digest]
            [clojure.string :refer (replace-first)]
            [clojure.pprint :refer :all])
  (:import [java.io File]
           [javax.imageio ImageIO]))

(declare main-channel remove-channel update-channel register-channel)

(defn push-hash
  [[- path source-channel :as message]]
  [(digest/md5 path) source-channel])

(defn register?
  [[command]]
  (= "r" command))

(defn update?
  [[command]]
  (= "u" command))

(defn remove?
  [[command]]
  (= "d" command))

(defn debug?
  [[command]]
  (.startsWith command "debug"))

(defn viz-debug-message
  [[command message source-channel]]
  (debug command)
  (debug message)
  (viz/view-propagation main-channel [(replace-first command "debug" "") message source-channel]))

(defn inform-change
  [ch message]
  (lamina/enqueue ch message))

(defn isPathEqual
  [hash1 [hash2]]
  (= hash1 hash2))

(defn is-source-channel-equal
  [channel1 [- channel2]]
  (= channel1 channel2))

(defn close-channels
  [channels & -]
  (doall
   (map close channels)))

(defn on-register
  [[path-hash source-channel]]
  (let [filter-fn (partial isPathEqual path-hash)
        channel-compare (partial is-source-channel-equal source-channel)
        remove-filter #(= (filter-fn %) (channel-compare %))
        filter-rm-ch (filter* remove-filter remove-channel)
        filter-up-ch (filter* filter-fn update-channel)]
    (ground remove-channel)
    (ground update-channel)
    (receive-all filter-up-ch (partial inform-change source-channel))
    (receive-all filter-rm-ch (partial close-channels [filter-up-ch filter-rm-ch]))
    (lamina/on-closed source-channel #(close-channels [filter-up-ch filter-rm-ch]))))

(defn setup-register-ch []
  (let [filter-ch (filter* register? main-channel)]
    (receive-all (map* push-hash filter-ch) on-register)
    filter-ch))

(def main-channel (lamina/channel))
(def remove-channel (map* push-hash (filter* remove? main-channel)))
(def update-channel (map* push-hash (filter* update? main-channel)))
(def register-channel (setup-register-ch))
(def debug-channel (filter* debug? main-channel))

(receive-all debug-channel viz-debug-message)

(defn channel-connect
  [ch client-info]
  (lamina/siphon (map* #(conj % ch) ch) main-channel))

(defn export-graph
  []
  (ImageIO/write (viz/render-graph main-channel) "png" (File. "resources/public/images/graph.png")))
