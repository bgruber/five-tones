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

(defn results [state]
  [:div {:className "bounds"}
   [:h2 "Some Category Results"]
   (for [group (:groups @state)]
     ^{:key (:id group)}
     [:div {:className "listItem"} (:name group)])])

(defn the-score [position hue]
  [:div {:className "page"}
   [:div {:className "scoreOverlay"}]
   [:div {:className "score"}
    [:div {:className "note"
           :style {:top position :background (str "hsl(" hue ", 100%, 50%)")}}]]])

(defn catOverlay [shown]
  [:div {:className (if shown "catOverlay enter" "catOverlay")
         :style {:backgroundImage "url(/SCI-FI_67162.jpg)"}}
   [:h1 "Some category"]])

(defn rick-content [state]
  [:div {:className "page"}
   (results state)
   (the-score 0 0)
   (catOverlay false)])

(defn main-content [state]
  (rick-content state)
  #_ (let [mode (:mode @state)]
    (case mode
      :topic (group-list (:topic @state) state)
      :note (color-field (:pitch @state)))))


