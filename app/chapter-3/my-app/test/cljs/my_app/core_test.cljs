(ns my-app.core-test
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [pjstadig.humane-test-output]
            [my-app.core :as rc]))

(deftest test-home
  (is (= true true)))

