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
         :parse-fn #(Integer/parseInt %)
         :validate [integer? "not an integer"]
         :default 60  ]
      [  "-s"
         "--state PATH"
         "the directory in which to keep state"
         :default "log"  ]
      [  "-u"
         "--url URL"
         "the URL to fetch"
         :default "http://localhost:8080/"  ]
      [  "-h" "--help" "help"  ]  ]  )

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

(defn http-get ; footnote 1
   [url]
   (let
      [  before (System/nanoTime)
         after #(System/nanoTime)  ]
      (let
         [  {status :status body :body}
               (try
                  (clj-http.client/get url
                     {  :insecure? true
                        :socket-timeout 1000
                        :conn-timeout   1000
                        :throw-exceptions false  }  )
                  (catch java.net.ConnectException e
                     {:body "" :status "connection failed"}  )
                  (catch java.net.SocketTimeoutException e
                     {:body "" :status "socket timed-out"}  )
                  (catch org.apache.http.conn.ConnectTimeoutException e
                     {:body "" :status "connection timed-out"}  )  )  ]
         (let
            [  duration (/ (- (after) before) 1e6)
               message
                  (format "response returned by %s in %s ms" url duration)  ]
            [  status body message  ]  )  )  )  )

(defn load-index ; footnote 2
   [directory]
   (try
      (into
         {}
         (map
           #(vec (reverse (split % #"\s+" 2)))
            (split-lines (slurp (str directory "/index.txt")))  )  )
      (catch java.io.FileNotFoundException e {})
      (catch      IllegalArgumentException e {})  )  )

(defn save-index ; footnote 2
   [directory index]
   (spit
      (str directory "/index.txt")
      (join (sort (map #(str (join " " (reverse %)) "\n") index)))  )  )

(defn main-loop
   [  {  {  :keys [delta state help url]  } :options
         :keys [arguments errors summary]  }  ]
   (if help   (usage 0 summary errors))
   (if errors (usage 1 summary errors))
   (log/info
      (format
         "fetching %s every %s seconds, keeping state across runs in %s"
         url delta state  )  )
   (loop
      [  index (load-index state)  ]
      (Thread/sleep (* 1000 delta))
      (let
         [  [status body message] (http-get url)  ]
         (if
            (not (= status 200))
            (do
               (log/info
                  (format
                     "invalid (%s) %s - keeping last known good state"
                     status message  )  )
               (recur index)  )
            (let
               [  oldmd5 (get index url) newmd5 (digest/md5 body)  ]
               (if
                  (= newmd5 oldmd5)
                  (do
                     (log/info (format "unchanged %s" message))
                     (recur index)  )
                  (let
                     [  new-index (assoc index url newmd5)  ]
                     (spit (str state "/" newmd5 ".out") body)
                     (save-index state new-index)
                     (log/debug (format "MD5: %s -> %s" oldmd5 newmd5))
                     (log/info (format "different %s" message))
                     (recur new-index)  )  )  )  )  )  )  )

(defn -main
   [& args]
   (main-loop (parse-opts args cli-options))  )

; Footnote 1:
;
; I probably don't need to do this as a series of cascading lets, but
; I want to be sure the order of the bindings happens in the right
; order, and I don't know whether let preserves the order.
; Regardless, I should factor out the timing code and redo it as a
; higher-order function, which I use to wrap the http-get.  I'll put
; it on the todo list...
;
; Footnote 2:
;
; Instead of passing in the directory argument each time, I should
; pass it in to a partially evaluated function, which I create at the
; beginning of the flow of execution.  Then evoke the partially
; evaluated function each time, with the rest of the args.  Another
; todo item...
