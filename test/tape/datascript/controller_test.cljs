(ns tape.datascript.controller-test
  (:require [clojure.set :as set]
            [cljs.test :refer [deftest testing testing is]]
            [integrant.core :as ig]
            [datascript.core :as d]
            [re-frame.core :as rf]
            [tape.module :as module :include-macros true]
            [tape.mvc :as mvc :include-macros true]
            [tape.datascript.controller :as datascript.c]
            [tape.datascript.app.controller :as app.c]))

(module/load-hierarchy)

(def ^:private config
  {::mvc/module nil
   ::datascript.c/module nil
   ::app.c/module nil})

(deftest load-dump-test
  (let [ds (d/db-with (d/empty-db) [{::y 42}])]
    (datascript.c/dump-local "some-key" ds)
    (is (= ds (datascript.c/load-local "some-key")))))

(deftest controller-test
  (let [system (-> config module/prep-config ig/init)]
    (testing "module"
      (is (set/subset? #{::datascript.c/key
                         ::datascript.c/schema
                         ::datascript.c/ds
                         ::datascript.c/add
                         ::datascript.c/set
                         ::datascript.c/load-fx
                         ::datascript.c/dump-fx
                         ::datascript.c/load
                         ::datascript.c/dump}
                       (set (keys system)))))
    (testing "api"
      (rf/dispatch-sync [::datascript.c/load])
      (rf/dispatch-sync [::app.c/add-x])
      (rf/dispatch-sync [::datascript.c/dump])
      (let [{::datascript.c/keys [ds] ::app.c/keys [query]} system]
        (is (= 42 (query ::query nil)))
        (is (= @ds (datascript.c/load-local datascript.c/key)))))
    (ig/halt! system)))
