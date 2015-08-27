goog.provide('js.tree');

goog.require('cljsjs.d3');

// http://bl.ocks.org/mbostock/1138500

js.tree.config = {w: 960, h: 600, r: 6, fill: d3.scale.category20()};

js.tree.force = d3.layout.force()
    .charge(-120)
    .linkDistance(30)
    .size([js.tree.config.w, js.tree.config.h]);

js.tree.svg = d3.select("body").append("svg:svg")
    .attr("width", js.tree.config.w)
    .attr("height", js.tree.config.h);

js.tree.drawJs.Tree = function(json) {
  var link = js.tree.svg.selectAll("line")
      .data(json.links)
      .enter().append("svg:line");

  var node = js.tree.svg.selectAll("circle")
      .data(json.nodes)
    .enter().append("svg:circle")
      .attr("r", js.tree.config.r - .75)
      .style("fill", function(d) { return js.tree.config.fill(d.group); })
      .style("stroke", function(d) {
          return d3.rgb(js.tree.config.fill(d.group)).darker();
      })
      .call(js.tree.force.drag);

  js.tree.force
      .nodes(json.nodes)
      .links(json.links)
      .on("tick", tick)
      .start();

  function tick(e) {

    // Push sources up and targets down to form a weak js.tree.
    var k = 6 * e.alpha;
    json.links.forEach(function(d, i) {
      d.source.y -= k;
      d.target.y += k;
    });

    node.attr("cx", function(d) { return d.x; })
        .attr("cy", function(d) { return d.y; });

    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });
  }
};
