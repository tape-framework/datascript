(ns tape.datascript.controller-test
  {:tape.mvc.controller/interceptors [datascript.c/inject]}
  (:require [clojure.set :as set]
            [cljs.test :refer [deftest testing is use-fixtures]]
            [integrant.core :as ig]
            [datascript.core :as d]
            [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]
            [tape.mvc.view :as v]
            [tape.module :as module :include-macros true]
            [tape.datascript.controller :as datascript.c]))

(module/load-hierarchy)

(defn add-x
  {::c/reg ::c/event-fx}
  [{::datascript.c/keys [ds]} _]
  {::datascript.c/ds (d/db-with ds [{::x 42}])})

(derive ::query ::c/sub-raw)
(defmethod ig/init-key ::query
  [_ ds]
  (fn [_ _] (d/q '[:find ?x . :where [_ ::x ?x]] @ds)))

(c/defmodule {::query (ig/ref ::datascript.c/ds)})

(def ^:private config
  {::c/module nil
   ::v/module nil
   ::datascript.c/module nil
   ::module nil})

(def ^:private system nil)

(use-fixtures :once
  {:before (fn [] (set! system (-> config module/prep-config ig/init)))
   :after (fn [] (ig/halt! system))})

(deftest load-dump-test
  (let [ds (d/db-with (d/empty-db) [{::y 42}])]
    (datascript.c/dump-local "some-key" ds)
    (is (= ds (datascript.c/load-local "some-key")))))

(deftest module-test
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

(deftest api-test
  (rf/dispatch-sync [::datascript.c/load])
  (rf/dispatch-sync [::add-x])
  (rf/dispatch-sync [::datascript.c/dump])
  (let [{::datascript.c/keys [ds] ::keys [query]} system]
    (is (= 42 (query ::query nil)))
    (is (= @ds (datascript.c/load-local datascript.c/key)))))
