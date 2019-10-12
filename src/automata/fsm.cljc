(ns automata.fsm
  "Finite state transducer library"
  (:require [clojure.spec.alpha :as s]))

(defn ^:no-doc extract
  [x k]
  (cond (map? x) (get x k) (keyword? x) x))

(defn transit
  "Given a set of rules for states, a current state and event,
   figure out the next state and potential side effects to perform"
  ([machine event]
   (let [transition (transit (::rules machine) (::state machine) event)]
     (if (map? transition)
       (let [{::keys [actions to]} transition]
         (-> machine (dissoc ::actions) (assoc ::state to)
             (cond-> (some? actions) (assoc ::actions actions))))
       transition)))
  ([rules state event]
   (let [transitions (get rules (extract state ::state))
         e           (extract event ::event)]
     (or (first
          (for [transition transitions :when (= (::event transition) e)]
            transition))
         (ex-info "cannot find transition"
                  {:type :exoscale.ex/not-found ::state state ::event event})))))

(defn validate-rules
  "Perform sanity checks on a rule set, intended to be ran when loading rules"
  [rules]
  (when-not (s/valid? ::rules rules)
    (throw (ex-info (s/explain-str ::rules rules)
                    {:type :exoscale.ex/incorrect})))
  (let [valid-states   (set (keys rules))
        found-states   (->> (vals rules)
                            (mapcat ::transitions)
                            (map ::to)
                            (set))
        invalid-states (remove #(contains? valid-states %) found-states)]
    (when (seq invalid-states)
      (throw (ex-info (format "transitions contain invalid states: "
                              (map name invalid-states))
                      {:type :exoscale.ex/incorrect}))))
  rules)

;; Specs
;; =====

(s/def ::state       keyword?)
(s/def ::action      keyword?)
(s/def ::actions     (s/coll-of ::action))
(s/def ::transition  (s/keys :req [::event ::to] :opt [::actions]))
(s/def ::transitions (s/coll-of ::transition))
(s/def ::rules       (s/map-of ::state ::transitions))
(s/def ::machine     (s/keys :req [::rules ::state] :opt [::actions]))
