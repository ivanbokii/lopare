(defproject lopare "0.1.0-SNAPSHOT" :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [sonian/carica "1.1.0"]
                 [im.chit/cronj "1.4.1"]
                 [org.clojure/data.json "0.2.6"]
                 [com.taoensso/timbre "3.4.0"]
                 [com.climate/java.shell2 "0.1.0"]]
  :main ^:skip-aot lopare.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]}}
  :uberjar-name "lopare.jar")
