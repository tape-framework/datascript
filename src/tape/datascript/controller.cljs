(ns tape.datascript.controller
  (:refer-clojure :exclude [set])
  (:require [clojure.edn :as edn]
            [datascript.core :as d]
            [reagent.core :as r]
            [re-frame.cofx :as cofx]
            [tape.mvc.controller :as c :include-macros true]
            [integrant.core :as ig]
            [tape.module :as module]))

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

(defn ^{::c/cofx ::ds} add [m] (assoc m ::ds @ds))

(def inject (cofx/inject-cofx ::ds))

;;; Fx

(defn ^{::c/fx ::ds} set [v]
  (when-not (identical? @ds v) (reset! ds v)))

(defn ^::c/fx dump [] (dump-local @ds))

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
                                  ::dump    dump})))
