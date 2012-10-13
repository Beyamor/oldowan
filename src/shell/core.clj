(ns shell.core
  (:import [java.io File]))

(def working-directory (atom nil))

(def prompt-terminator "೯ ")
(def shell-commands
  {"pwd" :print-working-directory
   "c-and-a" :print-command-and-args})

(defn get-working-directory
  "Returns the working directory as a Java file."
  []
  @working-directory)

(defn get-working-directory-name
  "Returns the name of the working directory."
  []
  (.getAbsolutePath (get-working-directory)))

(defn set-working-directory
  "Sets the working directory."
  [new-working-directory]
  (reset! working-directory new-working-directory))

(defn print-prompt
  "Prints the command prompt string."
  []
  (let [path-name (get-working-directory-name)]
    (print (str path-name prompt-terminator))
    (flush)))

(defn read-command
  "Reads a command from standard input."
  []
  (read-line))

(defn parse-command
  "Parses a command and its args out of a command string."
  [command-string]
  (.split command-string " "))

(defn get-command
  "Gets a command from standard in and parses it."
  []
  (parse-command (read-command)))

(defn print-invalid-command
  "Prints the response to a bad command."
  [command]
  (println (str "invalid command: " command)))

(defmulti execute-shell-command (fn [command & args] (keyword command)))

(defmethod execute-shell-command :print-working-directory
  [command & args]
  (println (get-working-directory-name)))

(defmethod execute-shell-command :print-command-and-args
  [command & args]
  (println (str "Command: " command))
  (doall (map-indexed (fn [index arg] (println (str "Arg" index ": " arg))) args)))

(defn process-command
  "Does *something* with a command."
  [command & args]
  (if (not (empty? command))
    (if-let [shell-command (shell-commands command)]
      (apply execute-shell-command shell-command args)
      (print-invalid-command command))))

(defn start-read-loop
  "That thing that does the things."
  []
  (loop []
    (do
      (print-prompt)
      (let [command-and-args (get-command)]
        (apply process-command command-and-args)
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
