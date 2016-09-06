(ns todoquiescent.core
  (:require-macros [cljs.core.async.macros :as am])
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [clojure.core.async :as async]
            [todoquiescent.model :as model]
            [todoquiescent.storage :as storage]
            [todoquiescent.footer :refer [Footer]]
            [todoquiescent.item :refer [TodoItem]]
            [todoquiescent.app :as app :refer [TodoApp]]
            [enfocus.core :as ef]
            [bidi.bidi :as bidi]))

(defn render [model]
  (q/render (TodoApp {:todos (:todos model)
                      :view-mode (:view-mode model)})
            (aget (.getElementsByClassName js/document "todoapp") 0)))

(defn enable-render-updates [model-atom]
  (add-watch model-atom :render-todos (fn [_ _ old new]
                                        (when (not= old new)
                                          (render new)))))

(def my-routes ["/" {"" :all
                     "active" :active
                     "completed" :completed}])

(defn routing-fn [model-atom]
  (->> (-> (.-hash js/location)
           (.slice 1))
       (bidi/match-route my-routes)
       :handler
       (#(if (nil? %) :all %))
       (swap! model-atom assoc :view-mode)))

(defn enable-routing [model]
  (set! (.-onhashchange js/window) 
        #(routing-fn model)))

(defn enable-process-actions [model-todo]
  (am/go (while true 
           (let [[[f ks & e]] (async/alts! [model/update-model-channel])] 
             (apply swap! model-todo update-in ks f e)))))

(defn start-model []
  (or (storage/load-todos) (model/initial-model-todo)))

(defn ^:export react-quiesce-main []
  "application entry point"
  (.log js/console "iniciando aplicação")
  (let [model (atom (start-model))]
    (enable-process-actions model)
    (enable-render-updates model)
    (storage/enable-store-todos model)
    (enable-routing model)
    (app/enable-all-todos-completed-checkbox-update model)
    (render @model)))
