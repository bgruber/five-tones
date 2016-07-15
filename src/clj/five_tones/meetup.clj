(ns five-tones.meetup
  (:require [clj-http.client :as http]
            [config.core :refer [env]]
            [ring.util.response :as ring]))

(def meetup-api-key (:meetup-api-key env))

(def base-url "https://api.meetup.com")

(defn filter-headers [headers]
  (let [acceptable-headers #{"Content-Type"}]
   (filter (fn [k v] (acceptable-headers k)) headers)))

(defn fetch-events [topic]
  (http/get (str base-url "/2/open_events")
            {:query-params {"topic" topic
                            "key" meetup-api-key}}))

(defn fetch-my-events []
  (http/get (str base-url "/self/calendar")
            {:query-params {"key" meetup-api-key}}))

(defn fetch-groups [topicid]
  (http/get (str base-url "/find/groups")
            {:query-params {"topic_id" topicid
                            "key" meetup-api-key}}))

(defn- proxy-response [response]
  (let [content-header (-> response (ring/find-header "Content-Type") second)]
    (-> (ring/response (:body response))
        (ring/header "Content-Type" content-header))))

(defn groups [topicid]
  (proxy-response (fetch-groups topicid)))

(defn topic-events [topic]
  (proxy-response (fetch-events topic)))

(defn my-events [req]
  (let [meetup-response (fetch-my-events)
        content-header (-> meetup-response (ring/find-header "Content-Type") second)]
    (-> (ring/response (:body meetup-response))
        (ring/header "Content-Type" content-header))))
