(ns clojure-basics.core
  (:require [clojure.string :as str]
            [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [hiccup.core :as h])
  (:gen-class))

(def file-name "people.csv")

(defn read-people []
  (let [people (str/split-lines(slurp file-name))
        people (map (fn [line]
                      (str/split line #","))
                 people)
        header (first people)
        people (rest people)
        people (map (fn [line]
                      (zipmap header line))
                 people)]
   people))

(defn people-html [country]
  (let [people (read-people)
        people (filter (fn [person]
                         (or (nil? country)
                             (= (get person "country") country)))
                 people)]
    [:ol
     (map (fn [person]
            [:li (str 
                   (get person "first_name")
                   " "
                   (get person "last_name"))])
       people)]))
                  

(c/defroutes app
  (c/GET "/" []
    (h/html [:html
             [:body
              (people-html nil)]]))
  (c/GET "/:country" [country]
    (h/html [:html
             [:body
              (people-html country)]])))
              

(def server (atom nil))

(defn dev []
  (let [s (deref server)]
   (if s (.stop s)))
  (reset! server (j/run-jetty app {:port 3000 :join false})))

(defn -main [& args]
  (j/run-jetty app {:port 3000}))

