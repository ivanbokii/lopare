(ns lopare.handlers-test
  (:require [lopare.handlers :refer :all]
            [midje.sweet :refer :all]
            [taoensso.timbre :as timbre]
            [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]))

;;avoid logging in tests
(timbre/set-level! :fatal)

(let [job {:name "job"}
      error-job {:name "job" :error true :exception "some error"}]

  (facts "run-shell"
         (let [job-config {:name "job"
                           :entry ..entry..
                           :exec ..exec..}]
           (fact "should execute a shell command and return job-config if command succeded"
                 (run-shell job-config ..param..) => job-config
                 (provided
                  (json/write-str job-config) => ..json..
                  (shell/sh ..exec.. ..entry.. ..json.. ..param.. :dir "./jobs/job") => {:exit 0}))
           (fact "should execute a shell command and return error map if command failed"
                 (run-shell job-config ..param..) => (assoc job-config :error "some error")
                 (provided
                  (json/write-str job-config) => ..json..
                  (shell/sh ..exec.. ..entry.. ..json.. ..param.. :dir "./jobs/job") => {:exit 1 :err "some error"}))))

  (facts "pre-job"
         (fact "should execute shell job with a 'pre' param"
               (pre-job ..time.. job) => job
               (provided
                (run-shell job "pre") => job)))

  (facts "run-job"
         (fact "should execute shell job when there is no error"
               (run-job ..time.. job) => job
               (provided
                (run-shell job "") => job))

         (fact "should not execute shell job when there is an error"
               (run-job ..time.. error-job) => anything
               (provided
                (run-shell error-job "") => anything :times 0)))

  (facts "post-job"
         (fact "should execute shell job with a 'post' param if there is no error"
               (post-job ..time.. job) => anything
               (provided
                (run-shell job "post") => ..job-config..))
         (fact "should not execute shell job when there is an error"
               (post-job ..time.. error-job) => anything
               (provided
                (run-shell error-job "post") => anything :times 0))))
