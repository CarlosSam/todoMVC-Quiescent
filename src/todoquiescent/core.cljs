(ns todoquiescent.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [todoquiescent.storage :as storage]
            [todoquiescent.footer :refer [Footer]]
            [todoquiescent.item :refer [TodoItem]]
            [todoquiescent.model :refer [model-todo]]
            [todoquiescent.utils :refer [VIEW_MODE_ALL VIEW_MODE_ACTIVE VIEW_MODE_COMPLETED ENTER_KEY ESC_KEY]]
            [enfocus.core :as ef]
            [bidi.bidi :as bidi]))

(defn handle-input-key-press [evt]
  (when (= ENTER_KEY (.-keyCode evt))
    (let [t (-> evt .-currentTarget .-value .trim)]
      (when (seq t)
        (swap! model-todo
               update
               :todos
               (fn [tds]
                 (conj tds {:id (.now js/Date) :title t :completed false})))
        (ef/from (-> evt .-currentTarget) (ef/set-form-input ""))))))

(defn toggle-all-todos [evt]
  (let [completed? (not (-> evt .-currentTarget .-checked))]
    (dorun (map #(swap! model-todo update-in [:todos % :completed] (constantly completed?)) (range (count (:todos @model-todo)))))))

(add-watch model-todo :all-todos-completed (fn [_ _ old new]
                                             (when (not= old new)
                                               (let [todos-completed (every? :completed (:todos new))
                                                     toggle-all-checked (ef/from ".toggle-all" (ef/read-form-input))]
                                                 (ef/at ".toggle-all" (cond 
                                                                       (and todos-completed toggle-all-checked) (ef/set-form-input nil)
                                                                       (and (not todos-completed) (not toggle-all-checked)) (ef/set-form-input #{"all-completed"})))))))

(q/defcomponent TodoApp
   "Entire app"
   [{:keys [todos view-mode]}]
   (d/div {}
          (d/header {:className "header"}
                    (d/h1 {} 
                          "todos")
                    (d/input {:className "new-todo"
                              :onKeyUp handle-input-key-press}))
          (when (pos? (count todos))
            (d/section {:className "main"}
                       (d/input {:className "toggle-all"
                                 :type "checkbox"
                                 :value "all-completed"
                                 :onClick toggle-all-todos})
                       (d/label {:htmlFor "toggle-all"}
                                "Mark all as complete")
                       (d/ul {:className "todo-list"}
                             (map (fn [{:keys [id title completed]}] 
                                    (TodoItem {:id id 
                                               :title title 
                                               :completed completed})) 
                                  (filter (fn [t]
                                            (condp = view-mode
                                              VIEW_MODE_ALL true
                                              VIEW_MODE_ACTIVE (not (:completed t))
                                              VIEW_MODE_COMPLETED (:completed t)))
                                          todos)))))
          (when (pos? (count todos))
            (Footer {:view-mode view-mode
                     :qtds-todos-left (count (filter (complement :completed) todos))
                     :has-completed-todos (some :completed todos)}))))

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
