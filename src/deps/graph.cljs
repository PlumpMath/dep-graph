(ns ^:figwheel-always deps.graph
    (:require [clojure.string :as str]
              [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [deps.tree :as tree]
              cljsjs.d3))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:ns ""}))

(def d3 (.-d3 js/window))

(defn draw! [data]
  (.json d3 "Komunike.json"
    (fn [json]
      (tree/drawTree "#graph"
        (clj->js (str/split (:ns data) " "))
        json))))

(om/root
  (fn [data owner]
    (reify
      om/IRender
      (render [_]
        (dom/div nil
          (dom/span #js {}
            "filter ns: "
            (dom/input #js {:type "text"
                            :value (:ns data)
                            :onKeyDown (fn [e]
                                         (when (= "Enter" (.-key e))
                                           (draw! data)))
                            :onChange (fn [e]
                                        (om/update! data :ns
                                          (.. e -target -value)))}))
          (dom/br #js {})
          (dom/span #js {}
            "highlight ns: "
            (dom/input #js {:type "text"
                            :value (:hg data)
                            :onKeyDown (fn [e]
                                         (when (= "Enter" (.-key e))
                                           (draw! data)))
                            :onChange (fn [e]
                                        (om/update! data :ns
                                          (.. e -target -value)))}))
          (dom/svg #js {:id "graph"}
            (dom/g #js {}))))
      om/IDidMount
      (did-mount [_]
        (draw! data))))
  app-state
  {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

