function store(name) {
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
    var s = JSON.stringify({
            points:a,
            drawings:JSON.parse(localStorage['plugin-draw-tools-layer']),
            name: name
    });
    console.info(s);
}


