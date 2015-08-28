goog.provide('deps.tree');

goog.require('cljsjs.d3');

// http://bl.ocks.org/mbostock/1138500

deps.tree.config = {w: 1600, h: 1200, rx: 60, ry: 20, fill: d3.scale.category20()};

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

deps.tree.drawTree = function(nodeId, json) {
  var root = deps.tree.svg(nodeId);
  var link = root.selectAll("line")
      .data(json.edges)
      .enter().append("svg:line");

  var node = root.selectAll("g").data(json.nodes);

  var box = node.enter().append("g");

  var circle = box.append("svg:ellipse")
      .attr("rx", deps.tree.config.rx - .75)
      .attr("ry", deps.tree.config.ry)
      .style("fill", function(d) { return deps.tree.config.fill(d.group); })
      .style("stroke", function(d) {
          return d3.rgb(deps.tree.config.fill(d.group)).darker();
      })
      .call(deps.tree.force.drag);

  var label = box.append("text")
        .text(function(d){
  	    return d.name;
        })
    	.attr("text-anchor", "middle");
  deps.tree.force
      .nodes(json.nodes)
      .links(json.edges)
      .on("tick", tick)
      .start();

  function tick(e) {

    // Push sources up and targets down to form a weak deps.tree.
    var k = 6 * e.alpha;
    json.edges.forEach(function(d, i) {
      d.source.y -= k;
      d.target.y += k;
    });

    node.attr("transform", function(d) {
        return "translate(" + d.x + "," + d.y + ")";
    })
        .attr("y", function(d) { return d.y; });

    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });
  }
};
