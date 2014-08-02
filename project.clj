(defproject url-looper "0.10.0-SNAPSHOT"
   :dependencies
   [  [clj-http                  "0.9.2"]
      [digest                    "1.4.4"]
      [log4j/log4j               "1.2.17"
         :exclusions
         [  javax.mail/mail
            javax.jms/jms
            com.sun.jmdk/jmxtools
            com.sun.jmx/jmxri  ]  ]
      [org.clojure/clojure       "1.5.1"]
      [org.clojure/tools.cli     "0.3.1"]
      [org.clojure/tools.logging "0.2.6"]
      [org.slf4j/slf4j-log4j12   "1.7.1"]  ] 
   :main url-looper.core  )
