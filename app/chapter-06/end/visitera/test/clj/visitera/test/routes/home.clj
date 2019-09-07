(ns visitera.test.routes.home
  (:require
   [clojure.test :refer :all]
   [ring.mock.request :as mock]
   [mount.core :as mount]
   [visitera.db.core :refer [install-schema conn delete-database]]
   [visitera.routes.home :refer [register-handler! login-handler logout-handler]]))

(defn response-errors [response]
  (-> (get-in response [:flash :errors])
      (keys)))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'visitera.config/env
                 #'visitera.db.core/conn)
    (install-schema conn)
    (f)
    (delete-database)
    (mount/stop  #'visitera.config/env
                 #'visitera.db.core/conn)))

(deftest register-handler-test
  (testing "Bad input:"
    (testing "email and password should not be empty"
      (let [req {:params {:email "" :password ""}}
            response (register-handler! req)]
        (is (= [:email :password] (response-errors response)))))

    (testing "email and password should have correct format"
      (let [req {:params {:email "not-email.com" :password "short"}}
            response (register-handler! req)]
        (is (= [:email :password] (response-errors response))))))

  (testing "Correct input:"
    (testing "should create a user with correct email and password, and redirect to login"
      (let [req {:params {:email "some@email.com" :password "somepass"}}
            response (register-handler! req)]
        (is (= "/login" (get (:headers response) "Location")))))

    (testing "should return an error if user alredy exists"
      (let [req {:params {:email "some@email.com" :password "somepass"}}
            response (register-handler! req)]
        (is (= [:email] (response-errors response)))))))

(deftest login-handler-test
  ; register a user for testing
  (let [reg-req {:params {:email "test@email.com" :password "correctpass"}}]
    (register-handler! reg-req))

  (testing "email and password should not be empty"
    (let [req {:params {:email "" :password ""}}
          response (login-handler req)]
      (is (= [:email :password] (response-errors response)))))

  (testing "email should have correct format"
    (let [req {:params {:email "not-email.com" :password ""}}
          response (login-handler req)]
      (is (= [:email :password] (response-errors response)))))

  (testing "should return an error if user does not exist"
    (let [req {:params {:email "non-exist@email.com" :password "somepass"}}
          response (login-handler req)]
      (is (= [:email] (response-errors response)))))

  (testing "the password should be correct"
    (let [req {:params {:email "test@email.com" :password "wrongpass"}}
          response (login-handler req)]
      (is (= [:password] (response-errors response)))))

  (testing "should add identity field to session and redirect to "/" for correct input"
    (let [req {:params {:email "test@email.com" :password "correctpass"}}
          response (login-handler req)]
      (are [expected received] (= expected received)
        "/" (get (:headers response) "Location")
        (keyword "test@email.com") (get-in response [:session :identity])))))

(deftest logout-handler-test
  (testing "should clean up a session and redirect to /login"
    (let [req {}
          response (logout-handler req)]
      (is (= {} (:session response))))))