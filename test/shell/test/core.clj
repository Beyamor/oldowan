(ns shell.test.core
  (:use [shell.core])
  (:use [clojure.test])
  (:import java.io.File))

(deftest can-set-working-directory
         (let [real-directory (get-working-directory)
               test-directory (File. "")]
           (set-working-directory test-directory)
           (is (= test-directory (get-working-directory)))
           (set-working-directory real-directory)))

(deftest can-use-a-directory-that-exists
         (let [dir (File. (System/getProperty "user.dir"))] ; obvs user.dir exists, c'mon
           (is (use-directory dir (fn [_] true)))))

(deftest cannot-use-a-directory-that-doesnt-exist
         (let [dir (File. "this-directory-should-not-exist")]
           (is (nil? (use-directory dir (fn [_] true))))))

(deftest can-parse-commands
         (let [[command arg1 arg2] (parse-command "command arg1  arg2")]
           (is (= "command" command))
           (is (= "arg1" arg1))
           (is (= "arg2" arg2))))
