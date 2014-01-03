(ns bootup.task
  (:require [tailrecursion.boot.core :as boot]
            [tailrecursion.boot.task :as task]
            [ancient-clj.core        :as ancient]
            [rewrite-clj.zip         :as z]
            [clojure.java.io         :as io]))

(defn ->dep [artifact]
  (->> (ancient/latest-version-string! artifact)
       (vector artifact)))

(defn update-boot-deps []
  (let [core-artifact 'tailrecursion/boot.core
        task-artifact 'tailrecursion/boot.task
        latest-core (->dep core-artifact)
        latest-task (->dep task-artifact)]
    (->> (-> (z/of-file "boot.edn")
             (z/find-value z/next core-artifact) z/up (z/replace latest-core)
             (z/find-value z/next task-artifact) z/up (z/replace latest-task)
             (z/->root-string))
         (spit (io/file "boot.edn")))))

(boot/deftask upgrade-boot
  "Upgrades core boot dependencies to the latest version"
  [boot]
  (task/pass-thru-wrap #(update-boot-deps)))

(comment
  (let [boot (boot/init! (base/base-env))]
    ((boot/create-app! boot
                       {:require-tasks '#{[tailrecursion.boot.core.task :refer :all]}}
                       {}
                       {:main [:do [:help]]})
     (boot/make-event boot)))
)
