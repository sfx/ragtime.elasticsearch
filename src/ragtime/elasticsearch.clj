(ns ragtime.elasticsearch
  (:require [clj-http.client :as http]
            [ragtime.core :refer [connection Migratable]]))

(def ^{:private true} index "ragtime")
(def ^{:private true} mapping "migrations")
(def ^{:private true} created-at "created_at")

(defn- ensure-migrations-index-exists
  [es]
  (try
    (let [index-url (format "%s/%s" (:url es) index)]
      (http/post index-url)
      ;; Create the mapping for migrations first or get-ids will fail.
      (http/put
        (format "%s/%s/_mapping" index-url mapping)
        {:content-type :json
         :form-params {:migrations
                        {:properties {:created_at {:type "long"}}}}}))
    (catch Exception _)))

(defn- id-url
  [es id]
  (format "%s/%s/%s/%s" (:url es) index mapping id))

(defn- add-migration-id
  [es id]
  (http/put
    (id-url es id)
    {:content-type :json
     :form-params {:_id id
                   (keyword created-at) (System/currentTimeMillis)}}))

(defn- delete-migration-id
  [es id]
  (http/delete (id-url es id)))

(defn- get-ids
  [es]
  (let [url (format "%s/%s/%s/_search?size=1000&sort=%s:asc"
                    (:url es) index mapping created-at)
        resp (:body (http/get url {:as :json}))
        hits (-> resp :hits :hits)
        ids (map #(:_id %) hits)]
    (vec ids)))

(defrecord Elasticsearch []
  Migratable
  (add-migration-id [es id]
    (ensure-migrations-index-exists es)
    (add-migration-id es id))

  (remove-migration-id [es id]
    (ensure-migrations-index-exists es)
    (delete-migration-id es id))

  (applied-migration-ids [es]
    (ensure-migrations-index-exists es)
    (get-ids es)))

(defmethod connection "http" [url]
  (map->Elasticsearch {:url url}))
