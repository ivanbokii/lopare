(ns lopare.handlers-test
  (:require [lopare.handlers :refer :all]
            [midje.sweet :refer :all]
            [taoensso.timbre :as timbre]))

;;avoid logging in tests
(timbre/set-level! :fatal)

(let [job {:name "job"}
      error-job {:name "job" :error true :exception "some error"}]

  (facts "execute-shell-job"
         (fact "should execute an external app using shell"
               (execute-shell-job ..job-config.. ..additional-param..) => ..job-config..
               (provided
                (run-shell ..job-config.. ..additional-param..) => ..let-programs-result..))
         (fact "should return error map if job throws"
               (execute-shell-job job ..additional-param..) => {:name "job" :error true :exception "java.lang.Exception: error"}
               (provided
                (run-shell job ..additional-param..) =throws=> (Exception. "error"))))

  (facts "pre-job"
         (fact "should execute shell job with a 'pre' param"
               (pre-job ..time.. job) => job
               (provided
                (execute-shell-job job "pre") => job)))

  (facts "run-job"
         (fact "should execute shell job when there is no error"
               (run-job ..time.. job) => job
               (provided
                (execute-shell-job job "") => job))

         (fact "should not execute shell job when there is an error"
               (run-job ..time.. error-job) => anything
               (provided
                (execute-shell-job error-job "") => anything :times 0)))

  (facts "post-job"
         (fact "should execute shell job with a 'post' param if there is no error"
               (post-job ..time.. job) => anything
               (provided
                (execute-shell-job job "post") => ..job-config..))
         (fact "should not execute shell job when there is an error"
               (post-job ..time.. error-job) => anything
               (provided
                (execute-shell-job error-job "post") => anything :times 0))))
