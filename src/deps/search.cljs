(ns deps.search)

(def results
  {"TODO" #{"db.core",
            "server.core",
            "server.handler",
            "server.log",
            "util.db",
            "auth.db",
            "auth.api",
            "field.db",
            "field.db.compound",
            "field.db.multi",
            "components.maps",
            "components.general",
            "components.notification",
            "components.chart",
            "components.messages",
            "view.builder",
            "view.dashboard",
            "view.main",
            "util.sync",
            "util.reader",
            "util.error",
            "util.filetype-icons",
            "auth.model",
            "field.render.related",
            "model.nav",
            "model.state",
            "model.entry",
            "common.types",
            "util.util",
            "field.util"},
   ":field/type" #{"field.db",
                   "field.db.compound",
                   "field.db.multi",
                   "field.api",
                   "view.builder",
                   "view.dashboard",
                   "field.render",
                   "field.search.multi",
                   "field.render.multi",
                   "model.state",
                   "model.record",
                   "common.types",
                   "field.uti",
                   "field.util.mult"}
   "FIX" #{"server.handler",
           "server.log",
           "auth.db",
           "field.db",
           "field.db.compound",
           "field.db.multi",
           "components.maps",
           "components.chart",
           "view.builder",
           "view.list",
           "view.dashboard",
           "field.render.file",
           "model.state",
           "util.util"}})

(defn ^:export isSearched [phrases ns-str]
  {:pre [(string? ns-str)]}
  (true? (some #(contains? (get results %) ns-str) (into-array phrases))))
