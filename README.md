## README

STATUS: Pre-alpha, in design and prototyping phase.

#### About

`tape.datascript.controller`

Integrates the [datascript](https://github.com/tonsky/datascript) data 
structure via a `tape.mvc.controller` module, and provides co/effects to
interact with an instance.

You can use it as a replacement of app-db, or as an in-browser DB, save it to
LocalStorage etc.

#### Usage

You must be familiar with [datascript](https://github.com/tonsky/datascript),
`tape.module` and `tape.mvc` (particularly the controller part) before
proceeding.

##### Install

Add `tape.datascript` to your deps:

```clojure
tape/datascript {:local/root "../datascript"}
```

In `config.edn` add `:tape.datascript.controller/module` (and your schema, if
any):

```clojure
{:tape.profile/base {:tape.datascript.controller/schema <<schema-map>>}
 :tape.datascript.controller/module nil}
```

##### Interceptor & Co/Effect

For less verbose destructure calls, require in your ns:

```clojure
(ns sample.app.some.controller
  (:require [tape.datascript.controller :as datascript.c]))
```

In fx handlers that use it, add it to the interceptor chain via metadata and use
the `ds` coeffect and effect:

```clojure
(defn save
  {::c/reg ::c/event-fx
   ::c/interceptors [datascript.c/inject]}
  [{::datascript.c/keys [ds]} params]
  {::datascript.c/ds (d/db-with ds (some-model/make-tx-data params))})
```

If all the handlers in the namespace will require it, you can add the
interceptor as metadata at the namespace level:

```clojure
(ns sample.app.some.controller
  "Some docstring."
  {:tape.mvc.controller/interceptors [datascript.c/inject]}
  (:require [tape.datascript.controller :as datascript.c]))
```

If you used it akin to app-db, the equivalent of `re-frame.db/app-db` is
`:tape.datascript.controller/ds` entry in the system map and you can build a
signal graph on it via `re-frame.core/reg-sub-raw`.

##### LocalStorage

The `{::datascript.c/load true}` and `{::datascript.c/dump true}` effects or
`[::datascript.c/load]` and `[::datascript.c/dump]` events can be used to load
and dump the DataScript DB from and to LocalStorage.

#### License

Copyright © 2019 clyfe

Distributed under the MIT license.