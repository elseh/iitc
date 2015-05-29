var a = [];

for (var p in window.portals) {
    if (window.portals.hasOwnProperty(p)) {
        var port = window.portals[p];
        var ll = port._latlng;
        if (map.getBounds().contains(ll)) {
            a.push({id: p, lat: ll.lat, lng: ll.lng, title: port.options.data.title});

        }
    }
}

console.info(JSON.stringify(a));