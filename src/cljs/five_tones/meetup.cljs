(ns five-tones.meetup
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async]))

(def base-url "./meetup")

(defn fetch-my-events []
  (let [response (http/get (str base-url "/my-events"))]
    response))

(defn fetch-topic-events [topic]
  (js/console.log "fetching events for " topic)
  (http/get (str base-url "/events/" topic)))

(defn event-list [state]
  [:div
   [:h1 "Event list!"]
   [:ul (for [event (:results @state)]
          ^{:key (:id event)} [:li (:name event)])]])
