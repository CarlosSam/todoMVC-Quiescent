(ns todoquiescent.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]))

(def VIEW_MODE_ALL :all)

(def VIEW_MODE_ACTIVE :active)

(def VIEW_MODE_COMPLETED :completed)

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
              (d/button {:className "clear-completed"}
                        "Clear completed")) ))

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
               (d/button {:className "destroy"}))))

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

(def my-model {:todos [{:id 1357 :title "fazer aplicativo de ensino de línguas" :completed false}
                       {:id 6204 :title "vender muito" :completed false}
                       {:id 4369 :title "ensinar realmente" :completed true}]
               :view-mode :all})

(q/render (TodoApp my-model)
          (aget (.getElementsByClassName js/document "todoapp") 0))

(set! (.-onload js/window)
      #(do
         (.log js/console "Boa Noite")))

