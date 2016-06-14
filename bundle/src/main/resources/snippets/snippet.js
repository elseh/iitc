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


function storeKeys(name) {
    var a = [];

    for (var p in window.portals) {
        if (window.portals.hasOwnProperty(p)) {
            var port = window.portals[p];
            var ll = port._latlng;
            if (map.getBounds().contains(ll)) {
                a.push({
                    name:port.options.data.title,
                    record:ll.lat+", "+ll.lng+", "+port.options.data.title+", 0"
                });
            }
        }
    }
    a = a.sort(function(a1,a2) {return (a1.name == a2.name)? 0 : (a1.name < a2.name)? 1 : -1 });
    var text = a.map(function(r) {return r.record;}).join("\n");
    plugin.fieldUtils.downloadTempFile(name + ".csv", text, "plain/text");
    console.info(a);
}