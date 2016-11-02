(ns clojure-basics.core
  (:require [clojure.string :as str]
            [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [ring.middleware.params :as p]
            [ring.util.response :as r]
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
    [:table
     [:tr
      [:th "ID"]
      [:th "First Name"]
      [:th "Last Name"]
      [:th "Email"]
      [:th "Country"]
      [:th "IP Address"]]
     (map (fn [person]
            [:tr
             [:td  (get person "id")]
             [:td  (get person "first_name")]
             [:td  (get person "last_name")]
             [:td  (get person "email")]
             [:td  (get person "country")]
             [:td  (get person "ip_address")]])
       people)]))
     
(def messages (atom []))

(defn header []
  [:div
   [:a {:href "/Russia"} "Russia"]
   (repeat 5 "&nbsp")
   [:a {:href "/Brazil"} "Brazil"]
   (repeat 5 "&nbsp")
   [:a {:href "/Germany"} "Germany"]
   [:br]
   [:form {:action "/add-message" :method "post"}
    [:input {:type "text" :placeholder "Enter message" :name "text"}]
    [:button {:type "submit"} "Submit"]]
   [:ol
    (map (fn [message]
           [:li message])
      @messages)]])



(c/defroutes app
  (c/GET "/" []
    (h/html [:html
             [:body (header)
              (people-html nil)]]))
  (c/GET "/:country" [country]
    (h/html [:html
             [:body (header)
              (people-html country)]]))
  (c/POST "/add-message" request
    (let [params (get request :params)
          text (get params "text")]
      (swap! messages conj text)
      (spit "messages.edn" (pr-str @messages))
      (r/redirect "/"))))
              

(def server (atom nil))

(defn dev []
  (let [s (deref server)]
   (if s (.stop s)))
  (reset! server (j/run-jetty app {:port 3000 :join false})))

(defn -main [& args]
  (try
    (let [contents-str (slurp "messages.edn")
          contents-data (read-string contents-str)]
      (reset! messages contents-data))
    (catch Exception e))
  (j/run-jetty (p/wrap-params app) {:port 3000}))

