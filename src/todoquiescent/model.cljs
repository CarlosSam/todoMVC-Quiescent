(ns todoquiescent.model
  (:require [clojure.core.async :as async]))

(def model-todo (atom {:todos [#_{:id 1357 :title "fazer aplicativo de ensino de espanhol" :completed true}                                    
                               {:id 6204 :title "marketing digital" :completed false}
                               #_{:id 4369 :title "ensinar React.js" :completed true}]
                       :view-mode :all}))

(def update-model-channel (async/chan))

(defn add-todo [todos todo]
  (conj todos todo))

(defn toggle-all-todos [todos completed?]
  (vec (for [todo todos]
         (assoc todo :completed completed?))))
