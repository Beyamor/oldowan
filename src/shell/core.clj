(ns shell.core
  (:import [java.io File]))

(def working-directory (atom nil))

(def prompt-terminator "೯ ")

(defn get-working-directory
  "Returns the working directory as a Java file."
  []
  {:post [(instance? File %)]}
  @working-directory)

(defn get-working-directory-name
  "Returns the name of the working directory."
  []
  (.getAbsolutePath (get-working-directory)))

(defn set-working-directory
  "Sets the working directory."
  [new-working-directory]
  {:pre [(instance? File new-working-directory)]}
  (reset! working-directory new-working-directory))

(defn print-prompt
  "Prints the command prompt string."
  []
  (let [path-name (get-working-directory-name)]
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
  (set-working-directory (File. (System/getProperty "user.dir"))))

(defn -main
  "Our cute little main method."
  [& args]
  (setup)
  (start-read-loop))
