(ns kinesis-client.database
  (:gen-class)
  (:require [amazonica.aws.cloudwatch :as cloudwatch]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [component]
         (log/info "Starting Database Component")
         component)
  (stop [component]
        (log/info "Stopping Database Component")
        component))

(defn new-database [config]
  (map->Database {:config config}))

(defn upsert-tag [database installation tag metric]
  (cloudwatch/put-metric-data
    (get-in database [:config :aws-creds])
    :namespace   "iiot-team-enterprise"
    :metric-data [{:metric-name tag
                   :unit "Count"
                   :dimensions [{:name "Installation" :value installation}]
                   :value metric}]))
