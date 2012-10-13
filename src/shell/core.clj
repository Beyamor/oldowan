(ns shell.core
  (:import [java.io File]))

(def current-dir (atom nil))

(def prompt-terminator "೯ ")

(defn print-prompt
  "Prints the command prompt string."
  []
  (let [path-name (.getAbsolutePath @current-dir)]
    (print (str path-name prompt-terminator))
    (flush)))

(defn get-command
  "Reads a command from standard input."
  []
  (read-line))

(defn process-command
  "Does *something* with a command."
  [command]
  (println (str " -> " command)))

(defn start-read-loop
  "That thing that does the things."
  []
  (loop []
    (do
      (print-prompt)
      (let [command (get-command)]
        (process-command command)
        (recur)))))

(defn setup
  "Initializes whatevs out little shelly needs."
  []
  (reset! current-dir (File. (System/getProperty "user.dir"))))

(defn -main
  "Our cute little main method."
  [& args]
  (setup)
  (start-read-loop))
