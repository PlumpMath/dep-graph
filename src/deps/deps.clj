(ns deps.deps 
  "Generate a namespace dependency graph as an svg file"
  (:use [clojure.java.io :as io]
        [clojure.set :as set]
        [clojure.tools.namespace.file :as ns-file]
        [clojure.tools.namespace.track :as ns-track]
        [clojure.tools.namespace.find :as ns-find]
        [clojure.tools.namespace.dependency :as ns-dep]
        [clojure.data.json :as json]))

;; Should replace with tools.namespace
(defn depgraph
  "Generate a namespace dependency graph as svg file"
  [project]
  (let [json-file (str (:name project) ".json")
        source-files (apply set/union
                       (map (comp ns-find/find-clojure-sources-in-dir
                              io/file)
                         (project :source-paths)))
        tracker (ns-file/add-files {} source-files)
        dep-graph (tracker ::ns-track/deps)
        ns-names (set (map (comp second ns-file/read-file-ns-decl)
                        source-files))
        part-of-project? (partial contains? ns-names)
        nodes (filter part-of-project? (ns-dep/nodes dep-graph))
        edges (->> nodes
                (mapcat #(->> (filter part-of-project?
                                (ns-dep/immediate-dependencies dep-graph %))
                           (map (partial vector %)))))]
    {:edges edges :nodes nodes})
  #_(clojure.pprint/pprint {:edges edges :nodes nodes} )
  #_(with-open [^java.io.Writer w (io/writer  (io/file json-file))]
      (json/write {:edges edges :nodes nodes} w))
  #_(sh "dot" "-Tsvg" (str "-o" svgfile ) dotfile) )


