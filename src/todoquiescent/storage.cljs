(ns todoquiescent.storage
  (:require [todoquiescent.model :refer [model-todo]]
            [cljs.reader :as reader]))

(def LOCAL_STORAGE_NAMESPACE "todos-quiescent-cljs")

(defn load-todos []
  (->> LOCAL_STORAGE_NAMESPACE
       (.getItem js/localStorage)
       reader/read-string
       (reset! model-todo)))

(defn store-todos []
  (->> @model-todo
       str
       (.setItem js/localStorage LOCAL_STORAGE_NAMESPACE)))
