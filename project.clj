(defproject argus "0.1.0-SNAPSHOT"
  :description "'argus' is a socket server that will watch and distribute file changes to connected clients"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [uochan/watchtower "0.1.4"]
                 [aleph "0.3.0"]
                 [com.taoensso/timbre "2.7.1"]
                 [org.clojure/tools.cli "0.2.4"]
                 [digest "1.4.3"]
                 [compojure "1.1.5"]
                 [ring "1.2.1"]
                 [hiccup "1.0.4"]
                 [org.clojars.runa/conjure "2.2.0"]]
  :plugins [[lein-ring "0.8.5"]]
  :main argus.main
  :aot [argus.main]
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}}
  :jvm-opts ["-Djava.net.preferIPv4Stack=true"])
