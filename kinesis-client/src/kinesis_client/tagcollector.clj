(ns kinesis-client.tagcollector
  (:require [kinesis-client.database :as db]
            [amazonica.aws.kinesis :refer [worker]]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [cheshire.core :as json]))

(defn get-raw-bytes [byte-buffer]
  (let [b (byte-array (.remaining byte-buffer))]
    (.get byte-buffer b)
    (log/info "string:" (String. b))
    (json/parse-string (String. b) true)))

(defn process-records [database records]
  (log/info "About to process" (count records) "records")
  (doseq [record records]
    (let [{{:keys [tag metric installationId] :as data} :data} record]
      (log/info (.getName (Thread/currentThread)) "Received data:" data)
      (if (and installationId tag metric)
        (db/upsert-tag database installationId tag metric)
        (log/warn (.getName (Thread/currentThread)) "missing installationId, tag and/or metric, ignoring")))))

(defn start-workers [database conf]
  (doall
   (for [i (range (:kinesis-workers conf))
         :let [[w uuid :as worker-record]
               (worker :credentials (:aws-creds conf)
                       :region-name (:kinesis-region conf)
                       :endpoint (:kinesis-endpoint conf)
                       :app (:kinesis-application conf)
                       :deserializer get-raw-bytes
                       :stream (:kinesis-stream conf)
                       :processor (partial process-records database))]]
     (do
       (future (.run w))
       (log/info "Started worker" i "with uuid" uuid)
       worker-record))))

(defrecord TagCollector [conf database workers]
  component/Lifecycle
  (start [component]
         (log/info "Starting TagCounter Component")
         (let [workers (start-workers database conf)]
           (assoc component :workers workers)))
  (stop [component]
        (log/info "Stopping TagCounter Component")
        (doseq [[w uuid] workers]
          (log/info "Shutting down worker" uuid)
          (.shutdown w))
        (assoc component :workers nil)))

(defn new-tagcollector [conf]
  (map->TagCollector {:conf conf}))
