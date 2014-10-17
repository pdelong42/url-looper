(ns url-looper.core
   (:require
      [digest]
      [clj-http.client]
      [clojure.string    :refer [join split split-lines]]
      [clojure.tools.cli :refer [parse-opts]]
      [clojure.tools.logging :as log]  )
   (:gen-class)  )

(def cli-options
   [  [  "-d"
         "--delta INT"
         "delta - seconds to wait between attempts"
         :parse-fn #(* 1000 (Integer/parseInt %))
         :validate [integer? "not an integer"]
         :default 60000
         :default-desc "60"  ]
      [  "-s"
         "--state PATH"
         "the directory in which to keep state"
         :default "log"  ]
      [  "-u"
         "--url URL"
         "the URL to fetch"
         :default "http://localhost:8080/"  ]
      [  "-h" "--help" "help"  ]  ]  )

(defmacro timer-wrapper
   [block]
  `(let
      [  before# (System/nanoTime) ret# ~block  ]
      [  (/ (- (System/nanoTime) before#) 1e6) ret#  ]  )  )

(defn usage
   [exit-code options-summary & [error-msg]]
   (if error-msg (println error-msg "\n"))
   (println
      (join \newline
         [  "Usage:"
            ""
            "   java -jar url-looper-X.Y.Z-standalone.jar -options..."
            ""
            "Options: (with defaults indicated)"
            options-summary  ]  )  )
   (System/exit exit-code)  )

(defn http-get
   [url delta]
   (let
      [  [  duration {status :status body :body}  ]
         (timer-wrapper
            (try
               (clj-http.client/get url
                  {  :insecure?         true
                     :socket-timeout   10000
                     :conn-timeout      1000
                     :throw-exceptions false  }  )
               (catch                    java.net.ConnectException e
                  {:body "" :status "connection failed"}  )
               (catch              java.net.SocketTimeoutException e
                  {:body "" :status "socket timed-out"}  )
               (catch org.apache.http.conn.ConnectTimeoutException e
                  {:body "" :status "connection timed-out"}  )  )  )
         remaining (- delta duration)
         message (format "response returned by %s in %s ms" url duration)  ]
      [  status body (if (< remaining 0) 0 remaining) message  ]  )  )

(defn load-index
   [index-file]
   {  :filename index-file
      :index
      (try
         (into
            {}
            (map
              #(vec (reverse (split % #"\s+" 2)))
               (split-lines (slurp index-file))  )  )
         (catch java.io.FileNotFoundException e {})
         (catch      IllegalArgumentException e {})  )  }  )

(defn save-index
   [state pair]
   (let
      [  filename (:filename state)
         index    (:index    state)
         swap-n-cat #(str (join " " (reverse %)) "\n")  ]
      (spit filename (join (sort (map swap-n-cat (into index pair)))))  )  )

(defn main-loop
   [  {  {  :keys [delta state help url]  } :options
         :keys [arguments errors summary]  }  ]
   (if help   (usage 0 summary errors))
   (if errors (usage 1 summary errors))
   (log/info
      (format
         "fetching %s every %s seconds, keeping state across runs in %s"
         url delta state  )  )
   (letfn
      [  (fetch-and-compare ; footnote 1
            [index milliseconds]
            (Thread/sleep milliseconds)
            (let
               [  [status body remaining message] (http-get url delta)  ]
               (if
                  (not (= status 200))
                  (do
                     (log/info
                        (format
                           "invalid (%s) %s - keeping last known good state"
                           status message  )  )
                     (recur index remaining)  )
                  (let
                     [  oldmd5 (get index url)
                        newmd5 (digest/md5 body)  ]
                     (if
                        (= newmd5 oldmd5)
                        (do
                           (log/info (format "unchanged %s" message))
                           (recur index remaining)  )
                        (let
                           [new-index (send-off index save-index {url newmd5})]
                           (spit (str state "/" newmd5 ".out") body)
                           (log/debug (format "MD5: %s -> %s" oldmd5 newmd5))
                           (log/info  (format "different %s" message))
                           (recur new-index remaining)  )  )  )  )  )  )  ]
      (let
         [index (agent (load-index (str state "/index.txt")))]
         (fetch-and-compare index (int (rand delta)))  )  )  )

(defn -main
   [& args]
   (main-loop (parse-opts args cli-options))  )

; Footnote 1:
;
; Now I remember why I did this (why I created fetch-and-compare) - it
; was an attempt to prepare the code for adding the support of
; checking multiple URLs concurrently (which should be soonish).
