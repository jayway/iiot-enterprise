(ns kinesis-client.database
  (:gen-class)
  (:require [amazonica.aws.cloudwatch :as cloudwatch]
            [capacitor.core :as influx]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defrecord Database [db-spec influx-client]
  component/Lifecycle
  (start [component]
         (log/info "Starting Database Component")
    (let [influx-client
          (influx/make-client (:influx-conf component))]
      (log/info "Created influx client" influx-client)
      (assoc component :influx-client influx-client)))
  (stop [component]
        (log/info "Stopping Database Component")
    (assoc component :influx-client nil)))

(defn new-database [config]
  (map->Database {:config config}))

(defn put-cloudwatch-metric [database {:keys [timestamp installationId tag metric]}]
  (log/info "Put metric data to CloudWatch")
  (let [data-to-put {:metric-name tag
                     :unit        "Count"
                     :timestamp   timestamp
                     :dimensions  [{:name "Installation" :value installationId}]
                     :value       (if (string? metric) (Double/parseDouble metric) metric)}]
    (log/info "data to put" data-to-put)
    (cloudwatch/put-metric-data
      (get-in database [:config :aws-creds])
      :namespace "iiot-team-enterprise"
      :metric-data [data-to-put])))

(defn put-influx-metric [database {:keys [timestamp installation tag metric]}]
  (log/info "Put metric data to InFluxDB using client" (:influx-client database))
  (influx/post-points 
    (:influx-client database)
    "iiot-team-enterprise"
    [{:metric-name    tag
      :timestamp      timestamp
      :installationId installation
      :metric         metric}]))
