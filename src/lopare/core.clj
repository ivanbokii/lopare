(ns lopare.core
  (:require [cronj.core :as scheduler]
            [lopare.handlers :as handlers]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json]
            [taoensso.timbre.appenders.rolling :as rolling])
  (:gen-class))

;;configure logging
(timbre/refer-timbre)
(timbre/set-config!
 [:appenders :rolling]
 (rolling/make-rolling-appender {:enabled? true} {:path "./log/lopare.log" :pattern :weekly}))

(defn make-schedule
  [job-config]
  (let [job {:id (:name job-config)
             :handler handlers/handler
             :pre-hook handlers/pre
             :post-hook handlers/post
             :schedule (:schedule job-config)
             :opts job-config}]
    job))

(defn get-config
  []
  (json/read-str (slurp "./jobs.json") :key-fn keyword))

(defn -main
  [& args]
  (let [jobs (:jobs (get-config))
        schedules (doall (map make-schedule jobs))
        cronj (scheduler/cronj :entries schedules)]
    (println "Lopare started")
    (scheduler/start! cronj)))
