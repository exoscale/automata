(ns automata.fsm
  "Finite state transducer library. Intended for data representations of
   state transitions, leaving actions to consumers of the library."
  (:require [clojure.spec.alpha :as s]))

(defn ^:no-doc extract
  [x k]
  (cond
    (map? x) (get x k)
    (keyword? x) x))

(defn transit
  "Given a set of rules for states, a current state and event,
   figure out the next state and potential side effects to perform.

   Transit can either be called on a *machine*, i.e: a map containing
   the current state and rules. In this case, an updated machine map
   will be returned, with a potentially new state and potential actions
   to perform if any.

   When called with the three-arity version, transition rules, state,
   and event are provided separately. The output is then a transition.

   Both arities throw when no possible transition was found"
  ([{::keys [rules state] :as machine} event]
   (let [{::keys [actions to]} (transit rules state event)]
     (-> machine
         (dissoc ::actions)
         (assoc ::state to)
         (cond-> (some? actions) (assoc ::actions actions)))))
  ([rules state event]
   (let [transitions (get rules (extract state ::state))
         e           (extract event ::event)]
     (or (reduce #(when (identical? (::event %2) e)
                    (reduced %2))
                 nil
                 transitions)
         (throw
          (ex-info "cannot find transition"
                   {:type   :exoscale.ex/not-found
                    ::state state
                    ::event event}))))))

(defn invalid-states
  "Returns a collection of invalid target states used in

  Predicate to check whether a given valid ruleset provides
   a functioning set of rules. Rules are deemed functioning if
   all target states are known."
  [rules]
  (let [valid-states (set (keys rules))]
    (-> (into #{}
              (comp (mapcat val)
                    (map ::to)
                    (distinct)
                    (remove (partial contains? valid-states)))
              rules)
        not-empty)))

(defn validate-rules
  "Perform sanity checks on a rule set, intended to be ran when loading rules.
   Throws on badly formulated rules"
  [rules]
  (when-not (s/valid? ::rules rules)
    (throw (ex-info (s/explain-str ::rules rules)
                    {:type :exoscale.ex/incorrect})))
  (when-let [states (invalid-states rules)]
    (throw (ex-info (reduce str "transitions contain invalid states: "
                            (interpose ", " (map name states)))
                    {:type :exoscale.ex/incorrect
                     :states states})))
  rules)

;; Specs
;; =====

(s/def ::state       qualified-keyword?)
(s/def ::action      qualified-keyword?)
(s/def ::event       qualified-keyword?)
(s/def ::actions     (s/coll-of ::action))
(s/def ::transition  (s/keys :req [::event ::to] :opt [::actions]))
(s/def ::transitions (s/coll-of ::transition))
(s/def ::rules       (s/map-of ::state ::transitions))
(s/def ::machine     (s/keys :req [::rules ::state] :opt [::actions]))
