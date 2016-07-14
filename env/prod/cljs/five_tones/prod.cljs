(ns five-tones.prod
  (:require [five-tones.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
