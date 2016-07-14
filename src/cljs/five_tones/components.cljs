(ns five-tones.components
  (:require [reagent.core :as r]))

(defn input-map [midi]
  (when midi
   (->> midi
        .-inputs
        (.entries)
        es6-iterator-seq
        js->clj
        (into {}))))

(defn current-input-available [current all]
  (and (some? current)
       (some? all)
       (all current.id)))

(defn midi-input-list [state]
  (let [current-input (:current-input @state)
        inputs        (input-map (:access @state))] 
    ;; if our currently-selected input is no longer available, ditch it
    (when-not (current-input-available current-input inputs)
      (swap! state assoc :current-input
             (if (empty? inputs) nil (last (vals inputs)))))
    [:select
     {:on-change #(swap! state assoc :current-input (inputs (-> % .-target .-value)))
      :value (if current-input current-input.id nil)}
     (for [input (vals inputs)]
       ^{:key input.id}
       [:option {:value input.id}
        input.name])]))

(defn current-input-name [state]
  (if-let [input (:current-input @state)]
    [:p "The current input is named " input.name]
    [:p "No input selected"]))

(defn midi-control [midi-state]
  (when (:access @midi-state)
    [:div
     (current-input-name midi-state)
     (midi-input-list midi-state)]))
