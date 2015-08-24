goog.provide('tree');

goog.require('cljsjs.d3');

var w = 960,
    h = 500,
    r = 6,
    fill = d3.scale.category20();

tree.config = {w: 960, h: 500, r: 6, fill: d3.scale.category20()};

tree.force = d3.layout.force()
    .charge(-120)
    .linkDistance(30)
    .size([tree.config.w, tree.config.h]);

tree.svg = d3.select("body").append("svg:svg")
    .attr("width", tree.config.w)
    .attr("height", tree.config.h);

tree.drawTree = function(json) {
  var link = tree.svg.selectAll("line")
      .data(json.links)
      .enter().append("svg:line");

  var node = tree.svg.selectAll("circle")
      .data(json.nodes)
    .enter().append("svg:circle")
      .attr("r", tree.config.r - .75)
      .style("fill", function(d) { return tree.config.fill(d.group); })
      .style("stroke", function(d) {
          return d3.rgb(tree.config.fill(d.group)).darker();
      })
      .call(force.drag);

  force
      .nodes(json.nodes)
      .links(json.links)
      .on("tick", tick)
      .start();

  function tick(e) {

    // Push sources up and targets down to form a weak tree.
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
