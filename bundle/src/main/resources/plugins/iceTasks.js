// ==UserScript==
// @name         ICE tasks
// @namespace    http://tampermonkey.net/
// @version      0.1
// @description  try to take over the world!
// @author       You
// @match        https://enl.tschoertner.net/iclight/operation/*
// @grant        none
// @updateURL      http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/plugins/iceTasks.js
// @downloadURL    http://rawgit.com/elseh/iitc/master/bundle/src/main/resources/plugins/iceTasks.js

// ==/UserScript==

(function() {
    'use strict';
    const METHOD_API_ADD					= "api/v3/api.php?a=add";
    var boxElement;
    var inputField;
    var lblStatus;
    var menuShown;

    var tabType = document.location.pathname.split("operation")[1];

    var insertIntoTable = function(table, elem1, elem2) {
        var string = '<tr><td width="20%">' + elem1 + '</td><td>' + elem2 + '</td></tr>';
        table[0].insertAdjacentHTML("beforeend", string);
        return [
            table.find("tr:last td:first").children()[0],
            table.find("tr:last td:last").children()[0]
        ]
    };

    var replaceTasks = function() {
        var json = JSON.parse(inputField.value);

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
                lblStatus.innerHTML = tasks.length + " tasks has been added to operation.";
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

    var useKeys = function() {
        var requiredKeys = {};
        JSON.parse(localStorage.last_keys)
            .forEach(function(key) {
                requiredKeys[key.portalUrl] = Math.max(0, parseInt(key.amount) -
                    (key.items.length == 0 ? 0 : key.items.map(function (item) {
                        return parseInt(item.amount)
                    })
                        .reduce(function (a, b) {
                            return a + b
                        })))}
        );
        var tasks_to_update = tasks
            .filter(function(task) {return task.action==ACTION_KEYS})
            .map(function(task) {
                var required = requiredKeys[task.portalUrl];
                if (required > 0) {task.description = "collect " + required + " keys";}
                task.status = (required > 0) ? TASK_STATUS_ASSIGNED : TASK_STATUS_DONE;
                return task;
            });
        console.log(tasks_to_update);
        updateTasks_v4(
            tasks_to_update,
            function() {lblStatus.innerHTML = "keys success"},
            function() {lblStatus.innerHTML = "keys fail"});
    };

    var loadKeys = function() {
        var keyString = inputField.value;
        var itemsMap = {};
        items.forEach(function(item) {
            itemsMap[parseFloat(item.lat) + ":" + parseFloat(item.lon)] = item.id;
        });

        var itemsToUpdate =
            keyString.split("\n").map(function(itemString) {
                var params= itemString.split(", ");
                var key = itemsMap[parseFloat(params[0]) + ":" + parseFloat(params[1])];
                if (!key) {
                    console.log(key, itemString);
                    return null;
                }
                return {
                    requiredItemId: key,
                    amount: params[3]
                };
            }).filter(function(item) {return !!item;});

        setItems(operationId, itemsToUpdate, function() {
            console.log("success");
            openUrl("operation/items");

        }, function() {
            // on error
            console.log("fail");

        });
    };

    function key_amount(item) {
        return item.items.length == 0 ? 0 :
            item.items.map(
                function(item){
                    return parseInt(item.amount);
                }
            ).reduce(function(a,b) {return a+b});
    }

    var storeKeys = function() {
        localStorage.setItem("last_keys", JSON.stringify(items));
        localStorage.setItem("lastKeyUpdate", Date.now());
        localStorage.setItem("lastKeyOperation", operation.title);
        inputField.innerHTML = items.map(function(item) {return item.lat + ", " + item.lon + ", " + item.description + ", " + key_amount(item)}).join("\n");
    };

    var menuClick = function() {
        if (menuShown) return;
        menuShown = true;
        var tasks = $(".main_container .box:first");
        var newBox = '<div class="box"><div class="title">TRI' + tabType + '</div></div>';
        tasks[0].insertAdjacentHTML("beforebegin", newBox);
        boxElement = tasks[0].previousElementSibling;
        boxElement.insertAdjacentHTML("beforeend", '<table class="listview"></table>' );
        var table = $(boxElement).find("table");

        var r;
        r = insertIntoTable(table,
            '',
            '<textarea style="width:100%;height: 4em;"></textarea>');
        inputField = r[1];

        var lastKeyUpdate = localStorage.getItem("lastKeyUpdate") ?
            new Date(Number.parseInt(localStorage.getItem("lastKeyUpdate"))) : "never";
        var operationTitle = localStorage.getItem("lastKeyOperation");
        if (tabType == "/tasks") {
            r = insertIntoTable(table,
                '<input type="button" value="Add Tasks" />',
                'To create tasks insert json into the text area field');
            r[0].onclick = replaceTasks;

            r = insertIntoTable(table,
                '<input type="button" value="Apply Keys" />',
                'To update farm tasks you should have keys copy ' +
                'from the equipment tab. Your last copy was made: ' + lastKeyUpdate
                +". Operation name: " + operationTitle);
            r[0].onclick = useKeys;
        }

        if (tabType == "/items") {
            r = insertIntoTable(table,
                '<input type="button" value="Load" />',
                'To load YOUR keys copy keys.csv file into the text area. ' +
                'This operation does not affect others keys. ');
            r[0].onclick = loadKeys;

            r = insertIntoTable(table,
                '<input type="button" value="Store" />',
                'Stores keys amounts and required amount into the local storage. ' +
                'After storing you can use it into the tasks tab for tasks update. ' +
                'Also it writes total keys to the text area. ' +
                'It would be great to synchronize keys with linking plan before loading keys.');
            r[0].onclick = storeKeys;
        }

        boxElement.appendChild(lblStatus = document.createElement("div"));
    };

    createTabBar({
        buttons: [
            {
                cssClass: "tri-button",
                text: "TRI" + tabType,
                onClick: menuClick
            }
        ]
    });

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


})();