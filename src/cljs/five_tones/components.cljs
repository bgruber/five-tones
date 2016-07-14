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

(defn current-input-available? [current all]
  (and (some? current)
       (some? all)
       (all current.id)))

(defn raw-midi->note [raw-midi]
  (if (not (= 3 (count raw-midi)))
    nil ;; proper midi messages have 3 bytes
    (let [[message-byte note velocity] raw-midi]
      {:type (case message-byte
                  144 :noteon
                  128 :noteoff
                  nil)
       :pitch note
       :velocity velocity})))

(def RING-MAX-SIZE 10) ;; TODO make this the max length of any of the melodies we're matching
(defonce melody-ring (r/atom '()))
(defn update-melody-ring [message]
  (when-let [note-message (raw-midi->note (array-seq message.data))]
    (if (= :noteon (:type note-message))
      (swap! melody-ring (comp #(take RING-MAX-SIZE %) conj) (:pitch note-message)))))

(defn on-midi-message [message]
  (update-melody-ring message))

(defn set-current-input! [state input]
  (when-let [old-input (:current-input @state)]
    (aset old-input "onmidimessage" nil))
  (aset input "onmidimessage" on-midi-message)
  (swap! state assoc :current-input input))

(defn midi-input-list [state]
  (let [current-input (:current-input @state)
        inputs        (input-map (:access @state))] 
    ;; if our currently-selected input is no longer available, ditch it
    (when-not (current-input-available? current-input inputs)
      (set-current-input! state (if (empty? inputs) nil (last (vals inputs)))))
    [:select
     {:on-change #(set-current-input! state (inputs (-> % .-target .-value)))
      :value (if current-input current-input.id nil)}
     (for [input (vals inputs)]
       ^{:key input.id}
       [:option {:value input.id}
        input.name])]))

(defn current-input-name [state]
  (if-let [input (:current-input @state)]
    [:p "The current input is named " input.name]
    [:p "No input selected"]))

(defn melody-display []
  [:p (for [pitch @melody-ring]
        (str pitch " "))])

(defn midi-control [midi-state]
  (when (:access @midi-state)
    [:div
     [current-input-name midi-state]
     [midi-input-list midi-state]
     [melody-display]]))
