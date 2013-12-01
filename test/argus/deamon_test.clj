(ns argus.deamon-test
  (:require [clojure.test :refer :all]
            [argus.deamon :refer :all]
            [conjure.core :refer :all]))

(deftest deomanize
  (stubbing [get-pid-file nil]
            (daemonize)
            ))


(run-tests)
