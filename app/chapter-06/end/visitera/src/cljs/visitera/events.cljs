(ns visitera.events
  (:require
   [re-frame.core :as rf]
   [ajax.core :as ajax]
   [ajax.edn :as ajax-edn]
   [visitera.config :as cfg]))

;;db
(rf/reg-event-db
 :navigate
 (fn [db [_ route]]
   (assoc db :route route)))

(rf/reg-event-db
 :common/set-error
 (fn [db [_ error]]
   (assoc db :common/error error)))

(rf/reg-event-db
 :set-countries
 (fn [db [_ countries]]
   (assoc db :countries countries)))

(rf/reg-event-db
 :set-last-updated
 (fn [db [_ country]]
   (assoc db :last-updated country)))

;;fx
(rf/reg-event-fx
 :fetch-user-countries
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             "/api/user-countries"
                 :response-format (ajax-edn/edn-response-format)
                 :on-success      [:set-countries]
                 :on-failure      [:common/set-error]}}))

(rf/reg-event-fx
 :update-user-countries
 (fn [{:keys [db]} [_ country]]
   {:http-xhrio {:method          :put
                 :uri             "/api/user-countries"
                 :params          country
                 :format          (ajax-edn/edn-request-format)
                 :response-format (ajax-edn/edn-response-format)
                 :on-success      [:set-countries]
                 :on-failure      [:common/set-error]}
    :dispatch [:set-last-updated country]}))

;;subscriptions
(rf/reg-sub
 :route
 (fn [db _]
   (-> db :route)))

(rf/reg-sub
 :page
 :<- [:route]
 (fn [route _]
   (-> route :data :name)))

(rf/reg-sub
 :docs
 (fn [db _]
   (:docs db)))

(rf/reg-sub
 :common/error
 (fn [db _]
   (:common/error db)))

(rf/reg-sub
 :countries
 (fn [db _]
   (:countries db)))

(defn- normalize-countries [countries]
  (into [] cat [(->> (:visited countries)
                     (map (fn [c-id] {:id     c-id
                                      :fill   (:visited cfg/colors)
                                      :status :visited})))
                (->> (:to-visit countries)
                     (map (fn [c-id] {:id     c-id
                                      :fill   (:to-visit cfg/colors)
                                      :status :to-visit})))]))

(rf/reg-sub
 :visited-count
 (fn [db _]
   (-> db :countries :visited count)))

(rf/reg-sub
 :to-visit-count
 (fn [db _]
   (-> db :countries :to-visit count)))

(rf/reg-sub
 :normalized-countries
 (fn []
   (rf/subscribe [:countries]))
 (fn [countries]
   (normalize-countries countries)))

(rf/reg-sub
 :last-updated
 (fn [db _]
   (:last-updated db)))