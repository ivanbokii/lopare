(ns lopare.handlers-test
  (:require [lopare.handlers :refer :all]
            [midje.sweet :refer :all]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(with-redefs [info ..info..
              error ..error..]
  (let [job {:name "job"}
        error-job {:name "job" :error true :exception "some error"}]

    (facts "execute-shell-job"
           (fact "should execute an external app using shell"
                 (execute-shell-job ..job-config.. ..additional-param..) => ..job-config..
                 (provided
                  (run-shell ..job-config.. ..additional-param..) => ..let-programs-result..))
           (fact "should return error map if job throws"
                 (execute-shell-job ..job-config.. ..additional-param..) => {:error true :exception "java.lang.Exception: error"}
                 (provided
                  (run-shell ..job-config.. ..additional-param..) =throws=> (Exception. "error"))))

    (facts "pre-job"
           (fact "should execute shell job with a 'pre' param"
                 (pre-job ..time.. job) => job
                 (provided
                  (execute-shell-job job "pre") => job
                  (info "Starting job: " "job") => ..something.. :times 1)))

    (facts "run-job"
           (fact "should execute shell job when there is no error"
                 (run-job ..time.. job) => job
                 (provided
                  (execute-shell-job job "") => job
                  (info "Running job: " "job") => ..something.. :times 1))

           (fact "should not execute shell job when there is an error"
                 (run-job ..time.. error-job) => anything
                 (provided
                  (execute-shell-job error-job "") => anything :times 0
                  (error "job" ": RUN is skipped because of error on the previous step") => anything :times 1)))

    (facts "post-job"
           (fact "should execute shell job with a 'post' param if there is no error"
                 (post-job ..time.. job) => anything
                 (provided
                  (execute-shell-job job "post") => ..job-config..
                  (info "Finishing job: " "job") => anything :times 1
                  (info "Finished job: " "job") => anything :times 1))
           (fact "should not execute shell job when there is an error"
                 (post-job ..time.. error-job) => anything
                 (provided
                  (execute-shell-job error-job "post") => anything :times 0
                  (error "job" ": POST is skipped because of error on the previous step") => anything :times 1)))))
