(ns five-tones.meetup
  (:require [clj-http.client :as http]
            [config.core :refer [env]]
            [ring.util.response :as ring]))

(def meetup-api-key (:meetup-dev-scott-api-key env))

(def base-url "http://api.dev.meetup.com")

(defn filter-headers [headers]
  (let [acceptable-headers #{"Content-Type"}]
   (filter (fn [k v] (acceptable-headers k)) headers)))

(defn fetch-my-events []
  (http/get (str base-url "/self/calendar")
            {:query-params {"key" meetup-api-key}}))

(defn my-events [req]
  (let [meetup-response (fetch-my-events)
        content-header (-> meetup-response (ring/find-header "Content-Type") second)]
    (-> (ring/response (:body meetup-response))
        (ring/header "Content-Type" content-header))))
