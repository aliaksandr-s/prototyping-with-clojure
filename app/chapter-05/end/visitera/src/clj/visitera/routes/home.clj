(ns visitera.routes.home
  (:require
   [clojure.java.io :as io]
   [visitera.layout :refer [register-page login-page home-page]]
   [visitera.middleware :as middleware]
   [ring.util.http-response :as response]
   [visitera.db.core :refer [conn find-user add-user]]
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
        (let [updated-session (assoc session :identity (keyword (:email params)))]
          (-> (response/found "/")
              (assoc :session updated-session)))))))

(defn logout-handler [request]
  (-> (response/found "/login")
      (assoc :session {})))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page
         :middleware [middleware/wrap-restricted]}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/register" {:get register-page
                 :post register-handler!}]
   ["/login" {:get login-page
              :post login-handler}]
   ["/logout" {:get logout-handler}]])
