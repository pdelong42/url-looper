(ns url-looper.core
   (  :require
      [digest]
      [clj-http.client :as http]
      [clojure.string    :refer [join]]
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
      [  "-f"
         "--filename FILENAME"
         "the filename to write to"
         :default "output.txt"  ]
      [  "-h" "--help" "help"  ]
      [  "-u"
         "--url URL"
         "the URL to fetch"
         :default "http://localhost:8080/"  ]  ]  )

(defn usage
   [exit-code options-summary & [error-msg]]
   (if error-msg (println error-msg "\n"))
   (println
      (join \newline
         [  "usage: write me"
            ""
            "Options:"
            options-summary  ]  )  )
   (System/exit exit-code)  )

(defn http-get
   [url]
   (let
      [  before (System/nanoTime)
         after #(System/nanoTime)  ]
      (let
         [  {status :status body :body}
               (try
                  (http/get url {:insecure? true :throw-exceptions false})
                  (catch java.net.ConnectException foo "")  )  ]
         [  status body (/ (- (after) before) 1e6)  ]  )  )  )

; These two functions are just placeholders at the moment.  The plan
; is to have them convert between filename and associative array, but
; it's obviously not that far along yet.

(defn load-state
   [filename]
   (try
      (slurp filename)
      (catch java.io.FileNotFoundException foo "")  )  )

(defn save-state
   [filename string]
   (spit filename string)  )

(defn main-loop
   [  {  {:keys [delta filename help url]} :options
          :keys [arguments errors summary]  }  ]
   (if help   (usage 0 summary errors))
   (if errors (usage 1 summary errors))
   (log/info
      (format "fetching %s every %s seconds, keeping state across runs in %s" url delta filename)  )
   (loop
      [  oldmd5
            (digest/md5
               (try
                  (slurp filename)
                  (catch java.io.FileNotFoundException foo "")  )  )  ]
      (Thread/sleep (* 1000 delta))
      (let
         [  [status body duration]
               (http-get url)
            newmd5 (digest/md5 body)
            message (format "response returned by %s in %s ms" url duration)  ]
         (if
            (= status 200)
            (do
               (if
                  (= newmd5 oldmd5)
                  (log/info (format "unchanged %s" message))
                  (do
                     (spit filename body)
                     (log/debug (format "md5: %s -> %s" oldmd5 newmd5))
                     (log/info (format "different %s" message)  )  )  )
               (recur newmd5)  )
            (do
               (log/info
                  (format "invalid (%s) %s - keeping last known good state" status message)  )
               (recur oldmd5)  )  )  )  )  )

(defn -main
   [& args]
   (main-loop (parse-opts args cli-options))  )
