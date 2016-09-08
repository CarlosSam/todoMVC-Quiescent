(defproject todoquiescent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.225"]
                 [org.clojure/core.async "0.2.385"]
                 [enfocus "2.1.1"]
                 [quiescent "0.3.2"]]
  :plugins [[lein-figwheel "0.5.0-1"]
            [lein-cljsbuild "1.1.3"]]
  :clean-targets ^{:protect false} [:target-path "resources/public/cljs"]
  :cljsbuild {
    :builds {:dev {:source-paths ["src"]
                   :figwheel true
                   :compiler {:main "todoquiescent.core"
                              :asset-path "cljs/out-dev"
                              :output-to "resources/public/cljs/main-dev.js"
                              :output-dir "resources/public/cljs/out-dev"}}
             :prod {:source-paths ["src"]
                    :figwheel false
                    :compiler {:main "todoquiescent.core"
                               :asset-path "cljs/out"
                               :output-to "resources/public/cljs/main.js"
                               :output-dir "resources/public/cljs/out"
                               :optimizations :advanced}}}}
  :main ^:skip-aot todoquiescent.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
