(ns visitera.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [visitera.core-test]))

(doo-tests 'visitera.core-test)

