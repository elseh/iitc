// ==UserScript==
// @name         ICE import
// @namespace    http://tampermonkey.net/
// @version      0.1
// @description  try to take over the world!
// @author       You
// @match        https://enl.tschoertner.net/iclight/extensions/onion_dome.htm
// @grant        none
// ==/UserScript==

(function() {
    'use strict';

    var saveJson = function() {
        var json = JSON.parse(txtPortals.value);

        lblStatus.innerHTML = "saving...";

        var tasks = json.tasks;

        tasks.forEach(function (task) {task.title = transliterate(task.title); task.linkTitle = transliterate(task.linkTitle);});

        var data = {
            key:		getCookie("operationToken"),
            sync:		false,
            tasks:		tasks
        };

        apiCall(METHOD_API_ADD, JSON.stringify(data), function(response) {
            if (response !== null && response.success) {
                lblStatus.innerHTML = "saved!";
                console.log(tasks.length + " tasks has been added to operation.");
            } else {
                if (response.error === null) {
                    lblStatus.innerHTML = "ERROR!";
                } else if (response.error == ERR_API_INVALID_PARAMETERS) {
                    lblStatus.innerHTML = "ERROR: Invalid parameters.";
                } else if (response.error == ERR_API_INVALID_TOKEN) {
                    lblStatus.innerHTML = "ERROR: Invalid token. Log in and select an operation.";
                } else if (response.error == ERR_API_NO_PERMISSIONS) {
                    lblStatus.innerHTML = "ERROR: No permissions.";
                } else {
                    lblStatus.innerHTML = "ERROR!";
                }
            }
        }, function() {
            lblStatus.innerHTML = "ERROR!";
        });

    };


    function transliterate(word){
        var answer = "" , a = {};

        a["Ё"]="YO";a["Й"]="I";a["Ц"]="TS";a["У"]="U";a["К"]="K";a["Е"]="E";a["Н"]="N";a["Г"]="G";a["Ш"]="SH";a["Щ"]="SCH";a["З"]="Z";a["Х"]="H";a["Ъ"]="'";
        a["ё"]="yo";a["й"]="i";a["ц"]="ts";a["у"]="u";a["к"]="k";a["е"]="e";a["н"]="n";a["г"]="g";a["ш"]="sh";a["щ"]="sch";a["з"]="z";a["х"]="h";a["ъ"]="'";
        a["Ф"]="F";a["Ы"]="I";a["В"]="V";a["А"]="a";a["П"]="P";a["Р"]="R";a["О"]="O";a["Л"]="L";a["Д"]="D";a["Ж"]="ZH";a["Э"]="E";
        a["ф"]="f";a["ы"]="i";a["в"]="v";a["а"]="a";a["п"]="p";a["р"]="r";a["о"]="o";a["л"]="l";a["д"]="d";a["ж"]="zh";a["э"]="e";
        a["Я"]="Ya";a["Ч"]="CH";a["С"]="S";a["М"]="M";a["И"]="I";a["Т"]="T";a["Ь"]="'";a["Б"]="B";a["Ю"]="YU";
        a["я"]="ya";a["ч"]="ch";a["с"]="s";a["м"]="m";a["и"]="i";a["т"]="t";a["ь"]="'";a["б"]="b";a["ю"]="yu";

        for (var i in word){
            if (word.hasOwnProperty(i)) {
                if (a[word[i]] === undefined){
                    answer += word[i];
                } else {
                    answer += a[word[i]];
                }
            }
        }
        return answer;
    }
    console.log("setting up", document.location);
    var loadFunction = function () {
        console.log("in load");
        var btn = document.createElement("input");
        btn.onclick = saveJson;
        btn.type="button";
        btn.value="JSON";
        frmImport.appendChild(btn);
        frmImport.appendChild(lblStatus);
    };


    loadFunction();


})();