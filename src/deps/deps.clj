(ns deps.deps 
  "Generate a namespace dependency graph as an svg file"
  (:use [clojure.java.io :as io]
        [clojure.java.shell :only [sh]]
        [clojure.data.json :as json]))

(defn ffile [file]
  (if (string? file)
    (java.io.File. file)
    file))

;; Should replace with tools.namespace
(defn read-ns [file]
  (with-open [f (-> file ffile java.io.FileReader.
                java.io.PushbackReader.)]
    (binding [*in* f]
      (let [x (clojure.core/read)]
        (if (= (first x) 'ns)
          x
          nil)))))

(defn get-required [ns-form]
  (set
    (mapcat
      (fn [x]
        (if (list? x)
          (map #(symbol (str (name (first x))
                          "."
                          (name
                            (if (vector? %)
                              (first %)
                              %)))) (rest x))
          (list x)))
      (map #(if (vector? %) (first %) %) (mapcat rest (filter #(and (or (vector? %) (list? %)) (= :require (first %))) ns-form))))))


(defn get-used [ns-form]
  (-> ns-form
    ((partial filter #(and (coll? %) (= :use (first %)))))
    ((partial map rest))
    ((partial apply concat))
    ((partial map #(if (vector? %) (first %) %)))
    ((partial mapcat
              (fn [x]
                (if (coll? x)
                  (map #(symbol (str (name (first x))
                                     "."
                                     (name
                                       (if (vector? %)
                                         (first %)
                                         %)))) (rest x))
                  [x]))))
    ((partial remove keyword?))))

(defn get-import [ns-form]
  (-> ns-form
    ((partial filter #(and (coll? %) (= :import (first %)))))
    ((partial map rest))
    ((partial apply concat))
    ((partial mapcat
              (fn [x]
                (if (coll? x)
                  (map #(symbol (str (name (first x)) "." (name %))) (rest x))
                  [x]))))))

(defn parse [ns-form]
  {:name (second ns-form)
   :clojure-depends (concat (get-required ns-form)
                            (get-used ns-form))
   :java-depends (get-import ns-form)})

(defn all-clojure-files [root]
  (let [root (ffile root)]
    (if (.isDirectory root)
      (mapcat all-clojure-files (.listFiles root
                                            (proxy [java.io.FilenameFilter] []
                                              (accept [dir name]
                                                      (or (.endsWith name ".clj")
                                                          (.isDirectory (java.io.File. dir name)))))))
      [root])))


(defn parse-directory [dir]
  (reduce #(assoc % (:name %2) (dissoc %2 :name)) {}
          (map parse (remove nil? (map read-ns (all-clojure-files dir))))))

(defn restructure [files]
  (reduce
    (fn [m dep]
      (-> m (update :java #(into % (:java-depends (second dep))))
        (update :clojure #(conj (into % (:clojure-depends (second dep)))
                            (first dep)))
        (update :edges
          #(into %
             (map (partial vector (first dep))
               (concat (:java-depends (second dep))
                 (:clojure-depends (second dep))))))))
    {:java #{} :clojure #{} :edges #{}}
    files))

(defn dot [x]
  (format "digraph simple_hierarchy {\n graph [rankdir = \"LR\"];\n %s subgraph cluster_clojure {\nlabel=\"clojure\";\ncolor=blue;\n%s} subgraph cluster_java {\nlabel=\"java\";\ncolor=red;\n%s}}" (:edges x) (:clojure x) (:java x)))
  
(defn safe-name [string]
  (str (.replaceAll (str string) "(\\.|-|\\$)" "_")))

(defn safe-name-and-label [structure tag]
  (update structure tag
    #(reduce str (map
                   (fn [x]
                     (str (safe-name x) "[label=\"" x "\"];\n")) %))))

(defn edges [structure]
  (update structure :edges
    (fn [x]
      (reduce str (map #(format "%s->%s;\n" (safe-name (first %)) (safe-name (second %))) x)))))

(defn depgraph
  "Generate a namespace dependency graph as svg file"
  [project]
  (let [source-path (:source-path project "src")
        json-file (str (:name project) ".json")
        g (-> source-path
            parse-directory
            ((partial remove (comp nil? first)))
            restructure
            ;; ((partial spit dotfile))
            )
        idx (into {} (map-indexed (fn [i n] [n i]) (:clojure g)))
        edges (->> (:edges g)
                (map (fn [[from to]]
                       {:source (get idx from) :target (get idx to)})))
        nodes (mapv (fn [[n i]] {:name (str n)}) (sort-by second idx))]
    (clojure.pprint/pprint {:edges edges :nodes nodes} )
    (with-open [^java.io.Writer w (io/writer  (io/file json-file))]
      (json/write {:edges edges :nodes nodes} w))
    #_(sh "dot" "-Tsvg" (str "-o" svgfile ) dotfile) ))


