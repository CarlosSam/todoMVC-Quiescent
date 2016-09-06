(ns todoquiescent.model
  (:require [clojure.core.async :as async]))

(defn initial-model-todo [] 
  {:todos []
   :view-mode :all})

(def update-model-channel (async/chan))

