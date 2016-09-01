(ns todoquiescent.model
  (:require [clojure.core.async :as async]))

(def model-todo (atom {:todos []
                       :view-mode :all}))

(def update-model-channel (async/chan))

