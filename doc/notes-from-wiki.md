url-looper general (README)
===========================

Why not use Nagios or other tools?

   A: Forked zombie processes...doesn't want to wait for all the forked processes. Wanted to check state, and something more thresh-holdy. This tool can output and be piped to Nagios.

MD5 collision possibilities?

   A: Did not consider likely enough to worry about. If it happens, will switch to SHA.

Why only check 200's?

   A: Only cares about changes of status from 200.

   203, 204? They are OK, but indicate changes in the server.

url-looper code
===============

Clojure usually use Java namespace conventions. A: Leiningen generated the url-looper.core namespace.

In a large organization like this you'd want to use Java conventions. Missing --version option

Consider a more descriptive name for timer-wrapper macro.

   timer-wrapper takes a block of code and wraps it with code that times its execution and returns what the code block returns.

Why not print the message from the Java exception.

   A: Doesn't care about that info. Not prepared to deal with it at the moment.

clj-http.client GET method, does it return execution time?

   A: Tried it, but noticed there was a significant difference in time between it and the time I measured using System.nanotime. So stuck with the latter until he knows what is causing the difference.

Maybe make the 1e6 less magical.

   Zack: it's a holdover from LISP. Standard.

defn http-get is using "magical un-boxing" (destructuring).

   Nice, concise.

defn http-get: the e exception names are right-justified, which makes it confusing to read.

   If you have a new exception, the commit affects several lines.

      A: LISP parentheses conventions already have this issue

   Have to do whitespace edits to maintain alignment

   The Java exception names are more important than the e's, so better to align those.

The elegant conciseness of the code makes it hard to understand what's going on. Add comments.

defn merge-from-file is not testing that file exists but is not readable.

   Wrap the code in with block to handle closing (use this instead of slurp). More explicit.

      Further research shows that slurp uses with internally anyway, so it's fine.

   Including error handling of bad lines (data) in index file.
