(defproject
  kinesis-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cheshire "5.4.0"]
                 [com.stuartsierra/component "0.2.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [amazonica "0.3.19"
                  :exclusions [commons-codec joda-time]]
                 [joda-time "2.7"]
                 [clj-pid "0.1.2"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [environ "1.0.0"]
                 [capacitor "0.4.2"]
                 [clj-time "0.8.0"]]
  :plugins [[lein-environ "1.0.0"]]
  :main ^:skip-aot kinesis-client.main
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
