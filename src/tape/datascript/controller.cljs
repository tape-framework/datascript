(ns tape.datascript.controller
  (:refer-clojure :exclude [set])
  (:require [clojure.edn :as edn]
            [integrant.core :as ig]
            [datascript.core :as d]
            [reagent.core :as r]
            [re-frame.cofx :as cofx]
            [tape.module :as module]
            [tape.mvc.controller :as c :include-macros true]))

;;; Data

;; db moniker is already established in re-frame, so we use `ds` to simplify
;; code on destructuring calls
(def ^:private key-name (-> ::ds symbol str))

(defn load-local []
  (when-let [s (js/localStorage.getItem key-name)]
    (edn/read-string s)))

(defn dump-local [ds]
  (js/localStorage.setItem key-name (pr-str ds)))

;;; DB

(defonce ds (r/atom nil))

;;; Cofx

(defn add
  {::c/reg ::c/cofx
   ::c/id ::ds}
  [m] (assoc m ::ds @ds))

(def inject (cofx/inject-cofx ::ds))

;;; Fx

(defn set
  {::c/reg ::c/fx
   ::c/id ::ds}
  [v] (when-not (identical? @ds v) (reset! ds v)))

(defn dump
  {::c/reg ::c/fx}
  [] (dump-local @ds))

;;; Integrant

(defmethod ig/init-key ::load-fn [_ {:keys [schema]}]
  (fn [] (reset! ds (or (load-local) (d/empty-db schema)))))

;;; Module

(defmethod ig/init-key ::module [_ _]
  (fn [config]
    (module/merge-configs config {::schema  nil             ;; provided
                                  ::load-fn {:schema (ig/ref ::schema)}
                                  ::add     #'add
                                  ::set     #'set
                                  ::load    (ig/ref ::load-fn)
                                  ::dump    #'dump})))
