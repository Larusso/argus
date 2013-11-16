(ns pearl-file-watcher.message-encoder
  (:require [clojure.java.io :as io])
  (:import [java.nio ByteBuffer]
           [java.io File]))

(defn slurp-binary-file! [^File file]
  (io! (with-open [reader (io/input-stream file)]
         (let [buffer (byte-array (.length file))]
           (.read reader buffer)
           buffer))))


(defn prepare-message
  [id file-path]
  (let [file-content (slurp-binary-file! (io/as-file file-path))]
    [id (vec file-content)]))
