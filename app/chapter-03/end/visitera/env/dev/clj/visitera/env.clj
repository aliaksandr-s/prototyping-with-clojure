(ns visitera.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [visitera.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[visitera started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[visitera has shut down successfully]=-"))
   :middleware wrap-dev})
