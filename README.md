# url-looper

This is just a simple program to fetch the same URL over and over again.  If
the response changes, it logs that fact.  That's it.  It's meant to interface
with other tools, like Splunk or Nagios.

State is maintained by keeping an MD5 hash of the response in the memory of the
running instance.  Persistence across runs is achieved by writing the response
to a file on disk any time the hash changes, and reading it in at the start of
each run.

This was written for two reasons: I wanted to scratch an itch - that is, I
wanted to be able to monitor a service hosted for us by an external vendor; and
I wanted an excuse to practice writing Clojure.

I started this project over at bitbucket.org, and copied it over here when I
realized I wanted to do "releases".  Unfortunately, I was unable to preserve
history (not that there was much of it to speak of).  This is because it was a
subfolder in a larger "dumping-ground" project, where I put my doodles and my
practice Lein projects.  Extracting only its history would have been more work
than it's worth (believe me, I tried).

Some caveats...

Any response other than a 200 OK is considered to be an error.  I didn't really
feel like writing the extra logic to handle redirects (though I suppose that I
should, in the interests of my secondary goal, of getting more practice writing
Clojure).  I figure that if you're checking a URL for changes, then you've
already traced through all the redirects, and only care about the final
endpoint.  I belive clj-http already follows redirects by default anyway.

I should write an init script for this - it's on the ToDo list.  However, I see
no point in writing a watchdog process.  Your monitoring system *is* your
watchdog process, and this code checks-in regularly even when everything is
okay.  If the heartbeat disappears, then your monitoring should notice, or it's
not doing its job.

## Usage

    $ java -jar url-looper-X.Y.Z-standalone.jar -h
