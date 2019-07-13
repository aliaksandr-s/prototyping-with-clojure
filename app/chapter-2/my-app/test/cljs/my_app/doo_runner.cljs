(ns my-app.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [my-app.core-test]))

(doo-tests 'my-app.core-test)

