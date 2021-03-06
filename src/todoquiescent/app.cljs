(ns todoquiescent.app
  (:require-macros [cljs.core.async.macros :as am])
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [clojure.core.async :as async]
            [todoquiescent.model :as model]
            [todoquiescent.item :refer [TodoItem]]
            [todoquiescent.footer :refer [Footer]]
            [todoquiescent.utils :refer [VIEW_MODE_ALL VIEW_MODE_ACTIVE VIEW_MODE_COMPLETED ENTER_KEY ESC_KEY]]
            [enfocus.core :as ef]))

(defn handle-input-key-press [evt]
  (when (= ENTER_KEY (.-keyCode evt))
    (let [t (-> evt .-currentTarget .-value .trim)]
      (when (seq t)
        (am/go (async/>! model/update-model-channel [(partial conj) [:todos] {:id (.now js/Date) :title t :completed false}]))
        (ef/from (-> evt .-currentTarget) (ef/set-form-input ""))))))

(defn toggle-all-todos [evt]
  (let [completed? (not (-> evt .-currentTarget .-checked))]
    (am/go (async/>! model/update-model-channel [#(vec (for [todo %]
                                                         (assoc todo :completed %2))) 
                                                 [:todos] 
                                                 completed?]))))

(defn enable-all-todos-completed-checkbox-update [model]
  (add-watch model :all-todos-completed (fn [_ _ old new]
                                          (when (not= old new)
                                            (let [todos-completed (every? :completed (:todos new))
                                                  toggle-all-checked (ef/from ".toggle-all" (ef/read-form-input))]
                                              (ef/at ".toggle-all" (cond 
                                                                     (and todos-completed toggle-all-checked) (ef/set-form-input nil)
                                                                     (and (not todos-completed) (not toggle-all-checked)) (ef/set-form-input #{"all-completed"}))))))))

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
                                               :completed completed
                                               :todos todos})) 
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

