(ns five-tones.core
  (:require [five-tones.components :as components]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(defonce state (atom {}))

;; -------------------------
;; Utility
(defn input-map [midi]
  (when midi
   (->> midi
        .-inputs
        (.entries)
        es6-iterator-seq
        js->clj
        (into {}))))

;; -------------------------
;; Components
(defn midi-input-list []
  (let [inputs (:midi-inputs @state)]
    [:ul
     (for [input (vals inputs)]
       ^{:key input.id} [:li input.name])]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to five-tones"]
   [:div [:a {:href "/about"} "go to about page"]]
   (midi-input-list)])

(defn about-page []
  [:div [:h2 "About five-tones"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn midi-state-change [event]
  ;; just swap out the whole thing - HACKATHON
  (js/console.log "Got midi change event")
  (swap! state assoc :midi-inputs (input-map event.target)))

(defn init-midi []
  (-> (js/navigator.requestMIDIAccess)
      (.then (fn [midi]
               (js/console.log "Got midi access!")
               (swap! state assoc :midi-inputs (input-map midi))
               (aset midi "onstatechange" midi-state-change))
             #(js/console.log "failed to init midi"))))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (init-midi)
  (mount-root))
