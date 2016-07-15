(ns five-tones.components)

(defn event-list [state]
  [:div
   [:h1 "Event list!"]
   [:ul (for [event (:results @state)]
          ^{:key (:id event)} [:li (:name event)])]])
