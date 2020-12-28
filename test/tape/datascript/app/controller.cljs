(ns tape.datascript.app.controller
  {:tape.mvc/interceptors [datascript.c/inject]}
  (:require [integrant.core :as ig]
            [datascript.core :as d]
            [tape.mvc :as mvc :include-macros true]
            [tape.datascript.controller :as datascript.c]))

(defn add-x
  {::mvc/reg ::mvc/event-fx}
  [{::datascript.c/keys [ds]} _]
  {::datascript.c/ds (d/db-with ds [{::x 42}])})

(derive ::query ::mvc/sub-raw)
(defmethod ig/init-key ::query
  [_ ds]
  (fn [_ _] (d/q '[:find ?x . :where [_ ::x ?x]] @ds)))

(mvc/defm ::module
          {::query (ig/ref ::datascript.c/ds)})
