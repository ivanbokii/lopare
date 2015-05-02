(ns lopare.handlers-test
  (:require [lopare.handlers :refer :all]
            [midje.sweet :refer :all]))

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
             (pre-job ..time.. ..job-config..) => ..job-config..
             (provided
              (execute-shell-job ..job-config.. "pre") => ..job-config..)))

(facts "run-job"
       (fact "should execute shell job with"
             (run-job ..time.. ..job-config..) => ..job-config..
             (provided
              (execute-shell-job ..job-config.. "") => ..job-config..)))
