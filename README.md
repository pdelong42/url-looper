# url-looper

This is just a simple program to fetch the same URL over and over again.  If
the response changes, it logs that fact.  That's it.  It's meant to interface
with other tools, like Splunk or Nagios.

State is maintained by keeping an MD5 hash of the response in the memory of the
running instance.  Persistence across runs is achieved by writing the response
to a file on disk every any time the hash changes [1], and reading it in at the
start of each run.

[1] The caveat here is that it really should only do this when it gets a 200 OK
response.  But I haven't written this logic yet.

This was written for two reasons: I wanted to scratch an itch - that is, I
wanted to be able to monitor a service hosted for us by an external vendor; and
I wanted an excuse to practice writing Clojure;

I started this project over at bitbucket.org, and copied it over here when I
realized I wanted to do "releases".  Unfortunately, I was unable to preserve
history (not that there was much of it to speak of).  This is because it was a
subfolder in a larger "dumping-ground" project, where I put my doodles and my
practice Lein projects.  Extracting only its history would have been more work
than it's worth (believe me, I tried).

## Usage

FIXME: explanation

    $ java -jar url-looper-0.1.0-standalone.jar [args]
