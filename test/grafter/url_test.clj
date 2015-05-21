(ns grafter.url-test
  (:require [clojure.test :refer :all]
            [grafter.url :refer :all])
  (:import [java.net URL URI]))

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

    (is (= "http://foo.com/?foo=http://foobar.com/"
           (str (append-query-param (->url "http://foo.com/")
                                    :foo
                                    (->url "http://foobar.com/"))))
        "Serialises URL object parameters properly")))

(deftest to-grafter-url-protocol-test
  (testing "extends String"
    (let [grafter-url (->grafter-url "http://www.tokyo-3.com:777/ayanami?geofront=retracted")]
      (are [expected actual] (= expected actual)
           777 (port grafter-url)
           "www.tokyo-3.com" (host grafter-url)
           "http" (scheme grafter-url)
           ["ayanami"] (path-segments grafter-url))

      (is (= grafter-url (->grafter-url grafter-url))))))
