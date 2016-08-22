(ns todoquiescent.footer
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [todoquiescent.utils :refer [VIEW_MODE_ALL VIEW_MODE_ACTIVE VIEW_MODE_COMPLETED]]
            [todoquiescent.model :refer [model-todo]]))

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
