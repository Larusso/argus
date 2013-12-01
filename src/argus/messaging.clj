(ns argus.messaging
  (:require [lamina.core :as lamina :refer (map* filter* close ground receive-all)]
            [lamina.viz :as viz]
            [lamina.trace :as trace :refer (defn-instrumented)]
            [taoensso.timbre :as timbre :refer (debug info spy)]
            [digest]
            [clojure.string :refer (replace-first)]
            [clojure.pprint :refer :all]
            [clojure.java.io :as io]
            [clojure.string :refer (replace-first)])
  (:import [java.io File]
           [java.nio ByteBuffer]
           [javax.imageio ImageIO]))

(declare main-channel remove-channel update-channel register-channel)
(declare export-graph export-propagation)

(def connected-clients (atom []))

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

(defn- slurp-binary-file! [^File file]
  (io! (with-open [reader (io/input-stream file)]
         (let [buffer (byte-array (.length file))]
           (.read reader buffer)
           buffer))))

(defn- prepare-message
  [id file-path]
  (let [file-content (slurp-binary-file! (io/as-file file-path))]
    ["u" id (vec file-content)]))

(defn- get-relative-path
  [root-paths changed-file]
  (when-let [root (filter #(.startsWith changed-file %) root-paths)]
    (replace-first changed-file (first root) "")))

(defn- create-change-message
  [root-files changed-file-path]
  (let [abs-path (.getAbsolutePath changed-file-path)
        relative-path (get-relative-path root-files abs-path)
        hash-value (digest/md5 relative-path)]
    (prepare-message relative-path abs-path)))

(defn files-changed [root-files changed-files]
  (info "files changed" changed-files)
  (let [messages (map #(create-change-message root-files %) changed-files)]
    (apply lamina/enqueue main-channel messages)
    (export-propagation (last messages))))

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

(defn channel-connect
  [ch client-info]
  ;;check if we can somehow join to channels so that the graph will no longer display a channel for each client
  (let [joined-ch ch]
    (lamina/siphon (map* #(conj % joined-ch) joined-ch) main-channel)
    (swap! connected-clients conj joined-ch))
    (export-graph))

(defn export-graph
  []
  (let [channels (conj @connected-clients main-channel)
        image-buffer (apply viz/render-graph channels)]
  (ImageIO/write image-buffer "png" (File. "resources/public/images/graph.png"))))

(defn export-propagation
  [message]
  (spy message)
  (let [image-buffer (viz/render-propagation main-channel message)]
    (ImageIO/write image-buffer "png" (File. "resources/public/images/propagation.png"))))
