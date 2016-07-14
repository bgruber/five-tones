(ns five-tones.core
  (:require [five-tones.components :as components]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(defonce midi-state (reagent.core/atom {}))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to five-tones"]
   [:div [:a {:href "/about"} "go to about page"]]
   (components/midi-control midi-state)])

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
  (js/console.log "Got midi change event")
  (swap! midi-state assoc :last-change event))

(defn init-midi []
  (-> (js/navigator.requestMIDIAccess)
      (.then (fn [midi]
               (js/console.log "Got midi access!")
               (swap! midi-state assoc :access midi)
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
