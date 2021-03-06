(ns five-tones.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [five-tones.components :as components]
            [five-tones.midi :as midi]
            [five-tones.meetup :as meetup]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [accountant.core :as accountant]
            [cljs.core.async :as async :refer [<!]]))

;; -------------------------
;; Views

(defonce state (atom {:mode :note :pitch 0}))

(defn populate-events [topic]
  (go (let [response (<! (meetup/fetch-topic-events topic))]
        (js/console.log "got results for " topic)
        (swap! state assoc
               :events (:body response)
               :mode :results))))

(defn populate-groups [topic]
  (go (let [response (<! (meetup/fetch-groups topic))]
        (js/console.log "got results for " (name topic))
        (<! (async/timeout 2000))
        (swap! state assoc
               :groups (:body response)
               :mode :results))))

(defn home-page []
  [:div
   #_[midi/midi-control]
   [components/main-content state]])


;; -------------------------
;; Commands
(defn topic-command [topic]
  (swap! state assoc
         :mode :note
         :topic topic
         :groups [])
  (go (<! (async/timeout 150))
      (swap! state assoc :mode :loading))
  (populate-groups topic))

(defn noteon-command [pitch]
  (swap! state assoc
         :mode :note
         :topic nil
         :pitch pitch))

(defn command-dispatcher []
  (go-loop []
    (let [[command value] (<! midi/command-channel)]
      (js/console.log (str "command: " command " value: " value))
      (case command
        :topic (topic-command value)
        :noteon (noteon-command value)))
    (recur)))


;; -------------------------
;; Initialize app

(defn init-midi []
  (let [channel (async/chan)]
    (-> (js/navigator.requestMIDIAccess)
        (.then (fn [midi]
                 (js/console.log "Got midi access!")
                 (midi/init-midi channel midi)
                 (aset midi "onstatechange"
                       (fn [event]
                         (js/console.log "Got midi change event")
                         (async/put! channel event))))
               #(js/console.log "failed to init midi")))))

(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (init-midi)
  (mount-root)
  (command-dispatcher))
