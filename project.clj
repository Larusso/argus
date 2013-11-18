(defproject pearl-file-watcher "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [uochan/watchtower "0.1.4"]
                 [aleph "0.3.0"]
                 [com.taoensso/timbre "2.7.1"]
                 [org.clojure/tools.cli "0.2.4"]
                 [digest "1.4.3"]]
  :main pearl-file-watcher.core
  :aot [pearl-file-watcher.core]
  :jvm-opts ["-Djava.net.preferIPv4Stack=true"])
