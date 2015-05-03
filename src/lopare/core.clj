(ns lopare.core
  (:require [carica.core :as carica]
            [cronj.core :as scheduler]
            [lopare.handlers :as handlers]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rolling :as rolling])
  (:gen-class))

;;configure logging
(timbre/refer-timbre)
(timbre/set-config!
 [:appenders :rolling]
 (rolling/make-rolling-appender {:enabled? true} {:path "log/lopare.log" :pattern :weekly}))

;;set config to jobs.json
(def config (carica/configurer (carica/resources "jobs.json")))
(def override-config (carica/overrider config))

(defn make-schedule
  [job-config]
  (let [job {:id (:name job-config)
             :handler handlers/run-job
             :pre-hook handlers/pre-job
             :post-hook handlers/post-job
             :schedule (:schedule job-config)
             :opts job-config}]
    job))

(defn -main
  [& args]
  (let [jobs (config :jobs)
        schedules (doall (map make-schedule jobs))
        cronj (scheduler/cronj :entries schedules)]
    (scheduler/start! cronj)))
