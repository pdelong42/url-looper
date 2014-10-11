 - [ ] handle the exceptions that can get thrown by "spit";
 - [X] consider the merits of writing the entire file to the log when it changes;
   - considered - I can't think of a good reason to do this;
 - [ ] add an option (-N) for specifying a Nagios command pipe to write to...;
   - ...but only if we can establish a coherent use case for this;
 - [X] put in a usage / help message;
   - [X] add a usage function, which auto-generates the options
   - [X] finish filling-in the usage message
 - [X] adjust the log format so that it's more in-line with what gets thrown in syslog;
 - [X] ~~log to syslog instead of to a file;~~ postponed indefinitely
   - this can be done to a remote syslog server, but not to a local
     one that's only configured to listen on a Unix domain socket
     (instead of localhost), because Java;
 - [ ] find-out how to get environment variable settings, for the purposes of
       getting CWD and printing absolute path to output file (which should be
       in "./tmp");
 - [ ] write an init script;
 - [X] get it to stop treating self-signed certs as an error;
 - [X] handle java.net.ConnectException (connection timed-out);
 - [X] handle 404 exceptions;
   - I cheated - I disabled exception throwing for clj-http/get;
 - [ ] document possible outputs for the usage of external tool (e.g., Splunk);
 - [X] change the output filename to be based on MD5 hash, and use an
       accompanying index file to map URLs to their latest MD5;
 - [ ] work-out what it takes to use the `index.txt` file as a persistent
       configuration state across runs, while still honoring the command-line
       flags and their defaults in an intelligible way;
 - [ ] revamp the logging framework to use something more flexible;
