(ns shell.core
  (:import [java.io File]))

(def working-directory (atom nil))

(def prompt-terminator "೯ ")
(def shell-commands
  {"pwd" :print-working-directory})

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

(defn get-command
  "Reads a command from standard input."
  []
  (read-line))

(defn print-invalid-command
  "Prints the response to a bad command."
  [command]
  (println (str "invalid command: " command)))

(defmulti execute-shell-command keyword)
(defmethod execute-shell-command :print-working-directory
  [_]
  (println (get-working-directory-name)))

(defn process-command
  "Does *something* with a command."
  [command]
  (if (not (empty? command))
    (if-let [shell-command (shell-commands command)]
      (execute-shell-command shell-command)
      (print-invalid-command command))))

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
