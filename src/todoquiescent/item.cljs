(ns todoquiescent.item
  (:require-macros [cljs.core.async.macros :as am])
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [clojure.core.async :as async]
            [todoquiescent.model :refer [update-model-channel]]
            [todoquiescent.utils :refer [VIEW_MODE_ALL VIEW_MODE_ACTIVE VIEW_MODE_COMPLETED ENTER_KEY ESC_KEY]]
            [enfocus.core :as ef]))

(defn remove-todo [id]
  (am/go (async/>! update-model-channel [(fn [todos] (vec (remove #(= (:id %) id) todos))) [:todos] id])))

(defn remove-todo-listener [evt]
  (let [id (js/parseInt (ef/from (-> evt .-currentTarget .-parentElement .-parentElement) (ef/get-attr :data-id)))]
    (remove-todo id)))

(defn idx-todo [todos id]
  (count (for [e todos
               :while (not= (:id e) id)]
           nil)))

(defn toggle-completed [todos evt]
  (let [id (-> evt .-currentTarget .-parentElement .-parentElement (ef/at (ef/get-attr :data-id)) js/parseInt)
        idx (idx-todo todos id)]
    (am/go (async/>! update-model-channel [not [:todos idx :completed]]))))

(defn leave-edit-mode []
  (when (seq (ef/from ".editing" identity))
    (ef/at ".editing" (ef/remove-class "editing"))))

(defn process-edit-input [todos evt]
  (let [t (-> evt .-currentTarget .-value .trim)
        id (js/parseInt (ef/at (-> evt .-currentTarget .-parentElement) (ef/get-attr :data-id)))
        idx (idx-todo todos id)]
    (if (seq t)
      (am/go (async/>! update-model-channel [(constantly t) [:todos idx :title]]))
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
  [{:keys [id title completed todos]}]
  (d/li {:data-id id
         :className (when completed "completed")}
        (d/div {:className "view"}
               (d/input {:className "toggle"
                         :type "checkbox"
                         :defaultChecked completed
                         :onClick (partial toggle-completed todos)})
               (d/label {:onDoubleClick prepare-edit-input}
                        title)
               (d/button {:className "destroy"
                          :onClick remove-todo-listener}))
        (d/input {:defaultValue title
                  :className "edit"
                  :onBlur (partial process-edit-input todos)
                  :onKeyUp handle-onkeyup})))
