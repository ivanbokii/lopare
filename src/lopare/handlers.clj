(ns lopare.handlers
  (:require [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]
            [clj-time.local :as time]))

(timbre/refer-timbre)

(defn run
  [config step]
  (let [executable (clojure.string/split (:exec config) #" ")
        entry (:entry config)
        path-to-job-dir (str "./jobs/" (:name config))
        shell-params (concat executable [entry (json/write-str config) step :dir path-to-job-dir])]
    (apply shell/sh shell-params)))

(defn error-handler
  [fn]
  (try
    (let [result (fn)]
      (when-not (= (:exit result) 0)
        {:error result}))
    (catch Exception e {:error e})))

(defn save-run
  [run-result]
  (spit "last-run" run-result))

(defn pre
  [time config]
  (let [execute (partial run config :pre)
        result (error-handler execute)
        error (:error result)]
    (if error
      {:config config :pre {:error error} :error true :start-time time}
      {:config config :pre {:start-time time :end-time (time/local-now)} :start-time time})))

(defn handler
  [time pre-result]
  (when-not (:error pre-result)
    (let [execute (partial run (:config pre-result) :handler)
          result (error-handler execute)
          error (:error result)]
      (if error
        {:error error}
        {:start-time time :end-time (time/local-now)}))))

(defn post
  [time handler-result]
  (when-not (or (:error handler-result) (:error (:result handler-result)))
    (let [execute (partial run (:config handler-result) :post)
          result (error-handler execute)
          error (:error result)]
      (if error
        (save-run (assoc handler-result :post {:error error} :end-time (time/local-now)))
        (save-run (assoc handler-result :post {:start-time time :end-time (time/local-now)}))))))
