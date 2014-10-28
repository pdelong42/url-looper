 - [X] consider the merits of writing the entire file to the log when it changes;
   - considered - I can't think of a good reason to do this;
 - [X] adjust the log format so that it's more in-line with what gets thrown in syslog;
 - [X] ~~log to syslog instead of to a file;~~ postponed indefinitely
   - this can be done to a remote syslog server, but not to a local
     one that's only configured to listen on a Unix domain socket
     (instead of localhost), because Java;
 - [ ] handle the exceptions that can get thrown by "spit";
 - [ ] add an option (-N) for specifying a Nagios command pipe to write to...;
   - ...but only if we can establish a coherent use case for this;
   - describe how the use-case would work;
 - [ ] find-out how to get environment variable settings, for the purposes of
       getting CWD and printing absolute path to output file (which should be
       in "./tmp");
 - [ ] write an init script;
   - I cheated - I disabled exception throwing for clj-http/get;
 - [ ] document possible outputs for the usage of external tool (e.g., Splunk);
 - [ ] work-out what it takes to use the `index.txt` file as a persistent
       configuration state across runs, while still honoring the command-line
       flags and their defaults in an intelligible way;
 - [ ] revamp the logging framework to use something more flexible;
 - [ ] find-out why :request-time is so much smaller than the measured time (is
       the overhead really that high?);
 - [ ] parameterize the request timeout values, instead of having them set to
       the current hard-coded values;
 - [ ] find-out if log4j is already thread-safe (probably) or if I need to wrap
       it in agent abstractions as a precaution;
 - [ ] The arg processing and merging is really broken.  Need to fix.
       I didn't finally realize this until I started adding support
       for checking multiple URLs concurrently.
 - [ ] update the README to reflect the latest feature additions;
 - [ ] see if log4j will let us change the name logged to be something
       besides the thread-pool ID (say, using a prefix);
 - [ ] revert the -l option back to -s for compatibility, and to not break
       semver;
 - [ ] produce some ancillary documentation explaining the rationale behind the
       options merging;
 - [ ] produce some ancillary documentation explaining use-cases for Splunk and
       Nagios;
 - [ ] decide on a consistent convention for using "str" vs. "format";
