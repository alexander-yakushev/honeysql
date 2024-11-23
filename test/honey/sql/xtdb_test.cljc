;; copyright (c) 2020-2024 sean corfield, all rights reserved

(ns honey.sql.xtdb-test
  (:require [clojure.test :refer [deftest is testing]]
            [honey.sql :as sql]
            [honey.sql.helpers :as h
             :refer [select exclude rename from where]]))

(deftest select-tests
  (testing "select, exclude, rename"
    (is (= ["SELECT * EXCLUDE _id RENAME value AS foo_value FROM foo"]
           (sql/format (-> (select :*) (exclude :_id) (rename [:value :foo_value])
                           (from :foo)))))
    (is (= ["SELECT * EXCLUDE (_id, a) RENAME value AS foo_value FROM foo"]
           (sql/format (-> (select :*) (exclude :_id :a) (rename [:value :foo_value])
                           (from :foo)))))
    (is (= ["SELECT * EXCLUDE _id RENAME (value AS foo_value, a AS b) FROM foo"]
           (sql/format (-> (select :*) (exclude :_id)
                           (rename [:value :foo_value]
                                   [:a :b])
                           (from :foo)))))
    (is (= ["SELECT * EXCLUDE _id RENAME value AS foo_value, c.x FROM foo"]
           (sql/format (-> (select [:* (-> (exclude :_id) (rename [:value :foo_value]))]
                                   :c.x)
                           (from :foo)))))
    (is (= ["SELECT * EXCLUDE (_id, a) RENAME value AS foo_value, c.x FROM foo"]
           (sql/format (-> (select [:* (-> (exclude :_id :a) (rename [:value :foo_value]))]
                                   :c.x)
                           (from :foo)))))
    (is (= ["SELECT * EXCLUDE _id RENAME (value AS foo_value, a AS b), c.x FROM foo"]
           (sql/format (-> (select [:* (-> (exclude :_id)
                                           (rename [:value :foo_value]
                                                   [:a :b]))]
                                   :c.x)
                           (from :foo))))))
  (testing "select, nest_one, nest_many"
    (is (= ["SELECT a._id, NEST_ONE (SELECT * FROM foo AS b WHERE b_id = a._id) FROM bar AS a"]
           (sql/format '{select (a._id,
                                 ((nest_one {select * from ((foo b)) where (= b_id a._id)})))
                         from ((bar a))})))
    (is (= ["SELECT a._id, NEST_MANY (SELECT * FROM foo AS b) FROM bar AS a"]
           (sql/format '{select (a._id,
                                 ((nest_many {select * from ((foo b))})))
                         from ((bar a))})))))

(deftest dotted-array-access-tests
  (is (= ["SELECT (a.b).c"]
         (sql/format '{select (((. (nest :a.b) :c)))}))))
