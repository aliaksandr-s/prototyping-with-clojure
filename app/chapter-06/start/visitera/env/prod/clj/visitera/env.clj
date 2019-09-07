(ns visitera.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[visitera started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[visitera has shut down successfully]=-"))
   :middleware identity})
