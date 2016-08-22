(ns todoquiescent.storage
  (:require [todoquiescent.model :refer [model-todo]]))

(def LOCAL_STORAGE_NAMESPACE "todos-quiescent-cljs")

(defn load-todos []
  (->> LOCAL_STORAGE_NAMESPACE 
       (.getItem js/localStorage) 
       (.parse js/JSON) 
       js->clj 
       ((fn [mp]
          (reduce (fn [s k]
                    (let [v (get mp k)
                          v (if (string? v)
                              (keyword v)
                              (vec (map #(reduce (fn [sv kv]
                                                   (assoc sv (keyword kv) (get % kv)))
                                                 {}
                                                 (keys %))
                                        v)))]
                      (assoc s (keyword k) v)))
                  {}
                  (keys mp))))
       (reset! model-todo)))

(defn store-todos []
  (->> @model-todo
       clj->js
       (.stringify js/JSON)
       (.setItem js/localStorage LOCAL_STORAGE_NAMESPACE)))
