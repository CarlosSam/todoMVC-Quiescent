(ns todoquiescent.core
  (:require-macros [cljs.core.async.macros :as am])
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [clojure.core.async :as async]
            [todoquiescent.storage :as storage]
            [todoquiescent.footer :refer [Footer]]
            [todoquiescent.item :refer [TodoItem]]
            [todoquiescent.app :refer [TodoApp]]
            [todoquiescent.model :as model :refer [model-todo]]
            [enfocus.core :as ef]
            [bidi.bidi :as bidi]))

(defn render []
  (q/render (TodoApp {:todos (:todos @model-todo)
                      :view-mode (:view-mode @model-todo)})
            (aget (.getElementsByClassName js/document "todoapp") 0)))

(add-watch model-todo :render-todos (fn [_ _ old new]
                                      (when (not= old new)
                                        (render))))

(add-watch model-todo :store-todos (fn [_ _ old new]
                                     (when (not= old new)
                                       (storage/store-todos))))

(def my-routes ["/" {"" :all
                     "active" :active
                     "completed" :completed}])

(defn routing-fn []
  (->> (-> (.-hash js/location)
           (.slice 1))
       (bidi/match-route my-routes)
       :handler
       (#(if (nil? %) :all %))
       (swap! model-todo assoc :view-mode)))

(set! (.-onload js/window)
      #(do
         (storage/load-todos)
         (routing-fn)))

(set! (.-onhashchange js/window) 
      routing-fn)

(render)

(am/go (while true 
         (let [[[f ks & e]] (async/alts! [model/update-model-channel])] 
           (apply swap! model-todo update-in ks f e))))
