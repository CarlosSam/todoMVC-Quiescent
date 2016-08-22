(ns todoquiescent.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [todoquiescent.storage :as storage]
            [todoquiescent.footer :refer [Footer]]
            [todoquiescent.model :refer [model-todo]]
            [todoquiescent.utils :refer [VIEW_MODE_ALL VIEW_MODE_ACTIVE VIEW_MODE_COMPLETED ENTER_KEY ESC_KEY]]
            [enfocus.core :as ef]
            [bidi.bidi :as bidi]))

(defn remove-todo [id]
  (swap! model-todo (fn [md] (assoc md :todos (vec (remove #(= (:id %) id) (:todos md)))))))

(defn remove-todo-listener [evt]
  (let [id (js/parseInt (ef/from (-> evt .-currentTarget .-parentElement .-parentElement) (ef/get-attr :data-id)))]
    (remove-todo id)))

(defn idx-todo [id]
  (count (for [e (:todos @model-todo)
               :while (not= (:id e) id)]
           nil)))

(defn toggle-completed [evt]
  (let [id (-> evt .-currentTarget .-parentElement .-parentElement (ef/at (ef/get-attr :data-id)) js/parseInt)
        idx (idx-todo id)]
    (swap! model-todo update-in [:todos idx :completed] not)))

(defn leave-edit-mode []
  (when (seq (ef/from ".editing" identity))
    (ef/at ".editing" (ef/remove-class "editing"))))

(defn process-edit-input [evt]
  (let [t (-> evt .-currentTarget .-value .trim)
        id (js/parseInt (ef/at (-> evt .-currentTarget .-parentElement) (ef/get-attr :data-id)))
        idx (idx-todo id)]
    (if (seq t)
      (swap! model-todo update-in [:todos idx :title] (constantly t))
      (remove-todo id))
    (leave-edit-mode)))

(defn prepare-edit-input [evt]
  (let [li-element (-> evt .-currentTarget .-parentElement .-parentElement)]
    (ef/at li-element (ef/add-class "editing"))
    (ef/at li-element ".edit" (ef/focus))))

(defn handle-onkeyup [evt]
  (cond
   (= ESC_KEY (.-keyCode evt)) (do 
                                 (ef/at (-> evt .-currentTarget) (ef/set-form-input (ef/from (-> evt .-currentTarget .-parentElement) (ef/get-text))))
                                 (leave-edit-mode))
   (= ENTER_KEY (.-keyCode evt)) (ef/at (-> evt .-currentTarget) (ef/blur))
   :default nil))

(q/defcomponent TodoItem
  "item"
  :keyfn identity
  [{:keys [id title completed]}]
  (d/li {:data-id id
         :className (when completed "completed")}
        (d/div {:className "view"}
               (d/input {:className "toggle"
                         :type "checkbox"
                         :defaultChecked completed
                         :onClick toggle-completed})
               (d/label {:onDoubleClick prepare-edit-input}
                        title)
               (d/button {:className "destroy"
                          :onClick remove-todo-listener}))
        (d/input {:defaultValue title
                  :className "edit"
                  :onBlur process-edit-input
                  :onKeyUp handle-onkeyup})))

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
