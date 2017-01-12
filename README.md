# grafter-url

A standalone Clojure library to help generate URLs and access their
individual components.

grafter-url defines an `IURL` protocol which is extended to both
`java.net.URL`, `java.net.URI` and our own GrafterURL record type.

## Usage

````clojure
(-> (->url "http://www.example.org")
    (append-path-segments ["path" "segments"])
    (set-scheme "https")
    (set-query-params {"query" "parameter"})
    (set-url-fragment "fragment"))

;; => #<GrafterURL https://www.example.org/path/segments?query=parameter#fragment>
````

## java.net.URI Warning

Note
[there are bugs](https://blog.stackhunter.com/2014/03/31/encode-special-characters-java-net-uri/)
in java.net.URI related to double encoding, and also not encoding
certain query parameters.  Some of these bugs may be inherited by the
functions in this API.

## TODO

- Incremental Construction
- API Improvements
- Bug Fixing
- Validation

## License

Copyright © 2015 Swirrl IT Ltd.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
