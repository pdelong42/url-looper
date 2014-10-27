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
      [  "-l"
         "--logs PATH"
         "the directory in which to keep state and history"
         :default "log"  ]
      [  "-u"
         "--url URL"
         "the URL to fetch"  ]
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

(defn merge-from-file
   [filename index]
   (try
      (into index
         (map
           #(vec (reverse (split % #"\s+" 2)))
            (split-lines (slurp filename))  )  )
      (catch java.io.FileNotFoundException e index)
      (catch      IllegalArgumentException e index)  )  )

(defn load-index
   [filename & pairs]
   (let
      [  apply-default? #(into %2 (if (not (keys %2)) %))
         index
         (apply-default?
            {"http://localhost:8080" ""}
            (merge-from-file filename (into {} pairs))  )  ]
      {  :filename filename :index index  }  )  )

(defn save-index
   [state url md5]
   (let
      [  filename (:filename state)
         index    (into (:index state) {url md5})
         swap-n-cat #(str (join " " (reverse %)) "\n")  ]
      (spit filename (join (sort (map swap-n-cat index))))
      {  :filename filename :index index  }  )  )

(defn make-url-processor
   [delta logs state]
   (fn
      [url]
      (fn
         [milliseconds]
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
                  (recur remaining)  )
               (let
                  [  md5 (digest/md5 body)  ]
                  (if
                     (= md5 (get (:index @state) url))
                     (do
                        (log/info (format "unchanged %s" message))
                        (recur remaining)  )
                     (do
                        (log/info  (format "different %s" message))
                        (log/debug (format "new MD5 = %s" md5))
                        (spit (str logs "/" md5 ".out") body)
                        (send-off state save-index url md5)
                        (recur remaining)  )  )  )  )  )  )  )  )

(defn make-thread-launcher
   [url-processor delta]
   (fn
      [url]
      (let
         [process-url (url-processor url)]
         (log/info (format "fetching %s every %s seconds" url (/ delta 1000)))
         (process-url (int (rand delta)))  )  )  )

(defn main-loop
   [  {  {  :keys [delta logs help url]  } :options
         :keys [arguments errors summary]  }  ]
   (if help   (usage 0 summary errors))
   (if errors (usage 1 summary errors))
   (log/info (format "keeping state across runs and history in \"%s\"" logs))
   (let
      [  state (agent (load-index (str logs "/index.txt") {url ""}))
         url-processor (make-url-processor delta logs state)
         thread-launcher (make-thread-launcher url-processor delta)  ]
      (dorun (pmap thread-launcher (keys (:index @state))))  )  )

(defn -main
   [& args]
   (main-loop (parse-opts args cli-options))  )
