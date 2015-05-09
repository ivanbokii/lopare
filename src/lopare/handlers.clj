(ns lopare.handlers
  (:require [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json]
            [clj-time.local :as time]))

(timbre/refer-timbre)

(defn run
  [config step]
  (let [executable (clojure.string/split (:exec config) #" ")
        entry (:entry config)
        path-to-job-dir (str "./jobs/" (:name config))
        shell-params (concat executable [entry (json/write-str (:arg config)) (name step) :dir path-to-job-dir])]
    (apply shell/sh shell-params)))

(defn error-handler-wrapper
  [fn]
  (try
    (let [result (fn)]
      (when-not (= (:exit result) 0)
        {:error result}))
    (catch Exception e {:error (.getMessage e)})))

(defn save-run
  [run-result]
  (spit (str "./last-runs/" (:name (:config run-result))) (str (json/write-str run-result) "\n") :append true))

(defn pre
  [time config]
  (let [execute (partial run config :pre)
        result (error-handler-wrapper execute)
        error (:error result)]
    (if error
      {:config config :pre {:error error} :error true :start-time (str time)}
      {:config config :pre {:start-time (str time) :end-time (str (time/local-now))} :start-time (str time)})))

(defn handler
  [time pre-result]
  (when-not (:error pre-result)
    (let [execute (partial run (:config pre-result) :handler)
          result (error-handler-wrapper execute)
          error (:error result)]
      (if error
        {:error error}
        {:start-time (str time) :end-time (str (time/local-now))}))))

(defn post
  [time handler-result]
  (if-not (or (:error handler-result) (:error (:result handler-result)))
    (let [execute (partial run (:config handler-result) :post)
          result (error-handler-wrapper execute)
          error (:error result)]
      (if error
        (save-run (assoc handler-result :post {:error error} :end-time (str (time/local-now))))
        (save-run (assoc handler-result :post {:start-time (str time) :end-time (str (time/local-now))} :end-time (str (time/local-now)))))))
  (save-run (assoc handler-result :post {:skipped true})))
