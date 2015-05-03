(ns lopare.handlers
  (:require [me.raynes.conch.low-level :as shell]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn run-shell
  [job-config additional-param]
  (let [executable (:exec job-config)
        path-to-job-dir (str "./jobs/" (:name job-config))
        path-to-job (str path-to-job-dir "/" (:entry job-config))]
    (shell/proc executable path-to-job (json/write-str job-config additional-param :dir path-to-job-dir))))

(defn execute-shell-job
  [job-config additional-param]
  (try
    (run-shell job-config additional-param)
    job-config
    (catch Exception e {:name (:name job-config) :error true :exception (str  e)})))

(defn pre-job
  [time job-config]
  (info "Starting job: " (:name job-config))
  (execute-shell-job job-config "pre"))

(defn run-job
  [time job-config]
  (info "Running job: " (:name job-config))
  (if-not (:error job-config)
    (execute-shell-job job-config "")
    (error (:name job-config) ": RUN is skipped because of error on the previous step" job-config)))

(defn post-job
  [time job-config]
  (info "Finishing job: " (:name job-config))
  (if-not (:error job-config)
    (do
      (execute-shell-job job-config "post")
      (info "Finished job: " (:name job-config)))
    (error (:name job-config) ": POST is skipped because of error on the previous step")))
