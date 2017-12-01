// ==UserScript==
// @id             iitc-plugin-advancedLinks@elseh
// @name           IITC plugin: advanced links
// @version        0.1.1
// @namespace      https://github.com/jonatkins/ingress-intel-total-conversion
// @updateURL      http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/plugins/linksPlugin.js
// @downloadURL    http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/plugins/linksPlugin.js
// @description    [Fielding tools] Allow import portals and drawings
// @include        https://www.ingress.com/intel*
// @include        http://www.ingress.com/intel*
// @match          https://www.ingress.com/intel*
// @match          http://www.ingress.com/intel*
// @include        https://www.ingress.com/mission/*
// @include        http://www.ingress.com/mission/*
// @match          https://www.ingress.com/mission/*
// @match          http://www.ingress.com/mission/*
// @grant          none
// ==/UserScript==


function wrapper(plugin_info) {
// ensure plugin framework is there, even if iitc is not yet loaded
    if(typeof window.plugin !== 'function') window.plugin = function() {};
// PLUGIN START ////////////////////////////////////////////////////////

    window.plugin.showName = {
        name: "newField"

    };

    var setup =  function() {
        window.showPortalPosLinks = function(lat, lng, name) {
            var encoded_name = 'undefined';
            if(name !== undefined) {
                encoded_name = encodeURIComponent(name);
            }

            if (typeof android !== 'undefined' && android && android.intentPosLink) {
                android.intentPosLink(lat, lng, map.getZoom(), name, true);
            } else {
                var qrcode = '<div id="qrcode"></div>';
                var script = '<script>$(\'#qrcode\').qrcode({text:\'GEO:'+lat+','+lng+'\'});</script>';
                var gmaps = '<a href="https://maps.google.com/maps?ll='+lat+','+lng+'&q='+lat+','+lng+'%20('+encoded_name+')">Google Maps</a>';
                var bingmaps = '<a href="http://www.bing.com/maps/?v=2&cp='+lat+'~'+lng+'&lvl=16&sp=Point.'+lat+'_'+lng+'_'+encoded_name+'___">Bing Maps</a>';
                var osm = '<a href="http://www.openstreetmap.org/?mlat='+lat+'&mlon='+lng+'&zoom=16">OpenStreetMap</a>';
                var latLng = '<span>&lt;' + lat + ',' + lng +'&gt;</span>';
                var txt = "**" +name+ "**" + "\n" + "https://maps.google.com/maps?ll="+lat+','+lng+'&q='+lat+','+lng+'%20('+encoded_name+')' + "\n" + lat + ',' + lng;
                dialog({
                    html: '<div style="text-align: center;">' + qrcode + script + gmaps + '; ' + bingmaps + '; ' + osm + '<br />' + latLng + '</div><textarea style="width:100%;height:4em;">'+txt+'</textarea>',
                    title: name,
                    id: 'poslinks'
                });
            }
        }
    };


// PLUGIN END //////////////////////////////////////////////////////////


    setup.info = plugin_info; //add the script info data to the function as a property
    if(!window.bootPlugins) window.bootPlugins = [];
    window.bootPlugins.push(setup);
// if IITC has already booted, immediately run the 'setup' function
    if(window.iitcLoaded && typeof setup === 'function') setup();
} // wrapper end
// inject code into site context
var script = document.createElement('script');
var info = {};
if (typeof GM_info !== 'undefined' && GM_info && GM_info.script) info.script = { version: GM_info.script.version, name: GM_info.script.name, description: GM_info.script.description };
script.appendChild(document.createTextNode('('+ wrapper +')('+JSON.stringify(info)+');'));
(document.body || document.head || document.documentElement).appendChild(script);