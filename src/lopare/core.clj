(ns lopare.core
  (:require [cronj.core :as scheduler]
            [lopare.handlers :as handlers]
            [clojure.data.json :as json])
  (:gen-class))

(defn make-schedule
  [job-config]
  (let [job {:id (:name job-config)
             :handler handlers/handle
             :schedule (:schedule job-config)
             :opts job-config}]
    job))

(defn get-config []
  (json/read-str (slurp "./jobs.json") :key-fn keyword))

(defn -main [& args]
  (let [jobs (:jobs (get-config))
        schedules (doall (map make-schedule jobs))
        cronj (scheduler/cronj :entries schedules)]
    (println "Lopare started")
    (scheduler/start! cronj)))
