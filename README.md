# ragtime.elasticsearch

A [Ragtime](https://github.com/weavejester/ragtime) plugin for Elasticsearch migrations.

[![Build Status](https://travis-ci.org/sfx/ragtime.elasticsearch.svg)](https://travis-ci.org/sfx/ragtime.elasticsearch)

## Usage

### project.clj

```clojure
:profiles {:ragtime-elasticsearch
            {:dependencies [[ragtime.elasticsearch "0.1.0"]]
             :plugins [[ragtime/ragtime.lein "0.3.7"]]
             :ragtime {:migrations elasticsearch-migrations/migrations}
             :source-paths ["migrations"]}}
```

### Defining a migration

```clojure
(ns elasticsearch-migrations
  (:require [clj-http.client :as http]))

(def index "my_index")
(def index-url (memoize #(format "%s/%s" (:url %) index)))

(def create-index
  {:id "0001-create-index"
   :up (fn [es] (http/post (index-url es)))
   :down (fn [es] (http/delete (index-url es)))})

(defn migrations
  "Return a list of migrations to apply."
  []
  (list create-index))
```

### Running migrations

```
lein with-profile ragtime-elasticsearch ragtime migrate -d "http://localhost:9200" -r "ragtime.elasticsearch"
```

* `-d` or `--database` is your Elasticsearch URL.
* `-r` or `--require` should be the value `ragtime.elasticsearch` so Ragtime uses this library's namespace.

## Side effects

Usage of this plugin creates a `ragtime` index in your Elasticsearch cluster, with a `migrations` mapping. The `id` and `created_at` of applied migrations are stored in each document.

## License

Copyright © 2014 Brian Bowman, Zeeshan Lakhani, SFX Entertainment

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
