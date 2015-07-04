// ==UserScript==
// @id             iitc-plugin-fielding@elseh
// @name           IITC plugin: fielding tools
// @version        0.1.1
// @namespace      https://github.com/jonatkins/ingress-intel-total-conversion
// @updateURL      http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/fieldingPlugin.js
// @downloadURL    http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/fieldingPlugin.js
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
    var s = JSON.stringify([
            a,
            JSON.parse(localStorage['plugin-draw-tools-layer']),
            window.plugin.fieldUtils.name
        ]);
    console.info(s);
    window.plugin.fieldUtils.value = s;
}    

window.plugin.fieldUtils.showName = function() {
    html = '<input type="text" value="' + window.plugin.fieldUtils.name + '" name="name" onChange="window.plugin.fieldUtils.name=this.value"/>';
    html += '<button onclick="window.plugin.fieldUtils.showDrawings();">Copy drawings</button>'
    
    dialog({
    html: html,
    id: 'plugin-fieldUtils-name',
    dialogClass: 'ui-dialog-fieldUtilsSet',
    title: 'Input name'
  });
    
}

window.plugin.fieldUtils.showDrawings = function() {
    window.plugin.fieldUtils.store();
    html = '<p><a onclick="$(\'.ui-dialog-fieldUtils-copy textarea\').select();">Select all</a> and press CTRL+C to copy it.</p>'
                +'<textarea readonly onclick="$(\'.ui-dialog-fieldUtils-copy textarea\').select();">'+window.plugin.fieldUtils.value+'</textarea>'
    
    
    dialog({
    html: html,
    id: 'plugin-fieldUtils-copy',
    dialogClass: 'ui-dialog-fieldUtils-copy',
    title: 'Copy value'
  });
    
}
    


var setup =  function() {
    $('#toolbox').append(' <a onclick="window.plugin.fieldUtils.showName();" title="Input name">Enter name</a>');
}  


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