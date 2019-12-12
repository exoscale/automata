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
  {:state/init     [{::fsm/event :event/create  ::fsm/to :state/creating ::fsm/actions [:action/create]}
                    {::fsm/event :event/kill    ::fsm/to :state/killed}]
   :state/creating [{::fsm/event :event/created ::fsm/to :state/down}
                    {::fsm/event :event/error   ::fsm/to :state/init}]
   :state/down     [{::fsm/event :event/start   ::fsm/to :state/starting ::fsm/actions [:action/start]}
                    {::fsm/event :event/kill    ::fsm/to :state/killing  ::fsm/actions [:action/kill]}]
   :state/starting [{::fsm/event :event/started ::fsm/to :state/up}
                    {::fsm/event :event/error   ::fsm/to :state/down}]
   :state/stopping [{::fsm/event :event/stopped ::fsm/to :state/down}
                    {::fsm/event :event/error   ::fsm/to :state/up}]
   :state/up       [{::fsm/event :event/stop    ::fsm/to :state/stopping ::fsm/actions [:action/stop]}
                    {::fsm/event :event/kill    ::fsm/to :state/killing  ::fsm/actions [:action/kill]}]
   :state/killing  [{::fsm/event :event/killed  ::fsm/to :state/killed}
                    {::fsm/event :event/error   ::fsm/to :state/killing}]
   :state/killed   []})

(def generator
  (gen/let [[state transitions] (gen/elements (dissoc resource-rules :state/killed))
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
             {::fsm/rules resource-rules ::fsm/state :state/init}
             [:event/create :event/created :event/start :event/started
              :event/stop :event/stopped :event/kill :event/killed])))

(comment
  (require '[automata.view :as v])
  (spit "/home/pyr/t/rules.dot" (println (v/draw-fsm resource-rules)))

  )
