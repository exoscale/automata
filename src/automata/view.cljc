(ns automata.view)

(defn draw-transition
  [[state transitions]]
  (for [{:automata.fsm/keys [event to actions]} transitions]
    (str (name state) " -> " (name to) " [label=\"" (name event) "\"];\n")))

(defn draw-fsm
  [rules]
  (str "digraph G {\n" (reduce str (mapcat draw-transition rules)) "}\n"))
