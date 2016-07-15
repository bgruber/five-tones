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

(defonce events-state (atom {}))

(defn populate-events [topic]
  (go (let [response (<! (meetup/fetch-topic-events topic))]
        (js/console.log "got results for " topic)
        (reset! events-state (:body response)))))

(defn home-page []
  [:div
   [midi/midi-control]
   [components/event-list events-state]])

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

(defn command-dispatcher []
  (go-loop []
    (let [[command value] (<! midi/command-channel)]
      (case command
        :topic (populate-events (name value))
        :noteon (js/console.log "received note " value)))
    (recur)))

(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (init-midi)
  (mount-root)
  (command-dispatcher))
