(ns tape.datascript.controller
  (:refer-clojure :exclude [set key])
  (:require [clojure.edn :as edn]
            [integrant.core :as ig]
            [datascript.core :as d]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [tape.mvc.controller :as c :include-macros true]))

;;; Helpers

(defn load-local
  [key]
  (when-let [s (js/localStorage.getItem key)]
    (edn/read-string s)))

(defn dump-local
  [key ds]
  (js/localStorage.setItem key (pr-str ds)))

;;; DB

;; db moniker is already established in re-frame, so we use `ds` to simplify
;; code on destructuring calls
(def key (-> ::ds symbol str))

(def ^{::c/reg :tape/const} schema {})

(def ^{::c/reg :tape/const} ds
  (r/atom (d/empty-db) :meta {:listeners (atom {})}))

;;; Handlers

(defmethod ig/init-key ::add [_ ds]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [add ^{::c/id ::ds}
        (fn [m] (assoc m ::ds @ds))]
    add))

(def inject (rf/inject-cofx ::ds))

(defmethod ig/init-key ::set [_ ds]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [set
        ^{::c/id ::ds}
        (fn [v]
          (when-not (identical? @ds v)
            (reset! ds v)))]
    set))

(defmethod ig/init-key ::load-fx [_ {:keys [key schema ds]}]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [load ^{::c/id ::load}
        (fn []
          (reset! ds (or (load-local key)
                         (d/empty-db schema))))]
    load))

(defmethod ig/init-key ::dump-fx [_ {:keys [key ds]}]
  ;; Workaround for: https://ask.clojure.org/index.php/8975
  (let [dump ^{::c/id ::dump}
        (fn [] (dump-local key @ds))]
    dump))

(defn load
  {::c/reg ::c/event-fx}
  [_ _] {::load true})

(defn dump
  {::c/reg ::c/event-fx}
  [_ _] {::dump true})

;;; Module

(c/defmodule {::key key
              ::add (ig/ref ::ds)
              ::set (ig/ref ::ds)
              ::load-fx {:key (ig/ref ::key)
                         :schema (ig/ref ::schema)
                         :ds (ig/ref ::ds)}
              ::dump-fx {:key (ig/ref ::key)
                         :ds (ig/ref ::ds)}})
