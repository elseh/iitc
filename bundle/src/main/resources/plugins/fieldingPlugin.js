// ==UserScript==
// @id             iitc-plugin-fielding@elseh
// @name           IITC plugin: fielding tools
// @version        0.1.1
// @namespace      https://github.com/jonatkins/ingress-intel-total-conversion
// @updateURL      http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/plugins/fieldingPlugin.js
// @downloadURL    http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/plugins/fieldingPlugin.js
// @description    [Fielding tools] Allow import portals and drawings
// @include        https://*.ingress.com/intel*
// @include        http://*.ingress.com/intel*
// @match          https://*.ingress.com/intel*
// @match          http://*.ingress.com/intel*
// @include        https://*.ingress.com/mission/*
// @include        http://*.ingress.com/mission/*
// @match          https://*.ingress.com/mission/*
// @match          http://*.ingress.com/mission/*
// @grant          none
// ==/UserScript==


function wrapper(plugin_info) {
// ensure plugin framework is there, even if iitc is not yet loaded
    if(typeof window.plugin !== 'function') window.plugin = function() {};
// PLUGIN START ////////////////////////////////////////////////////////

    window.plugin.fieldUtils = {
        name: "newField"

    };

    window.plugin.fieldUtils.store = function() {
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
            name: window.plugin.fieldUtils.name
        });
        console.info(s);
        window.plugin.fieldUtils.value = s;
    };

    window.plugin.fieldUtils.showName = function() {
        html = '<input type="text" value="' + window.plugin.fieldUtils.name + '" name="name" onChange="window.plugin.fieldUtils.name=this.value"/>';
        html += '<button onclick="window.plugin.fieldUtils.downDrawings();">Download drawings</button>';

        dialog({
            html: html,
            id: 'plugin-fieldUtils-name',
            dialogClass: 'ui-dialog-fieldUtilsSet',
            title: 'Input name'
        });

    };

    window.plugin.fieldUtils.downDrawings = function() {
        window.plugin.fieldUtils.store();
        downloadTempFile(window.plugin.fieldUtils.name + ".json", window.plugin.fieldUtils.value, "plain/text");

    };

    window.plugin.fieldUtils.downloadTempFile = function(filename, data, type) {
        var link = document.createElement("a");
        link.setAttribute("href", "data:" + type + ";charset=UTF-8," + encodeURIComponent(data));
        link.setAttribute("download", filename);
        link.style.display = "none";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };


    var setup =  function() {
        $('#toolbox').append(' <a onclick="window.plugin.fieldUtils.showName();" title="Input name">Enter name</a>');
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