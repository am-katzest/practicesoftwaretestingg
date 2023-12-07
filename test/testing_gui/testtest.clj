(ns testtest
  (:require [clojure.test :refer :all]
            [new-ways.clj-cukes :refer [Given run-gherkin-file When Then And]]))

(Given #"state being (\d+)" [state num]
       (assoc state :value (parse-long num)))

(When #"I add (\d+)" [state num]
       (update state :value + (parse-long num)))

(Then #"the state should be (\d+)" [state num]
      (is (= (parse-long num) (:value state))))

(deftest handler
  (run-gherkin-file "test/features/testtest.feature"))
