
(ns lopare.handlers-test
  (:require [lopare.handlers :refer :all]
            [midje.sweet :refer :all]
            [clojure.java.shell2 :as shell]
            [clojure.data.json :as json]
            [clj-time.local :as time]))

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

(facts "save-run"
       (fact "should append to a run file"
             (let [results {:config {:name ..run-results..}}]
               (save-run results) => anything
               (provided
                (json/write-str results) => ..content..
                (spit anything "..content..\n" :append true) => anything :times 1))))

(facts "execute"
       (fact "should execute a run wrapped in the error-handler-wrapper"
             (execute ..config.. ..step..) => ..run-result..
             (provided
              (error-handler-wrapper anything) => ..run-result.. :times 1)))

(facts "pre"
       (fact "should call save-run when there is an error"
             (let [config {:name ..name..}]
               (pre config ..next.. ..finish..) => anything
               (provided
                (execute config :pre) => {:error "the error"}
                (save-run {:name ..name.. :run {:pre {:error "the error"}}}) => anything :times 1)))
       (fact "should call next function if there was no error"
             (let [config {:name ..name..}]
               (pre config ..next.. ..finish..) => anything
               (provided
                (execute config :pre) => anything
                (#'lopare.handlers/time-dump) => ..time..
                (..next.. {:run {:start-time ..time..} :name ..name.. } ..finish..) => anything :times 1))))

(facts "handler"
       (fact "should call next with a config updated with results"
             (handler {:name "job"} ..next..) => anything
             (provided
              (retry-handler anything) => ..results..
              (..next.. {:run {:handler ..results..} :name "job"}) => anything :times 1)))

(facts "retry-handler"
       (fact "should return success if no error"
             (retry-handler {:name "job"}) => {:error [] :success true}
             (provided
              (execute {:name "job"} :handler) => ..results..))
       (fact "should fail if there is an error"
             (retry-handler {:name "job"}) => {:error ["error"]}
             (provided
              (execute {:name "job"} :handler) => {:error "error"}))
       (fact "should retry and retrun success if eventually there was no error"
             (let [call (atom 0)]
               (with-redefs [execute (fn [config step] (when (= @call 0) (swap! call inc) {:error "error"}))]
                 (let [config {:name "job" :retries 1}]
                   (retry-handler config) => {:error ["error"] :success true}))))
       (fact "should fail if job always fail"
             (let [config {:name "job" :retries 1}]
               (retry-handler config) => {:error ["error" "error"]}
               (provided
                (execute config :handler) => {:error "error"}))))
(facts "post"
       (fact "should call save-run when there is was an error"
             (let [config {:name ..name..}]
               (post config) => anything
               (provided
                (save-run {:name ..name.. :run {:end-time ..time..}}) => anything :times 1
                (#'lopare.handlers/time-dump) => ..time..)))
       (fact "should save an error if there was an error"
             (let [config {:name ..name..}]
               (post config) => anything
               (provided
                (execute config :post) => {:error "error"}
                (save-run {:name ..name.. :run {:post {:error "error"}}}) => anything :times 1))))
