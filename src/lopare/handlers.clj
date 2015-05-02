(ns lopare.handlers
  (:require [me.raynes.conch :as shell]
            [clojure.data.json :as json]))

(defn run-shell
  [job-config additional-param]
  (let [executable (:exec job-config)
        path-to-job-dir (str "./jobs/" (:name job-config))
        path-to-job (str path-to-job-dir "/" (:entry job-config))]
    (shell/let-programs [exec executable] (exec path-to-job (json/write-str job-config) additional-param :dir path-to-job-dir))))

(defn execute-shell-job
  [job-config additional-param]
  (try
    (run-shell job-config additional-param)
    job-config
    (catch Exception e {:error true :exception (str  e)})))

(defn pre-job
  [time job-config]
  (execute-shell-job job-config "pre"))

(defn run-job
  [time job-config]
  (if-not (:error job-config)
    (execute-shell-job job-config "")
    (println "JOB: Skip because of error on the previous step")))

(defn post-job
  [time job-config]
  (if-not (:error job-config)
    (execute-shell-job job-config "post")
    (println "POST: Skip because of error on the previous step")))
