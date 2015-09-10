goog.provide('deps.tree');

goog.require('cljsjs.d3');
goog.require('deps.search');

deps.tree.config = {w: 1600, h: 1200,
                    rx: 60, ry: 30,
                    fill: d3.scale.category20()};

deps.tree.force = d3.layout.force()
    .charge(-900)
    .linkDistance(150)
    .size([deps.tree.config.w, deps.tree.config.h]);

deps.tree.svg = function(nodeId) {
    return d3.select(nodeId)
                .attr("width", deps.tree.config.w)
        	.attr("height", deps.tree.config.h);
};

deps.tree.formatNs = function(s) {
    return s.split(".").join("\n");
};

deps.tree.colors = palette('tol',11); 

deps.tree.nodeToGroup = function(name) {
    return name.split("\.")[0];
};

deps.tree.Graph = function(nsOpts, json) {
   var removeNs = nsOpts.ns;
   var shouldKeep = function (name) {
       var keep = true;
       removeNs.forEach(function(ns) {
           if (name.startsWith(ns)) {
               keep = false;
           };
       });
       return keep;
   };
    
   var g = new dagreD3.graphlib.Graph().setGraph({}); 

   var highlightNs = nsOpts.highlight;

   var allColors = deps.tree.colors.slice(0);
   var nodeColors = {};
   json.nodes.forEach(function(node) {
      if (shouldKeep(node.name)) {
          var group = deps.tree.nodeToGroup(node.name);
          var color;
          if (typeof nodeColors[group] !== "undefined") {
              color = nodeColors[group];
          } else {
              color = allColors[allColors.length - 1];
              nodeColors[group] = color;
              allColors.pop();
          }

          var hgStyle = "";
          var labelStyle = "";
          if (deps.search.isSearched(highlightNs, node.name)) {
              labelStyle = "fill:#f0f1eb";
              hgStyle = "fill:black;";
          } 
          g.setNode(node.name, {label: node.name,
                                labelStyle: labelStyle, 
                                style: "fill:#" + color + ";stroke:black;" + hgStyle});

      };
   });
   json.edges.forEach(function(edge) {
      if (shouldKeep(edge.source) && shouldKeep(edge.target)) {
        g.setEdge(edge.source, edge.target,{});
      };
   });
   g.nodes().forEach(function(v) {
      var node = g.node(v);
      node.rx = deps.tree.config.rx;
      node.ry = deps.tree.config.ry;
   });
   return g;
};

deps.tree.drawTree = function(nodeId, nsOpts, json) {
  var g = deps.tree.Graph(nsOpts, json);
  var root = deps.tree.svg(nodeId);
  var inner = root.select("g");
  var zoom = d3.behavior.zoom().on("zoom", function() {
      inner.attr("transform", "translate(" + d3.event.translate + ")" +
                             "scale(" + d3.event.scale + ")");
  });
  root.call(zoom);
  var render = new dagreD3.render();
  render(inner, g);

  var initialScale = 0.75;
  zoom.translate([(root.attr("width") - g.graph().width * initialScale) / 2, 20])
      .scale(initialScale)
      .event(root);
  root.attr("height", g.graph().height * initialScale + 40);
};
