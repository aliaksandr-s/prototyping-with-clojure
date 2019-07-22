(ns visitera.routes.home
  (:require
   [visitera.layout :as layout]
   [clojure.java.io :as io]
   [visitera.middleware :as middleware]
   [ring.util.http-response :as response]
   [visitera.db.core :refer [conn find-user]]
   [datomic.api :as d]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/db-test" {:get (fn [_]
                       (let [db (d/db conn)
                             user (find-user db "abc")]
                         (-> (response/ok (:user/name user))
                             (response/header "Content-Type" "text/plain; charset=utf-8"))))}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])
