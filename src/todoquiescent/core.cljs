(ns todoquiescent.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]))

(q/defcomponent Footer
  "Footer"
  []
  (d/h3 {}
        "Este é o footer"))

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
                       (d/ul {:className "todo-list"})))
          (when (pos? (count (:todos model)))
            (Footer))))

(def my-model {:todos [{:id 1357 :title "fazer aplicativo de ensino de línguas" :completed true}
                       {:id 6204 :title "vender muito" :completed false}]
               :view-mode :all})

(q/render (TodoApp my-model)
          (aget (.getElementsByClassName js/document "todoapp") 0))

(set! (.-onload js/window)
      #(do
         (.log js/console "Boa Noite")))

