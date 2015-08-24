(ns ^:figwheel-always dep-graph.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [tree]
              cljsjs.d3))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def d3 (.-d3 js/window))

(om/root
  (fn [data owner]
    (reify
      om/IRender
      (render [_]
        
        (dom/div nil
          (.json d3 "dependency.json"
            (fn [e d]
              (.log js/console d)))))
      om/IDidMount
      (did-mount [_]
        (.json d3 "dependency.json" tree/drawTree))))
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

