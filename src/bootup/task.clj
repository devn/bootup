(ns bootup.task
  (:require [tailrecursion.boot.core     :as boot]
            [tailrecursion.boot.task     :as task]
            [cemerick.pomegranate.aether :as aether]
            [ancient-clj.core            :as ancient]
            [rewrite-clj.zip             :as z]
            [clojure.java.io             :as io]))

(defn ->latest [artifact]
  (->> (ancient/latest-version-string! artifact)
       (vector artifact)))

(def boot-edn "boot.edn")

(defn update-boot-deps! []
  (let [core-artifact 'tailrecursion/boot.core
        task-artifact 'tailrecursion/boot.task
        latest-core (->latest core-artifact)
        latest-task (->latest task-artifact)]
    (->> (-> (z/of-file boot-edn)
             (z/find-value z/next core-artifact) z/up (z/replace latest-core)
             (z/find-value z/next task-artifact) z/up (z/replace latest-task)
             (z/->root-string))
         (spit (io/file boot-edn)))))

(defn get-loader-dependencies [version]
  (let [coordinate ['tailrecursion/boot version]]
    (-> (aether/resolve-dependencies
         :coordinates [coordinate]
         :repositories (merge aether/maven-central {"clojars" "http://clojars.org/repo"}))
        (get coordinate))))

(boot/deftask upgrade-boot
  "Upgrades core boot dependencies to the latest version"
  [boot]
  (task/pass-thru-wrap
   (fn [] (do (println (-> (get-in @boot [:boot-version :vers])
                           get-loader-dependencies))
              (update-boot-deps!)))))

(comment
  (let [boot (boot/init! (base/base-env))]
    ((boot/create-app! boot
                       {:require-tasks '#{[tailrecursion.boot.core.task :refer :all]}}
                       {}
                       {:main [:do [:help]]})
     (boot/make-event boot)))
)
