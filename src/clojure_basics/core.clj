(ns clojure-basics.core
  (:require [clojure.string :as str])
  (:gen-class))

(def file-name "people.csv")

(count file-name)

(defn -main [& args]
  (let [people (str/split-lines(slurp file-name))
        people (map (fn [line]
                      (str/split line #","))
                 people)
        header (first people)
        people (rest people)
        people (map (fn [line]
                      (zipmap header line))
                 people)
        people (filter (fn [line]
                         (= (get line "country")
                            "Brazil"))
                 people)
        people (pr-str people)]
   (spit "people.edn" people)))
    

