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
       (fact "should execute shell job when there is no error"
             (run-job ..time.. ..job-config..) => ..job-config..
             (provided
              (execute-shell-job ..job-config.. "") => ..job-config..))
       (fact "should not execute shell job when there is an error"
             (run-job ..time.. {:error true :exception "some error"}) => anything
             (provided
              (execute-shell-job ..job-config.. "") => anything :times 0)))

(facts "post-job"
       (fact "should execute shell job with a 'post' param if there is no error"
             (post-job ..time.. ..job-config..) => anything
             (provided
              (execute-shell-job ..job-config.. "post") => ..job-config..))
      (fact "should not execute shell job when there is an error"
             (post-job ..time.. {:error true :exception "some error"}) => anything
             (provided
              (execute-shell-job ..job-config.. "post") => anything :times 0)))
