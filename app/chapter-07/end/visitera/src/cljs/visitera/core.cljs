(ns visitera.core
  (:require
   [day8.re-frame.http-fx]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   [visitera.ajax :as ajax]
   [visitera.events]
   [reitit.core :as reitit]
   [visitera.components.map :refer [map-component]]
   [visitera.components.navbar :refer [navbar]]
   [clojure.string :as string])
  (:import goog.History))

(defn home-page []
  [:div.content
   [map-component]])

(def pages
  {:home #'home-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :home]]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (let [uri (or (not-empty (string/replace (.-token event) #"^.*#" "")) "/")]
         (rf/dispatch
          [:navigate (reitit/match-by-path router uri)]))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:navigate (reitit/match-by-name router :home)])
  (ajax/load-interceptors!)
  (rf/dispatch [:fetch-user-countries])
  (hook-browser-navigation!)
  (mount-components))
