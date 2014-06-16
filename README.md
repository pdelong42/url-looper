# url-looper

This is just a simple program to fetch the same URL over and over again.  If
the response changes, it logs that fact.  That's it.  It's meant to interface
with other tools, like Splunk or Nagios.

State is maintained by keeping an MD5 hash of the response in the memory of the
running instance.  Persistence across runs is achieved by writing the response
to a file on disk every any time the hash changes, and reading it in at the
start of each run.

This was written for two reasons: I wanted to scratch an itch - that is, I
wanted to be able to monitor a service hosted for us by an external vendor; and
I wanted an excuse to practice writing Clojure;

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
endpoint.

I stopped logging the MD5 hash and the filename, because those aren't really
useful pieces of information from the point-of-view of a log-processing tool
(e.g., Splunk).  I figure the URL is unique enough to disambiguate one run from
another, if you're running more than one instance of this on the same host [1].
The hash and filename may get logged again if I decide to add debug-level
logging.

The only thing left ambiguous is when one would have more than one instance of
this checking the same URL.  But I can't think of a good use case for that, so
don't do it.

[1] Idea for a future feature: since running more than one instance of this
program (that is, more than one JVM) can add to memory pressure pretty quickly
on a host, perhaps I should add the ability to check more than one URL
concurrently.  This would also be a good excuse to get practice writing
multithreaded code in Clojure.  If I decided to go in this direction, a config
file would probably be better - command-line args would get unwieldy fast.

## Usage

FIXME: explanation

    $ java -jar url-looper-0.3.0-standalone.jar [args]
