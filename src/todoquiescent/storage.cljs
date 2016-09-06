(ns todoquiescent.storage
  (:require [cljs.reader :as reader]))

(def LOCAL_STORAGE_NAMESPACE "todos-quiescent-cljs")

(defn load-todos []
  (when-let [item_as_str (.getItem js/localStorage LOCAL_STORAGE_NAMESPACE)]
    (reader/read-string item_as_str)))

(defn store-todos [state]
  (->> state
       str
       (.setItem js/localStorage LOCAL_STORAGE_NAMESPACE)))

(defn enable-store-todos [model]
  (add-watch model :store-todos (fn [_ _ old new]
                                  (when (not= old new)
                                    (store-todos new)))))
