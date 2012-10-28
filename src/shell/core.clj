(ns shell.core
  (:import [java.io File IOException]
           [java.lang ProcessBuilder]))

(def prompt-terminator "೯ ")

(def exit-message "Make a sharper rock.")

(def command-continuer "\\")

(defn print-error
  "Prints an error message."
  [s]
  (println (str "Error: " s)))

(defn valid-execution-environment?
  "Checks if a possible execution environment is actually valid."
  [env]
  (and env (map? env)))

(defn get-working-directory
  "Returns the working directory as a Java file."
  [execution-context]
  {:pre [(valid-execution-environment? execution-context)]
   :post [%]}
  (:working-directory execution-context))

(defn get-working-directory-name
  "Returns the name of the working directory."
  [execution-context]
  (.getCanonicalPath (get-working-directory execution-context)))

(defn set-working-directory
  "Sets the working directory."
  [execution-context new-working-directory]
  {:pre [(= java.io.File (class new-working-directory))]
   :post [(valid-execution-environment? %)]}
  (assoc execution-context :working-directory new-working-directory))

(defn get-file
  "Given a path, this finds a file (relative to the working directory)"
  [execution-environment path]
  (let [path (if (.startsWith path "~")
               (str (System/getProperty "user.home") (.substring path 1))
               path)
        path-file (File. path)]
    (if (.isAbsolute path-file)
      path-file
      (File. (get-working-directory execution-environment) path))))

(defn use-directory
  "Tries to perform an operation with a directory,
   failing out with print messages if the directory
   specified doesn't exist or is not actually a directory."
  [dir f]
  (if (.exists dir)
    (if (.isDirectory dir)
      (f dir)
      (print-error (str (.getCanonicalPath dir) " is not a directory")))
    (print-error (str (.getCanonicalPath dir) " does not exist"))))

(defn print-prompt
  "Prints the command prompt string."
  [execution-environment]
  (let [path-name (get-working-directory-name execution-environment)]
    (print (str path-name prompt-terminator))
    (flush)))

(defn read-command
  "Reads a command from standard input."
  []
  (read-line))

(defn terminated-command?
  "Checks if a command line indicates more are coming."
  [line]
  (not (.endsWith line command-continuer)))

(defn remove-command-continuer
  "Removes the command continuation indicator from a line."
  [line]
  (let [continuer-index (.lastIndexOf line command-continuer)]
    (if (not= continuer-index -1)
      (.substring line 0 continuer-index)
      line)))

(defn read-full-command
  "Reads a command that may span multiple lines."
  []
  (loop [full-command ""]
    (let [new-line (read-command)]
      (if (terminated-command? new-line)
        (str full-command new-line)
        (recur (str full-command (remove-command-continuer new-line)))))))

(defn parse-command
  "Parses a command and its args out of a command string."
  [command-string]
  (remove empty? (.split command-string " ")))

(defn get-command
  "Gets a command from standard in and parses it."
  []
  (parse-command (read-full-command)))

(defn print-invalid-command
  "Prints the response to a bad command."
  [command]
  (println (str "unexpected command: " command)))

(defmulti execute-command (fn [execution-environment command & args] command))

(defmethod execute-command "pwd"
  [execution-environment command & args]
  (println (get-working-directory-name execution-environment)))

(defmethod execute-command "command-and-args"
  [execution-environment command & args]
  (println (str "Command: " command))
  (doall (map-indexed (fn [index arg] (println (str "Arg" index ": " arg))) args)))

(defmethod execute-command "cd"
  [execution-environment command & args]
  (if-let [path (first args)]
    (use-directory
      (get-file execution-environment path)
      #(set-working-directory execution-environment %))
    (println "Where?")))

(defmethod execute-command "ls"
  [execution-environment command & args]
  (if-let [path (first args)]
    (use-directory
      (get-file execution-environment path)
      (fn [dir]
        (doall (map #(println (.getName %)) (.listFiles dir)))))
    (execute-command execution-environment "ls" ".")))

(defmethod execute-command "exit"
  [execution-environment command & args]
  (assoc execution-environment :exit true))

(defn execute-clojure
  "Tries to execute some Clojure."
  [execution-environment & tokens]
  (try
    (-> (apply str (interpose " " tokens)) read-string eval println)
    (catch Exception e
      (.printStackTrace e))))

(defn execute-process
  "Tries to execute an arbitrary process."
  [execution-environment & command-and-args]
  (try
    (let [processBuilder (doto (ProcessBuilder. command-and-args)
               (.directory (get-working-directory execution-environment)))
          process (.start processBuilder)
          stream (.getInputStream process)]
      (with-open [reader (clojure.java.io/reader stream)]
        (doall (map println (line-seq reader)))))
    (catch IOException e ; assume this comes from being unable to find prcess
      (print-invalid-command (first command-and-args)))))

(defmethod execute-command :default
  [execution-environment command & args]
  (if (.startsWith command "(")
    (apply execute-clojure execution-environment command args)
    (apply execute-process execution-environment command args)))

(defn process-command
  "Does *something* with a command."
  [execution-environment & command-and-args]
  {:pre [(valid-execution-environment? execution-environment)]
   :post [(valid-execution-environment? %)]}
  (if (not (empty? command-and-args))
    (let [command-result (apply execute-command execution-environment command-and-args)]
      (if (valid-execution-environment? command-result)
        command-result
        execution-environment))
    execution-environment))

(defn start-read-loop
  "That thing that does the things."
  [execution-environment]
  (loop [execution-environment execution-environment]
    (do
      (print-prompt execution-environment)
      (let [command-and-args (get-command)
            new-execution-environment (apply process-command execution-environment command-and-args)]
        (if-not (:exit new-execution-environment)
          (recur new-execution-environment)
          (println exit-message))))))

(defn create-execution-environment
  "Initializes whatevs out little shelly needs."
  []
  (-> {}
    (set-working-directory (File. (System/getProperty "user.dir")))))

(defn -main
  "Our cute little main method."
  [& args]
  (start-read-loop (create-execution-environment)))
