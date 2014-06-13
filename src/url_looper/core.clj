(ns url-looper.core
   (  :require
      [digest]
      [clj-http.client :as http]
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

(defn main-loop
   [  {  {:keys [delta filename help url]} :options
          :keys [arguments errors summary]  }  ]
   ;(if help   (usage 0 summary errors))
   ;(if errors (usage 1 summary errors))
   (log/info (format "fetching %s every %s seconds and writing to %s" url delta filename))
   (letfn
      [  (inner-loop
            [oldmd5]
            (log/info (format "md5 = %s" oldmd5))
            (Thread/sleep (* 1000 delta))
            (let
               [  {status :status body :body}
                  (http/get url)
                  newmd5 (digest/md5 body)  ]
               (log/info (format "status = %s" status))
               (or
                  (= newmd5 oldmd5)
                  (spit filename body) ; footnote 1
                  (log/info "new content written to" filename "with an MD5 hash of" newmd5)  )
               (recur newmd5)  )  )  ]
      (inner-loop
         (digest/md5
            (try
               (slurp filename)
               (catch java.io.FileNotFoundException foo "")  )  )  )  )  )

(defn -main
   [& args]
   (main-loop (parse-opts args cli-options))  )

; Footnote 1:
;
; Since this always returns nil, the logging line following it will
; always get run with it.  Yes, this is incredibly kludgey - I should
; probably fix that.  I should also probably handle the exceptions
; that can be spat out by spit.
