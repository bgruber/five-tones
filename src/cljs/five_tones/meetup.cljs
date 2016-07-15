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

(def topicmap
  {:electronicmusic 411
   :pokemon 2602
   :science-fiction 18864
   :ghosts 220})

(defn fetch-groups [topic]
  (js/console.log "fetching groups for " topic)
  (http/get (str base-url "/groups/" (topicmap topic))))
