(ns deps.deps 
  "Generate a namespace dependency graph as an svg file"
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.file :as ns-file]
            [clojure.tools.namespace.track :as ns-track]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.tools.namespace.dependency :as ns-dep]
            [clojure.data.json :as json]))

(defn ns->group-name [nss] 
  (first (str/split (name nss) #"\.")))

(defn nodes->colors [nodes]
  (loop [colors {}
         nds nodes]
    (if-let [n (first nds)]
      (let [group (first (str/split (name n) #"\."))]
        (recur (if (contains? colors group)
                 colors
                 (assoc colors group group))
               (rest nds)))
      colors)))

;; Should replace with tools.namespace
(defn depgraph
  "Generate a namespace dependency graph as svg file"
  [project]
  (let [json-file (str (:name project) ".json")
        source-files (apply set/union
                       (map (comp ns-find/find-clojure-sources-in-dir io/file)
                         (project :source-paths)))
        tracker (ns-file/add-files {} source-files)
        dep-graph (tracker ::ns-track/deps)
        ns-names (set (map (comp second ns-file/read-file-ns-decl)
                        source-files))
        part-of-project? (partial contains? ns-names)
        nodes (filter part-of-project? (reverse (ns-dep/topo-sort dep-graph)))
        colors (nodes->colors nodes)
        edges (->> nodes
                (mapcat #(->> (filter part-of-project?
                                (ns-dep/immediate-dependencies dep-graph %))
                           (map (partial vector %)))))
        idx (into {} (map-indexed (fn [i n] [n i]) nodes))
        json-nodes (mapv (fn [[n i]] {:name (str n)}) (sort-by second idx))
        json-edges (->> edges 
                     (map (fn [[from to]]
                            {:source from :target to})))]
    (with-open [^java.io.Writer w (io/writer (io/file json-file))]
      (json/write {:edges json-edges :nodes json-nodes} w))))
