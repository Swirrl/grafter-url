(ns grafter.url-test
  (:require [clojure.test :refer :all]
            [grafter.url :refer :all])
  (:import [java.net URI URL URLEncoder]))

(defn build-url [base]
  (-> base
      (set-host "yak-hair.com")
      (set-port 90)
      (set-scheme "https")
      (set-url-fragment "11-things-to-do-with-yak-hair")
      (set-path-segments "articles" "yaks")
      (append-path-segments "shaving")
      (set-query-params {"article-id" 1})
      str))

(deftest set-query-params-test
  (is (= (URL. "http://foo.com/") (-> (URL. "http://foo.com/?foo=bar")
                                      (set-query-params {})))
      "Setting an empty hash of parameters removes query params completely"))

(deftest test-url-builders
  (let [in "http://foobar.com/blah/blah/blah"
        expected (URL. "https://yak-hair.com:90/articles/yaks/shaving?article-id=1#11-things-to-do-with-yak-hair")]

    (are [expected test] (is (= (str expected)
                                (str (build-url test))))
         expected (->url in)
         expected (URL. in)
         expected (URI. in))))

(deftest iurl-protocol-test
  (let [bar "http://bar.com/"]
    (are [value getter setter] (do
                                 (is (= value
                                        (-> (URI. bar)
                                            (setter value)
                                            getter)))

                                 (is (= value
                                        (-> (URL. bar)
                                            (setter value)
                                            getter)))

                                 (is (= value
                                        (-> (->url bar)
                                            (setter value)
                                            getter))))
         ;; test that what goes in comes out for all three
         ;; implementations.
         "foo.com" host set-host
         "https" scheme set-scheme
         nil port set-port
         9000 port set-port
         "foo" url-fragment set-url-fragment
         ["foo" "bar"] path-segments set-path-segments*
         {"foo" "bar" "baz" "fop"} query-params-map set-query-params)

    (is (= "http://foo.com/?foo=http%253A%252F%252Ffoobar.com%252F"
           (str (append-query-param (->url "http://foo.com/")
                                    :foo
                                    (->url "http://foobar.com/"))))
        "Serialises URL object parameters properly")))

(deftest append-query-params*-test
  (testing "URL encoding of query parameters"
    (is (= (URL. "http://foobar.com/?%26=ampersand&%3D=equals")
           (-> (URL. "http://foobar.com/")
               (append-query-params* "&" "ampersand" "=" "equals")))))

  (is (= (URL. "http://foo.com/?a=a&b=b&c=c")
         (-> (URL. "http://foo.com/")
             (append-query-params* "a" "a" "b" "b" "c" "c")))))

(deftest append-query-params-test
  (testing "URL encoding of query parameters"
    (is (= (URL. "http://foobar.com/?%26=ampersand&%3D=equals")
           (-> (URL. "http://foobar.com/")
               (append-query-params ["&" "ampersand"] ["=" "equals"])))))

  (is (= (URL. "http://foo.com/?a=a&b=b&c=c")
         (-> (URL. "http://foo.com/")
             (append-query-params ["a" "a"] ["b" "b"] ["c" "c"])))))

(deftest to-grafter-url-protocol-test
  (testing "extends String"
    (let [grafter-url (->grafter-url "http://www.tokyo-3.com:777/ayanami?geofront=retracted")]
      (are [expected actual] (= expected actual)
           777 (port grafter-url)
           "www.tokyo-3.com" (host grafter-url)
           "http" (scheme grafter-url)
           ["ayanami"] (path-segments grafter-url))

      (is (= grafter-url (->grafter-url grafter-url))))))

(deftest encoding-params
  (testing "Double encoding"
    (let [params [["http%3A%2F%2Ffoo" "http%3A%2F%2Fbar"]]]
      (is (= params
             (-> (URI. "http://x.com")
                 (set-query-params params)
                 query-params)))

      (is (= params
             (-> (URI. "http://x.com")
                 (set-path-segments "")
                 (set-path-segments "")
                 (set-query-params params)
                 query-params)))))

  (testing "Plus (+) decoding"
    (let [uri-with-plus (get (query-params-map (java.net.URI. "http://opendatacommunities.org/slice?plus=http%3A%2F%2Fopendatacommunities.org%2Fdef%2Fconcept%2Fgeneral-concepts%2Fbyage%2F16%2B"))
                             "plus")]
      (is (URI. uri-with-plus)
          "Is valid URI")

      (is (= "http://opendatacommunities.org/def/concept/general-concepts/byage/16+"
             uri-with-plus)
          "Is expected value"))))

(defn enc
  "URI encode string or URL/URI."
  [uri]
  (URLEncoder/encode (str uri) "UTF-8"))

(deftest canonicalise-test
  (testing "Sorts url query parameters alphabetically by their keys"
    (is (= (URI. "http://a.com/?a=a&b=b&c=c") (canonicalise "http://a.com/?c=c&a=a&b=b")))

    (testing "Encodings round trip properly"
      (let [uncanonical-url (URI. (str "http://mydomain.com/slice?dataset=" (enc "http://dataset/?foo=bar")
                                       "&" (enc "http://c") "=" (enc "http://c/val")
                                       "&" (enc "http://a") "=" (enc "http://a/val")
                                       "&" (enc "http://b") "=" (enc "http://b/val")))
            expected-uri (URI. (str "http://mydomain.com/slice?dataset=" (enc "http://dataset/?foo=bar")
                                    "&" (enc "http://a") "=" (enc "http://a/val")
                                    "&" (enc "http://b") "=" (enc "http://b/val")
                                    "&" (enc "http://c") "=" (enc "http://c/val")))]
        (is (= expected-uri (canonicalise uncanonical-url)))))))
