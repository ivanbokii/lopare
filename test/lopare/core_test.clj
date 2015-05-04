(ns lopare.core-test
  (:require [lopare.core :as lopare]
            [cronj.core :as scheduler]
            [lopare.handlers :as handlers])
  (:use midje.sweet))

(fact "make-schedule should map job-config to a cronj schedule"
      (with-redefs [handlers/run-job ..run-job..
                    handlers/pre-job ..pre-job..
                    handlers/post-job ..post-job..]
        (lopare/make-schedule
         {:name ..job-name.. :schedule ..schedule..}) => {:id ..job-name..
                                                          :handler ..run-job..
                                                          :pre-hook ..pre-job..
                                                          :post-hook ..post-job..
                                                          :schedule ..schedule..
                                                          :opts {:name ..job-name..
                                                                 :schedule ..schedule..}}))

(fact "should schedule cronj"
      (lopare/-main) => ..cronj-started..
      (provided
       (lopare/get-config) => {:jobs [..job1.. ..job2..]}
       (lopare/make-schedule ..job1..) => ..schedule1..
       (lopare/make-schedule ..job2..) => ..schedule2..
       (scheduler/cronj :entries [..schedule1.. ..schedule2..]) => ..cronj..
       (scheduler/start! ..cronj..) => ..cronj-started..))
