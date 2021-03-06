(ns five-tones.midi
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [reagent.core :as r]
            [cljs.core.async :as async]))

;; used to send commands back to the main app
(defonce command-channel (async/chan))

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

(defn- check-prefix
  "returns true if melody starts with prefix"
  [prefix melody]
  (= prefix
     (take (count prefix) melody)))

(def prefixes
  {:science-fiction [63 56 68 72 70]
   :pokemon [67 67 65 62 62 62 58 60 62 62 60 58 58 60 62 65 67]
   :electronicmusic [68 70 71 60 68 60 67 65]
   :ghosts [59 61 57 59 59 59 59 57 61 59 63 59 59]
   })

(def RING-MAX-SIZE 128) ;; TODO make this the max length of any of the melodies we're matching
(defn get-command
  "Given the most recent notes played, return a command if it should be sent"
  [melody]
  (let [matching-prefixes (filter #(check-prefix (second %) melody) prefixes)]
    (if (empty? matching-prefixes)
      [:noteon (first melody)]
      [:topic (ffirst matching-prefixes)])))

(defonce melody-ring (r/atom '()))
(defn update-melody-ring [message]
  (when-let [note-message (raw-midi->note (array-seq message.data))]
    (if (= :noteon (:type note-message))
      (get-command (swap! melody-ring (comp #(take RING-MAX-SIZE %) conj) (:pitch note-message))))))

(defn on-midi-message [message]
  (when-let [command (update-melody-ring message)]
    (async/put! command-channel command)))

(defn set-current-input! [state input]
  (when-let [old-input (:current-input @state)]
    (aset old-input "onmidimessage" nil))
  (aset input "onmidimessage" on-midi-message)
  (swap! state assoc :current-input input))

(defn- update-inputs-state [state]
  (let [current-input (:current-input @state)
        inputs        (input-map (:access @state))]
    (swap! state assoc :inputs inputs)
    ;; if our currently-selected input is no longer available, ditch it
    (when-not (current-input-available? current-input inputs)
      (set-current-input! state (last (vals inputs))))))

(defn midi-input-list [state]
  (let [current-input    (:current-input @state)
        current-input-id (if current-input current-input.id nil)
        inputs           (:inputs @state)] 
    [:select
     {:on-change #(set-current-input! state (inputs (-> % .-target .-value)))
      :value current-input-id}
     (for [input (vals inputs)]
       ^{:key input.id}
       [:option {:value input.id}
        input.name])]))

(defonce midi-state (r/atom {}))

;; cause i needed a way to show the melodies so i could copy-paste them into the list
(defn toggle-melody-display []
  (swap! midi-state update-in [:melody-display] not))
(defn melody-display []
  [:p (for [pitch @melody-ring]
        (str pitch " "))])

(defn midi-control []
  (when (:access @midi-state)
    [:div
     [:p "Choose MIDI Input: "
      [midi-input-list midi-state]]
     (when (:melody-display @midi-state)
       [melody-display])]))

(defn init-midi [channel access]
  (swap! midi-state assoc :access access)
  (update-inputs-state midi-state)
  (go-loop []
    (let [event (async/<! channel)]
      (js/console.log "Updating MIDI inputs")
      (update-inputs-state midi-state))
    (recur)))
