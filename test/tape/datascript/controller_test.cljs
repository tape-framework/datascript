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

(defn load
  {::c/reg ::c/event-fx}
  [_ _] {::datascript.c/load true})

(defn addx
  {::c/reg ::c/event-fx}
  [{::datascript.c/keys [ds]} _]
  {::datascript.c/ds (d/db-with ds [{::x 42}])})

(defn dump
  {::c/reg ::c/event-fx}
  [_ _] {::datascript.c/dump true})

(defn query
  {::c/reg ::c/sub-raw}
  [_ _] (d/q '[:find ?x . :where [_ ::x ?x]] @datascript.c/ds))

(c/defmodule)

(def ^:private config
  {:tape.profile/base {::datascript.c/schema {}}
   ::c/module         nil
   ::v/module         nil
   ::datascript.c/module nil
   ::module nil})

(def ^:private system nil)

(use-fixtures :once
  {:before (fn [] (set! system (-> config module/prep-config ig/init)))
   :after  (fn [] (ig/halt! system))})

(deftest load-dump-test
  (let [ds (d/empty-db)]
    (datascript.c/dump-local ds)
    (is (= ds (datascript.c/load-local)))))

(deftest module-test
  (is (set/subset? #{::datascript.c/schema
                     ::datascript.c/load-fn
                     ::datascript.c/add
                     ::datascript.c/set
                     ::datascript.c/load
                     ::datascript.c/dump}
                   (set (keys system)))))

(deftest api-test
  (rf/dispatch-sync [::load])
  (rf/dispatch-sync [::addx])
  (rf/dispatch-sync [::dump])
  (is (= 42 (query ::query nil)))
  (is (= @datascript.c/ds (datascript.c/load-local))))
