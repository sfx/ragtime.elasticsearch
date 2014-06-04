(ns ragtime.elasticsearch-test
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [ragtime.core :refer [add-migration-id
                                  remove-migration-id
                                  applied-migration-ids
                                  connection]]
            [ragtime.elasticsearch :refer :all]))

(def es-url "http://localhost:9200")
(def index "ragtime")

(def test-es (connection es-url))

(defn es-fixture [f]
  (http/delete (format "%s/%s" es-url index) {:throw-exceptions false})
  (f))
(use-fixtures :once es-fixture)

(defn refresh [] (http/post (format "%s/%s/_refresh" es-url index)))

(deftest test-migrations
  (add-migration-id test-es "12")
  (add-migration-id test-es "13")
  (add-migration-id test-es "20")
  (refresh)
  (is (= ["12" "13" "20"] (applied-migration-ids test-es)))
  (remove-migration-id test-es "13")
  (refresh)
  (is (= ["12" "20"] (applied-migration-ids test-es))))
