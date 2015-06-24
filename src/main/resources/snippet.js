var a = [];

for (var p in window.portals) {
    if (window.portals.hasOwnProperty(p)) {
        var port = window.portals[p];
        var ll = port._latlng;
        if (map.getBounds().contains(ll)) {
            a.push({id: p, latlng: ll, title: port.options.data.title, maxLinks: 8});
        }
    }
}

var result = {};
result.points = a;
result.drawings = JSON.parse(localStorage['plugin-draw-tools-layer']);

console.info(JSON.stringify(result));


