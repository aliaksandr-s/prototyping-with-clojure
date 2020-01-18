(ns visitera.components.map
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [visitera.config :as cfg]
   [goog.object :as g]))

(defn legend-tag
  [color]
  [:span
   {:style {:display          "inline-block"
            :width            "1.3rem"
            :height           "1.3rem"
            :border-radius    "5px"
            :background-color color}}])

(defn legend-row
  [color text count]
  [:div
   {:style {:display               "grid"
            :grid-template-columns "1.7rem 7rem 1.7rem"}}
   [legend-tag color]
   [:span text]
   [:span count]])

(defn legend-comp
  [to-visit-count visited-count]
  [:div
   {:style {:position "fixed"
            :top      "60%"
            :left     "5%"}}
   [legend-row
    (cfg/colors :to-visit)
    "Want to visit: "
    to-visit-count]
   [legend-row
    (cfg/colors :visited)
    "Already visited: "
    visited-count]])

(defn- get-next-status [status]
  (case status
    nil          :to-visit
    :not-visited :to-visit
    :to-visit    :visited
    :visited     :not-visited))

(defn map-component-inner
  [countries last-updated]
  (let [chart-ref (r/atom nil)
        polygon-ref (r/atom nil)

        on-country-click (fn [ev]
                           (let [country-id (g/getValueByKeys ev "target" "dataItem" "dataContext" "id")
                                 status (keyword (g/getValueByKeys ev "target" "dataItem" "dataContext" "status"))]
                             (rf/dispatch [:update-user-countries {:status (get-next-status status)
                                                                   :id country-id}])))

        create (fn [this]
                 ; Define globals
                 (def am4core (.-am4core js/window))
                 (def am4maps (.-am4maps js/window))
                 (def am4geodata_worldLow (.-am4geodata_worldLow js/window))

                 ; Create map instance
                 (def chart (.create am4core "chartdiv" (.-MapChart am4maps)))
                 (swap! chart-ref (fnil identity chart)) ;save to preperly destroy

                 ; Config chart
                 (set! (.-geodata chart) am4geodata_worldLow)
                 (set! (.-projection chart) (new (.-Miller (.-projections am4maps))))
                 (set! (.-zoomControl chart) (new (.-ZoomControl am4maps)))

                 ; Make map load polygon (like country names) data from GeoJSON
                 (def polygonSeries (.push (.-series chart) (new (.-MapPolygonSeries am4maps))))
                 (swap! polygon-ref (fnil identity polygonSeries))

                 ; remove antarctica
                 (set! (.-exclude polygonSeries) #js ["AQ"])

                 ; Make map load polygon (like country names) data from GeoJSON
                 (set! (.-useGeodata polygonSeries) true)

                 ; Configure series
                 (def polygonTemplate (.. polygonSeries -mapPolygons -template))
                 (set! (.-tooltipText polygonTemplate) "{name}")
                 (set! (.-fill polygonTemplate) (:not-visited cfg/colors))
                 (.on (.-events polygonTemplate) "hit" on-country-click)

                 ; set initial data
                 (set! (.-data polygonSeries) (clj->js countries))
                 ; Bind "fill" property to "fill" key in data
                 (set! (.. polygonTemplate -propertyFields -fill) "fill"))

        update (fn [comp]
                 (let [last-updated (second (rest (r/argv comp)))
                       polygon (. (g/get @polygon-ref "getPolygonById") call @polygon-ref (:id last-updated))]
                   (g/set (g/getValueByKeys polygon "dataItem" "dataContext") "status" (name (:status last-updated))) ;change status
                   (set! (.-fill polygon) ((:status last-updated) cfg/colors)))) ;change color

        destroy (fn []
                  (if @chart-ref (do (.dispose @chart-ref)
                                     (reset! chart-ref nil)
                                     (reset! polygon-ref nil))))]
    (r/create-class
     {:display-name  "map-component"
      :reagent-render (fn []
                        [:div {:id "chartdiv"
                               :style {:width "100%"
                                       :height "calc(100vh - 5rem)"}}])
      :component-did-mount
      create
      :component-will-unmount
      destroy
      :component-did-update
      update})))


(defn map-component []
  (let [norm-countries (rf/subscribe [:normalized-countries])
        countries      (rf/subscribe [:countries])
        last-updated   (rf/subscribe [:last-updated])
        visited-count  (rf/subscribe [:visited-count])
        to-visit-count (rf/subscribe [:to-visit-count])]
    (fn []
      (if @countries
        [:div
         [:div [map-component-inner @norm-countries @last-updated]]
         [:div [legend-comp @to-visit-count @visited-count]]]
        [:div "Loading"]))))