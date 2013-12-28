(ns bootup.task
  (:require [tailrecursion.boot.core :as boot]
            [tailrecursion.boot.task :as task]
            [ancient-clj.core        :as ancient]
            [ancient-clj.verbose     :as verbose]
            [rewrite-clj.parser      :as parser]
            [rewrite-clj.printer     :as printer]
            [rewrite-clj.zip         :as z]
            [clojure.java.io         :as io]))

(def boot-core-artifacts '[tailrecursion/boot.core tailrecursion/boot.task])

(def current-boot (z/of-file (io/file "boot.edn")))

(defn find-artifact-loc [artifact]
  (-> current-boot (z/find-value z/next artifact) z/up))

(defn get-latest-version-string [artifact]
  (ancient/latest-version-string! artifact))

(defn get-current-version-str [artifact]
  (-> (find-artifact-loc artifact) z/sexpr))

(defn get-latest-artifact-vectors []
  (mapv #(vector % (ancient/latest-version-string! %)) boot-core-artifacts))

(boot/deftask upgrade-boot
  "Upgrades core boot dependencies to the latest version"
  [boot]
  (task/pass-thru-wrap #(println get-latest-artifact-vectors)))
