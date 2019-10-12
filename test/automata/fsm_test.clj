(ns automata.fsm-test
  (:require [automata.fsm                    :as fsm]
            [clojure.spec.alpha              :as s]
            [clojure.test.check              :as tc]
            [clojure.test.check.generators   :as gen]
            [clojure.test.check.properties   :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test                    :refer :all]))

(defspec extract-test
  100000
  (prop/for-all
   [kw gen/keyword]
   (and (= kw (fsm/extract kw ::event))
        (= kw (fsm/extract {::event kw} ::event))
        (= ::foo (fsm/extract {kw ::foo} kw)))))

(def resource-rules
  {:init     [{::fsm/event :create  ::fsm/to :creating ::fsm/actions [:create]}
              {::fsm/event :kill    ::fsm/to :killed}]
   :creating [{::fsm/event :created ::fsm/to :down}
              {::fsm/event :error   ::fsm/to :init}]
   :down     [{::fsm/event :start   ::fsm/to :starting ::fsm/actions [:start]}
              {::fsm/event :kill    ::fsm/to :killing  ::fsm/actions [:kill]}]
   :starting [{::fsm/event :started ::fsm/to :up}
              {::fsm/event :error   ::fsm/to :down}]
   :stopping [{::fsm/event :stopped ::fsm/to :down}
              {::fsm/event :error   ::fsm/to :up}]
   :up       [{::fsm/event :stop    ::fsm/to :stopping ::fsm/actions [:stop]}
              {::fsm/event :kill    ::fsm/to :killing  ::fsm/actions [:kill]}]
   :killing  [{::fsm/event :killed  ::fsm/to :killed}
              {::fsm/event :error   ::fsm/to :killing}]
   :killed   []})

(def generator
  (gen/let [[state transitions] (gen/elements (dissoc resource-rules :killed))
            transition          (gen/elements transitions)]
    [state transition]))

(defspec transit-test
  100000
  (prop/for-all
   [[state transition] generator]
   (when (some? transition)
     (= transition (fsm/transit (fsm/validate-rules resource-rules)
                                state
                                (::fsm/event transition))))))

(deftest resource-transitions-test
  (= {::fsm/state :killed}
     (reduce fsm/transit
             {::fsm/rules resource-rules ::fsm/state :init}
             [:create :created :start :started :stop :stopped :kill :killed])))

(comment
  (require '[automata.view :as v])
  (spit "/home/pyr/t/rules.dot" (v/draw-fsm resource-rules))

  )
