(ns todoquiescent.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [enfocus.core :as ef]
            [bidi.bidi :as bidi]))

(def VIEW_MODE_ALL :all)

(def VIEW_MODE_ACTIVE :active)

(def VIEW_MODE_COMPLETED :completed)

(def model-todo (atom {:todos [{:id 1357 :title "fazer aplicativo de ensino de espanhol" :completed true}     
                               {:id 6204 :title "marketing digital" :completed false}
                               {:id 4369 :title "ensinar React.js" :completed true}]
                       :view-mode :all}))

(defn remove-completed [evt]
  (swap! model-todo  (fn [md] (assoc md :todos (vec (remove :completed (:todos md)))))))

(q/defcomponent Footer
  "Footer"
  [view-mode qtds-todos-left has-completed-todos]
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

(q/defcomponent TodoItem
  "item"
  [id title completed]
  (d/li {:data-id id
         :className (when completed "completed")}
        (d/div {:className "view"}
               (d/input {:className "toggle"
                         :type "checkbox"
                         :defaultChecked completed})
               (d/label {}
                        title)
               (d/button {:className "destroy"
                          :onClick remove-todo-listener}))))

(q/defcomponent TodoApp
   "Entire app"
   [model]
   (d/div {}
          (d/header {:className "header"}
                    (d/h1 {} 
                          "todos")
                    (d/input {:className "new-todo"}))
          (when (pos? (count (:todos model)))
            (d/section {:className "main"}
                       (d/input {:className "toggle-all"})
                       (d/label {:htmlFor "toggle-all"}
                                "Mark all as complete")
                       (d/ul {:className "todo-list"}
                             (map (fn [{:keys [id title completed]}] 
                                    (TodoItem id title completed)) 
                                  (filter (fn [t]
                                            (condp = (:view-mode model)
                                              VIEW_MODE_ALL true
                                              VIEW_MODE_ACTIVE (not (:completed t))
                                              VIEW_MODE_COMPLETED (:completed t)))
                                          (:todos model))))))
          (when (pos? (count (:todos model)))
            (Footer (:view-mode model) 
                    (count (filter (complement :completed) (:todos model)))
                    (some :completed (:todos model))))))

(defn render []
  (q/render (TodoApp @model-todo)
          (aget (.getElementsByClassName js/document "todoapp") 0)))

(add-watch model-todo :render-todos (fn [_ _ old new]
                                      (when (not= old new)
                                        (render))))

(def my-routes ["/" {"" :all
                     "active" :active
                     "completed" :completed}])

(defn routing-fn []
  (->> (-> (.-hash js/location)
           (.slice 1))
       (bidi/match-route my-routes)
       :handler
       (swap! model-todo assoc :view-mode)))

(set! (.-onload js/window)
      #(do
         (routing-fn)))

(set! (.-onhashchange js/window) 
      routing-fn)

(render)

