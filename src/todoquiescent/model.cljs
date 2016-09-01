(ns todoquiescent.model
  (:require [clojure.core.async :as async]))

(def model-todo (atom {:todos [#_{:id 1357 :title "fazer aplicativo de ensino de espanhol" :completed true}                                    
                               {:id 6204 :title "marketing digital" :completed false}
                               #_{:id 4369 :title "ensinar React.js" :completed true}]
                       :view-mode :all}))

(def update-model-channel (async/chan))

