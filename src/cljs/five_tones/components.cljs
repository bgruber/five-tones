(ns five-tones.components)

(defn event-list [topic state]
  [:div
   [:h2 (name topic)]
   [:ul (for [event (-> @state :events :results)]
          ^{:key (:id event)} [:li (:name event)])]])

(defn group-list [topic state]
  [:div
   [:h2 (name topic)]
   [:ul (for [group (:groups @state)]
          ^{:key (:id group)} [:li (:name group)])]])

(defn color-field [pitch]
  [:div pitch])

(defn main-content [state]
  (let [mode (:mode @state)]
    (case mode
      :topic (group-list (:topic @state) state)
      :note (color-field (:pitch @state)))))
