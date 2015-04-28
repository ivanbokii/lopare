(ns lopare.core
  (:require [carica.core :as carica]
            [cronj.core :as scheduler]
            [me.raynes.conch :as shell])
  (:gen-class))

;;set config to jobs.json
(def config (carica/configurer (carica/resources "jobs.json")))
(def override-config (carica/overrider config))

(defn run-job
  [time opts]
  (println (str time " >> " (:name opts))))

(defn make-schedule
  [job-config]
  (let [job {:id (:name job-config)
             :handler run-job
             :schedule (str "/" (get-in job-config [:repeat :minute]) " * * * * * *")
             :opts job-config}]
    (println (str "creating job for " (:name job-config)))
    job))

(defn -main
  [& args]
  (let [schedules (map make-schedule (config :jobs))
        cronj (scheduler/cronj :entries schedules)]
    (scheduler/start! cronj)))
