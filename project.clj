(defproject exoscale/automata "0.1.10-SNAPSHOT"
  :description "Data-based moore machines in Clojure"
  :url "https://github.com/exoscale/automata"
  :license {:name "ISC"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :deploy-repositories [["snapshots" :clojars] ["releases" :clojars]]
  :pedantic? :abort
  :profiles {:dev {:dependencies [[lambdaisland/kaocha    "0.0-554"]
                                  [org.clojure/test.check "0.10.0"]]
                   :pedantic?    :warn
                   :aliases      {"kaocha" ["with-profile" "+dev" "run" "-m" "kaocha.runner"]}}})
