(ns todoquiescent.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [enfocus.core :as ef]
            [bidi.bidi :as bidi]))

(def VIEW_MODE_ALL :all)

(def VIEW_MODE_ACTIVE :active)

(def VIEW_MODE_COMPLETED :completed)

(def ENTER_KEY 13)

(def ESC_KEY 27)

(def model-todo (atom {:todos [{:id 1357 :title "fazer aplicativo de ensino de espanhol" :completed true}     
                               {:id 6204 :title "marketing digital" :completed false}
                               {:id 4369 :title "ensinar React.js" :completed true}]
                       :view-mode :all}))

(defn remove-completed [evt]
  (swap! model-todo  (fn [md] (assoc md :todos (vec (remove :completed (:todos md)))))))

(q/defcomponent Footer
  "Footer"
  [{:keys [view-mode qtds-todos-left has-completed-todos]}]
  (d/footer {:className "footer"}
            (d/span {:className "todo-count"}
                    (d/strong {}
                              (str qtds-todos-left))
                    (str " " 
                         (if (= 1 qtds-todos-left)
                           "item"
                           "itens")
                         " left"))
            (d/ul {:className "filters"}
                  (d/li {}
                        (d/a {:className (when (= VIEW_MODE_ALL view-mode) "selected")
                              :href "#/"}
                             "All"))
                  (d/li {}
                        (d/a {:className (when (= VIEW_MODE_ACTIVE view-mode) "selected")
                              :href "#/active"}
                             "Active"))
                  (d/li {}
                        (d/a {:className (when (= VIEW_MODE_COMPLETED view-mode) "selected")
                              :href "#/completed"}
                             "Completed")))
            (when has-completed-todos
              (d/button {:className "clear-completed"
                         :onClick remove-completed}
                        "Clear completed"))))

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
               (d/label {}
                        title)
               (d/button {:className "destroy"
                          :onClick remove-todo-listener}))))

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
                       (d/input {:className "toggle-all"})
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
                                      (render)))

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
         (routing-fn)))

(set! (.-onhashchange js/window) 
      routing-fn)

(render)

