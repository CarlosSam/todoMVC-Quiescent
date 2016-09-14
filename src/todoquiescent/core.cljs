(ns todoquiescent.core
  (:require-macros [cljs.core.async.macros :as am])
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [goog.History]
            [goog.events :as e]
            [clojure.core.async :as async]
            [todoquiescent.model :as model]
            [todoquiescent.storage :as storage]
            [todoquiescent.footer :refer [Footer]]
            [todoquiescent.item :refer [TodoItem]]
            [todoquiescent.app :as app :refer [TodoApp]]
            [enfocus.core :as ef]))

(defn render [model]
  (q/render (TodoApp {:todos (:todos model)
                      :view-mode (:view-mode model)})
            (aget (.getElementsByClassName js/document "todoapp") 0)))

(defn enable-render-updates [model-atom]
  (add-watch model-atom :render-todos (fn [_ _ old new]
                                        (when (not= old new)
                                          (render new)))))

(defn enable-routing
  "Set up Google Closure history management"
  [model-atom]
  (let [h (goog.History.)]
    (.setEnabled h true)
    (e/listen h goog.History.EventType.NAVIGATE
              (fn [evt]
                (let [token (.-token evt)]
                  (am/go (async/>! model/update-model-channel [#(condp = %2
                                                                      "/active" :active
                                                                      "/completed" :completed
                                                                      :all)
                                                               [:view-mode] 
                                                               token])))))))

(defn enable-process-actions [model-todo]
  (am/go (while true 
           (let [[f ks & e] (async/<! model/update-model-channel)] 
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

(enable-console-print!)