(ns visitera.db.core
  (:require
   [datomic.api :as d]
   [io.rkn.conformity :as c]
   [mount.core :refer [defstate]]
   [visitera.config :refer [env]]
   [clojure.string :as str]))

(defstate conn
  :start (do (-> env :database-url d/create-database) (-> env :database-url d/connect))
  :stop (-> conn .release))

(defn delete-database
  []
  (-> env :database-url d/delete-database))

(defn install-schema
  "This function expected to be called at system start up.

  Datomic schema migraitons or db preinstalled data can be put into 'migrations/schema.edn'
  Every txes will be executed exactly once no matter how many times system restart."
  [conn]
  (let [norms-map (c/read-resource "migrations/schema.edn")]
    (c/ensure-conforms conn norms-map (keys norms-map))))

(defn show-schema
  "Show currenly installed schema"
  [conn]
  (let [system-ns #{"db" "db.type" "db.install" "db.part"
                    "db.lang" "fressian" "db.unique" "db.excise"
                    "db.cardinality" "db.fn" "db.sys" "db.bootstrap"
                    "db.alter"}]
    (d/q '[:find ?ident
           :in $ ?system-ns
           :where
           [?e :db/ident ?ident]
           [(namespace ?ident) ?ns]
           [((comp not contains?) ?system-ns ?ns)]]
         (d/db conn) system-ns)))

(defn show-transaction
  "Show all the transaction data
   e.g.
    (-> conn show-transaction count)
    => the number of transaction"
  [conn]
  (seq (d/tx-range (d/log conn) nil nil)))

(defn find-one-by
  "Given db value and an (attr/val), return the user as EntityMap (datomic.query.EntityMap)
   If there is no result, return nil.

   e.g.
    (d/touch (find-one-by (d/db conn) :user/email \"user@example.com\"))
    => show all fields
    (:user/first-name (find-one-by (d/db conn) :user/email \"user@example.com\"))
    => show first-name field"
  [db attr val]
  (d/entity db
            ;;find Specifications using ':find ?a .' will return single scalar
            (d/q '[:find ?e .
                   :in $ ?attr ?val
                   :where [?e ?attr ?val]]
                 db attr val)))


;; app domain functions
(defn add-user
  "Adds new user to a database"
  [conn {:keys [email password]}]
  (when-not (find-one-by (d/db conn) :user/email email)
    @(d/transact conn [{:user/email    email
                        :user/password password}])))

; (add-user conn {:email    "test@user.com"
;                 :password "somepass"})


(defn find-user [db email]
  "Find user by email"
  (d/touch (find-one-by db :user/email email)))

; (find-user (d/db conn) "test@user.com")


(defn get-country-id-by-alpha-3 [db alpha-3]
  (-> (find-one-by db :country/alpha-3 alpha-3)
      (d/touch)
      (:db/id)))

; (get-country-id-by-alpha-3 (d/db conn) "BLR")


(defn concat-keyword [part-1 part-2]
  "Concatenates two keywords: 
  (concat-keyword :one/two- :three) => :one/two-three"
  (let [name-1 (str/replace part-1 #"^:" "")
        name-2 (name part-2)]
    (-> (str name-1 name-2)
        (keyword))))

; (concat-keyword :user/countries- :visited)

(defn remove-from-countries [type conn user-email alpha-3]
  "Remove country from list"
  (let [user-id (-> (find-user (d/db conn) user-email)
                    (:db/id))
        country-id (get-country-id-by-alpha-3 (d/db conn) alpha-3)
        attr (concat-keyword :user/countries- type)]
    @(d/transact conn [[:db/retract user-id attr country-id]])))

; (remove-from-countries :visited conn "test@user.com" "BLR")
; (remove-from-countries :to-visit conn "test@user.com" "BLR")

(defn add-to-countries [type conn user-email alpha-3]
  "Add country to visited list"
  (when-let [country-id (get-country-id-by-alpha-3 (d/db conn) alpha-3)]
    (case type
      :visited  (remove-from-countries :to-visit conn user-email alpha-3)
      :to-visit (remove-from-countries :visited  conn user-email alpha-3))
    (let [attr (concat-keyword :user/countries- type)
          tx-user {:user/email user-email
                   attr        [country-id]}]
      @(d/transact conn [tx-user]))))

; (add-to-countries :visited conn "test@user.com" "BLR")
; (add-to-countries :to-visit conn "test@user.com" "BLR")


(defn get-countries [db user-email]
  (d/q '[:find (pull ?e
                     [{:user/countries-to-visit
                       [:country/alpha-3]}
                      {:user/countries-visited
                       [:country/alpha-3]}])
         :in $ ?user-email
         :where [?e :user/email ?user-email]]
     db user-email))

; (get-countries (d/db conn) "test@user.com")


; (d/q '[:find (pull ?e
;                    [:user/email
;                     :user/password
;                     :db/id
;                     {:user/countries-to-visit
;                      [:country/alpha-3]}
;                     {:user/countries-visited
;                      [:country/alpha-3]}])
;        :where [?e :user/email "test@user.com"]]
;      (d/db conn))
