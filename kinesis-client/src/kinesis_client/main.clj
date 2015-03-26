(ns kinesis-client.main
  (:gen-class)
  (:require
   [kinesis-client.tagcollector :as tagcollector]
   [kinesis-client.database :as database]
   [clojure.tools.logging :as log]
   [environ.core :refer [env]]
   [clj-pid.core :as pid]
   [com.stuartsierra.component :as component]))

(def aws-conf {:aws-creds {:access-key (env :aws-access-key-id)
                           :secret-key (env :aws-secret-key)
                           :endpoint (env :aws-region)}
               :kinesis-stream (env :kinesis-stream)
               :kinesis-application (env :kinesis-application)
               :kinesis-shards (env :kinesis-shards)
               :kinesis-workers (env :kinesis-workers)
               :kinesis-region (env :aws-region)
               :kinesis-endpoint (env :kinesis-endpoint)})

(def main-conf {:pidfile-name (env :pidfile-name)})

(def influx-conf {:host (env :influx-host)
                  :port (env :influx-port)
                  :scheme (env :influx-scheme)
                  :username (env :influx-username)
                  :password (env :influx-password)
                  :database (env :influx-database)})

(def conf {:aws-conf aws-conf
           :influx-conf influx-conf
           :main-conf main-conf})

(defn get-system [conf]
  "Create system by wiring individual components so that component/start
  will bring up the individual components in the correct order."
  (component/system-map
   :db            (database/new-database (:aws-conf conf))
   :tagcollector  (component/using (tagcollector/new-tagcollector
                                     (select-keys conf [:aws-conf :influx-conf]))
                                   {:database :db})))

(def system (get-system conf))

(defn -main
  "This is the kinesis-client application, that gets values from the given
  Kinesis stream, sharded on the installationid. The values are then stored
  in CloudWatch and InFluxDB.

  You must, in the configuration file profiles.clj, specify a Kinesis application
  name that is unique for the AWS account and region, like team-enterprise-app.
  You must also specify a Kinesis stream where to post the hashtags. That would perhaps
  be team-enterprise-stream. The stream will be created if it doesn't already exist."
  [& args]
  #_(pid/save (get-in conf [:main-conf :pidfile-name]))
  #_(pid/delete-on-shutdown! (get-in conf [:main-conf :pidfile-name]))
  (log/info "Application started, PID" (pid/current))
  (alter-var-root #'system component/start))
