(ns todoquiescent.model)

(def model-todo (atom {:todos [#_{:id 1357 :title "fazer aplicativo de ensino de espanhol" :completed true}     
                               #_{:id 6204 :title "marketing digital" :completed false}
                               #_{:id 4369 :title "ensinar React.js" :completed true}]
                       :view-mode :all}))
