(ns five-tones.components)

(defn event-list [topic state]
  [:div
   [:h2 (name topic)]
   [:ul (for [event (-> @state :events :results)]
          ^{:key (:id event)} [:li (:name event)])]])

(defn color-field [state]
  [:div "Empty"])

(defn main-content [state]
  (let [mode (:mode @state)]
    (case mode
      :topic (event-list (:topic @state) state)
      :note (color-field state))))
