(ns lopare.handlers
  (:require [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn run-shell
  [job-config additional-param]
  (try
    (let [executable (clojure.string/split (:exec job-config) #" ")
          entry (:entry job-config)
          path-to-job-dir (str "./jobs/" (:name job-config))
          shell-params (concat executable [entry (json/write-str job-config) additional-param :dir path-to-job-dir])
          result (apply shell/sh shell-params)]
      (if (not= (:exit result) 0)
        (do
          (error (:name job-config) additional-param "Error: " result)
          (assoc job-config :error (:err result)))
        job-config))
    (catch Exception e (do
                         (error e)
                         {:name (:name job-config) :error e}))))

(defn pre-job
  [time job-config]
  (info "Starting job: " (:name job-config))
  (run-shell job-config "pre"))

(defn run-job
  [time job-config]
  (info "Running job: " (:name job-config))
  (if-not (:error job-config)
    (run-shell job-config "")
    (error (:name job-config) ": RUN is skipped because of error on the previous step")))

(defn post-job
  [time job-config]
  (if-not (or (:error (:result job-config)) (:error job-config))
    (let [result (run-shell job-config "post")]
      (when-not (:error result) (info "Finished job: " (:name job-config))))
    (error (:name job-config) ": POST is skipped because of error on the previous step")))
