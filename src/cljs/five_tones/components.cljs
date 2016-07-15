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

(def topic-names
  {:science-fiction "Science Fiction"
   :pokemon "Pokemon!"
   :electronicmusic "Electronic Music"
   :ghosts "Ghosts"})

(defn results [topic state]
  [:div {:className "bounds"}
   [:h2 (topic-names topic) " Results"]
   (for [group (:groups @state)]
     ^{:key (:id group)}
     [:div {:className "listItem"} (:name group)])])

(def minPitch 48)
(def maxPitch 72)

(defn hue [pitch]
  (/ (* 100 (- pitch minPitch)) (- maxPitch minPitch)))

(defn position [pitch]
  (/ (* 360 (- pitch minPitch)) (- maxPitch minPitch)))

(defn the-score [pitch]
  [:div {:className "page"}
   [:div {:className "scoreOverlay"}]
   [:div {:className "score"}
    [:div {:className "note"
           :style {:top (position pitch) :background (str "hsl(" (hue pitch) ", 100%, 50%)")}}]]])

(defn catOverlay [topic shown]
  [:div {:className (if shown "catOverlay enter" "catOverlay")
         :style {:backgroundImage "url(/SCI-FI_67162.jpg)"}}
   [:h1 (topic-names topic)]])

(defn rick-content [state]
  [:div {:className "page"}
   (results :science-fiction state)
   (the-score (:pitch @state))
   (catOverlay (:topic @state) (= (:mode @state) :loading))])

(defn main-content [state]
  (rick-content state)
  #_ (let [mode (:mode @state)]
    (case mode
      :topic (group-list (:topic @state) state)
      :note (color-field (:pitch @state)))))


