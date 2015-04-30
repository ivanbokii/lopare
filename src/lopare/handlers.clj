(ns lopare.handlers
  (:require [me.raynes.conch :as shell]
            [clojure.data.json :as json]))

(defn execute-shell-job
  [job-config additional-param]
  (let [path-to-node "/usr/local/bin/node"
        path-to-job-dir (str "./jobs/" (:name job-config) "/")
        path-to-job (str "./jobs/" (:name job-config) "/" (:entry job-config))]
    (try
      (shell/let-programs [node path-to-node]
                          (node path-to-job (json/write-str job-config) additional-param :dir path-to-job-dir))
      job-config
      (catch Exception e (do
                           (println "Caught exception: " e)
                           {:error true :exception e})))))

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
