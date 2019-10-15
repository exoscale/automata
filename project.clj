(defproject exoscale/automata "0.1.6-SNAPSHOT"
  :description "Data-based moore machines in Clojure"
  :url "https://github.com/exoscale/automata"
  :license {:name "ISC"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :deploy-repositories [["snapshots" :clojars] ["releases" :clojars]]
  :pedantic? :abort
  :profiles {:dev {:plugins      [[lein-codox "0.10.7"]]
                   :dependencies [[lambdaisland/kaocha    "0.0-554"]
                                  [org.clojure/test.check "0.10.0"]]
                   :pedantic?    :warn
                   :aliases      {"kaocha" ["with-profile" "+dev" "run" "-m" "kaocha.runner"]}
                   :codox        {:source-uri "https://github.com/exoscale/automata/blob/{version}/{filepath}#L{line}"
                                  :doc-files  ["README.md"]
                                  :metadata   {:doc/format "markdown"}}}})
