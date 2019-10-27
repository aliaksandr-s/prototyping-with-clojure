(ns visitera.routes.home
  (:require
   [clojure.java.io :as io]
   [visitera.layout :refer [register-page login-page home-page]]
   [visitera.middleware :as middleware]
   [ring.util.http-response :as response]
   [visitera.db.core :refer [conn find-user add-user get-countries update-countries]]
   [datomic.api :as d]
   [visitera.validation :refer [validate-register validate-login]]
   [buddy.hashers :as hs]))

(defn register-handler! [{:keys [params]}]
  (if-let [errors (validate-register params)]
    (-> (response/found "/register")
        (assoc :flash {:errors errors
                       :email (:email params)}))
    (if-not (add-user conn params)
      (-> (response/found "/register")
          (assoc :flash {:errors {:email "User with that email already exists"}
                         :email (:email params)}))
      (-> (response/found "/login")
          (assoc :flash {:messages {:success "User is registered! You can log in now."}
                         :email (:email params)})))))

(defn password-valid? [user pass]
  (hs/check pass (:user/password user)))

(defn login-handler [{:keys [params session]}]
  (if-let [errors (validate-login params)]
    (-> (response/found "/login")
        (assoc :flash {:errors errors
                       :email (:email params)}))
    (let [user (find-user (d/db conn) (:email params))]
      (cond
        (not user)
        (-> (response/found "/login")
            (assoc :flash {:errors {:email "user with that email does not exist"}
                           :email (:email params)}))
        (and user
             (not (password-valid? user (:password params))))
        (-> (response/found "/login")
            (assoc :flash {:errors {:password "The password is wrong"}
                           :email (:email params)}))
        (and user
             (password-valid? user (:password params)))
        (let [updated-session (assoc session :identity (:email params))]
          (-> (response/found "/")
              (assoc :session updated-session)))))))

(defn logout-handler [request]
  (-> (response/found "/login")
      (assoc :session {})))

(defn get-user-countries-handler [{:keys [session]}]
  (let [email (:identity session)]
    (-> (response/ok (pr-str (get-countries (d/db conn) email)))
        (response/header "Content-Type" "application/edn"))))

(defn put-user-countries-handler [{:keys [params session]}]
  (let [email (:identity session)
        status (:status params)
        country (:id params)]
    (try
      (update-countries conn email status country)
      (-> (response/ok (pr-str (get-countries (d/db conn) email)))
          (response/header "Content-Type" "application/edn"))
      (catch Exception e (response/bad-request
                          (str "Error: " (.getMessage e)))))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page
         :middleware [middleware/wrap-restricted]}]
   ["/api"
    {:middleware [middleware/wrap-restricted]}
    ["/user-countries"
     ["" {:get get-user-countries-handler
          :put put-user-countries-handler}]]]
   ["/register" {:get register-page
                 :post register-handler!}]
   ["/login" {:get login-page
              :post login-handler}]
   ["/logout" {:get logout-handler}]])
