(ns automata.view
  "Simplistic visualization builder. Leaves calling
   graphviz tools up to the caller")

(defn identifier
  [x]
  (format "\"%s\"" (name x)))

(defn ^:no-doc draw-transition
  "Single transition view builder"
  [[state transitions]]
  (for [{:automata.fsm/keys [event to]} transitions]
    (str (identifier state) " -> " (identifier to) " [label="(identifier event) "];\n")))

(defn draw-fsm
  "Create a string which can later be fed to graphviz's dot"
  [rules]
  (str "digraph G {\n" (reduce str (mapcat draw-transition rules)) "}\n"))
