(ns url-fetcher.core
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [clj-time [core :as t]]))



;; (defn url-chain-1 [url]
;;   (:trace-redirects (http/get url)))


;; (defn url-chain-3 [url]
;;   (let [r (http/head url {:follow-redirects false})]
;;     ((juxt :status
;;            #(json/generate-string (:headers %))
;;            #(if (= 200 (:status %)) (:body (http/get url)) nil)
;;            #(get-in % [:headers "date"])) r)))

(defn curate-response [response]
  (let [body (fn [r] (if (= (:status r) 200) (:body r) nil))]
    (conj ((juxt :url :status (comp json/generate-string :headers) body) response)
          (str (t/now)))))

(defn url-chain-5 [url]
  (let [response (conj (http/get url {:follow-redirects false}) [:url url])
        get-next (fn [r]
                   (if-let [loc (get-in r [:headers "location"])]
                     (conj (http/get loc {:follow-redirects false}) [:url loc])
                     nil))]
    (map curate-response (iterate get-next response))))

(defn url-chain-6 [url]
  (take-while #(not= (second %) nil) (url-chain-5 url)))



(defn pad-url-vector [v]
  (apply str (interpose " " v)))

(defn print-chain [url]
  (doseq [v (url-chain-6 url)]
    (println (pad-url-vector v))))

; "http://bit.ly/Ms09c7"

(def test-redirect-chain
  (url-chain-1 "http://bit.ly/MYHC7e"))



(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))
