(ns lopare.handlers
  (:require [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]
            [clj-time.local :as time]))

(defn- time-dump []
  (str (java.util.Date.)))

(defn run [config step]
  (let [
        executable (clojure.string/split (:exec config) #" ")
        entry (:entry config)
        path-to-job-dir (str "./jobs/" (:name config))
        shell-params (concat executable [entry (json/write-str (:arg config)) (name step) :dir path-to-job-dir])]
    (apply shell/sh shell-params)))

(defn error-handler-wrapper [fun]
  (try
    (let [result (fun)]
      (when-not (= (:exit result) 0)
        {:error result}))
    (catch Exception e {:error (.getMessage e)})))

(defn save-run [run-result]
  (let [results (:run run-result)
        path (str "./last-runs/" (:name (:config run-result)))
        content (str (json/write-str run-result) "\n")]
    (spit path content :append true)))

(defn execute [config step]
  (let [configured-run (partial run config step)
        result (error-handler-wrapper configured-run)]
    result))

(defn pre [config next finish]
  (let [job-name (:name config)
        results (execute config :pre)]
    (println job-name "pre step running")
    (if-let [error (:error results)]
      (do (println job-name "pre step failed")
          (save-run {:pre {:error error}}))
      (let [config-with-time (assoc config :run {:start-time (time-dump)})]
        (next config-with-time finish)))))

(defn retry-handler [config]
  (loop [job-name (:name config)
         retries (or (:retries config) 0)
         errors []
         results (execute config :handler retries)]
    (if-let [error (:error results)]
      (do
        (println job-name "handler failed. Retries" retries)
        (if (> retries 0)
          (recur job-name (dec retries) (conj errors error) (execute config :handler))
          {:error (conj errors error)}))
      {:error errors :success true})))

(defn handler [config next]
  (do
    (println (:name config) "handler step running")
    (let [results (retry-handler config)
          updated-config (assoc-in config [:run :handler] results)]
      (next updated-config))))

(defn post [config]
  (let [job-name (:name config)
        results (execute config :post)]
    (println job-name "post step running")
    (if-let [error (:error results)]
      (do (println job-name "post failed")
          (save-run (assoc-in [:run :post] {:error error})))
      (let [config-with-time (assoc-in config [:run :end-time] (time-dump))]
        (save-run config-with-time)))))

(defn handle [time config] (pre config handler post))
