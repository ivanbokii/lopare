(ns lopare.handlers-test
  (:require [lopare.handlers :refer :all]
            [midje.sweet :refer :all]
            [taoensso.timbre :as timbre]
            [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]
            [clj-time.local :as time]))

;;avoid logging in tests
(timbre/set-level! :fatal)

(facts "run"
       (fact "should run shell"
             (run {:name "job" :exec "exec" :entry "entry" :arg "arguments"} :pre) => {:exit 0 :error "" :output "nothing"}
             (provided
              (shell/sh "exec" "entry" "\"arguments\"" "pre" :dir "./jobs/job") => {:exit 0 :error "" :output "nothing"})))

(facts "error handler wrapper"
       (fact "should return an error object if fn returned an error"
             (error-handler-wrapper (fn [] {:exit 1 :error "the error"})) => {:error {:exit 1 :error "the error"}})
       (fact "should return an error if fn throws"
             (error-handler-wrapper (fn [] (throw (Exception. "error")))) => {:error "error"}))

(facts "pre"
       (fact "should return correct map when no error"
             (pre ..start-time.. ..config..) => {:config ..config.. :pre {:start-time ..start-time.. :end-time ..end-time..} :start-time ..start-time..}
             (provided
              (error-handler-wrapper anything) => ..job-result..
              (time/local-now) => ..end-time..))
       (fact "should return error map when error"
             (pre ..start-time.. ..config..) => {:config ..config.. :pre {:error "the error"} :error true :start-time ..start-time..}
             (provided
              (error-handler-wrapper anything) => {:error "the error" :exit 1})))
