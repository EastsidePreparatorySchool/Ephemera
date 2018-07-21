/*
 SpaceCritters online - GM, QB, PM
 */


var updateInterval = 50;
var updateIntervalInactive = 500;
var updateIntervalActive = 50;

var textarea = document.getElementById("output");
var adminP = document.getElementById("admin");
var adminButton = document.getElementById("adminbutton");
var center = document.getElementById("center");
var splash = document.getElementById("splash");
var contentP = document.getElementById("content");
var engineName = document.getElementById("enginename");

var updateTimer;


// basic server calls in the absence of jquery

function request(obj) {
    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
        xhr.open(obj.method || "GET", obj.url);
        xhr.onload = () => {
            if (xhr.status >= 200 && xhr.status < 300) {
                resolve(xhr.response);
            } else {
                reject(xhr.statusText);
            }
        };
        xhr.onerror = () => reject(xhr.statusText);
        xhr.send(obj.body);
    });
}


function create() {
    println("trying to create engine ...")
    request({url: "protected/create?name=" + engineName.value})
            .then(data => {
                if (data !== null) {
                    println("  Response: " + data);
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}



function queryAdmin() {
    request({url: "protected/queryadmin"})
            .then(data => {
                if (data !== null) {
                    println("queryAdmin: " + data);
                    if (data === "yes") {
                        adminP.style.display = "inline";
                        adminButton.innerText = "Hide admin tools";
                        center.style.width = "60%";
                    }
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}

function listAllObservers() {
    request({url: "protected/listobservers?clientID=" + getClientID()})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    data = JSON.parse(data);
                    var list = "Observers:\Fn";
                    for (var s in data) {
                        list += data[s] + "\n";
                    }
                    println(list);
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}

function getStatus() {
    request({url: "protected/allstatus"})
            .then(data => {
                if (data !== null) {
                    data = JSON.parse(data);
                    for (var i = 0; i < data.length; i++) {
                        println(data[i]);
                    }
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}

function enginesView() {
    clearInterval(updateTimer);
    updateTimer = setInterval(listEngines, 1000);
}
function observersView() {
    clearInterval(updateTimer);
    updateTimer = setInterval(listObservers, 1000);
}
function statsView() {
    clearInterval(updateTimer);
    updateTimer = setInterval(listStats, 1000);
}

function listEngines() {
    request({url: "protected/listengines"})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    data = JSON.parse(data);
                    contentP.innerHTML = "<br><br><br><br>Engines:<br><br>";
                    for (var i = 0; i < data.length; i++) {
//                        println("Engine: '" + data[i].name + "', "
//                                + "alive: " + data[i].isAlive + ", "
//                                + "observers: " + data[i].observers + ", "
//                                + "turns: " + data[i].turns);

                        var atag = document.createElement("A");
                        if (data[i].isAlive) {
                            atag.href = "game.html?attach=" + data[i].name;
                        } else {
                            atag.href = "logdownload?name=" + data[i].name;
                            atag.download = "log_" + name + "_" + (new Date()).getTime();
                        }
                        var engine = document.createTextNode(data[i].name);
                        var info = document.createTextNode(", "
                                + "status: " + (data[i].isAlive ? "alive" : "dead") + ", "
                                + "turns: " + data[i].turns + ", "
                                + "observers: " + data[i].observers
                                );
                        atag.appendChild(engine);
                        contentP.appendChild(atag);
                        contentP.appendChild(info);
                        contentP.appendChild(document.createElement("BR"));
                    }
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}

function listObservers() {
    request({url: "protected/listobservers"})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    contentP.innerHTML = "<br><br><br><br>Observers:<br><br>";
                    data = JSON.parse(data);
                    if (data.length > 0) {
                        for (var i = 0; i < data.length; i++) {
                            var engine = document.createTextNode(data[i]);
                            contentP.appendChild(engine);
                            contentP.appendChild(document.createElement("BR"));
                        }
                    }
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}


function listStats() {
    request({url: "protected/allstatus2"})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    data = JSON.parse(data);
                    contentP.innerHTML = "<br><br><br><br>Statistics:<br><br>";
                    for (var item in data) {
                        var engine = document.createTextNode(item+":"+data[item]);
                        contentP.appendChild(engine);
                        contentP.appendChild(document.createElement("BR"));
                    }
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}
function toggleAdmin() {
    if (adminP.style.display === "none") {
        queryAdmin();
    } else {
        adminP.style.display = "none";
        adminButton.innerText = "Show admin tools";
        center.style.width = "80%";
    }
}

function killSplash() {
    splash.style.display = "none";
}


function init() {
    if (getClientID() === null) {
        setTimeout(killSplash, 2000);
    } else {
        killSplash();
    }

    makeClientID();
//    queryAdmin();
    listEngines();
    getStatus();
    println("initialized");
    updateTimer = setInterval(listEngines, 1000);
}

// 
function makeClientID() {
    window.sessionStorage.setItem("clientID", "" + ((new Date()).getTime()) % 10000);
}

function getClientID() {
    return window.sessionStorage.getItem("clientID");
}




// utilities
//

function print(message) {
    message = textarea.value + message;
    textarea.value = message.substr(-20000);
    textarea.scrollTop = textarea.scrollHeight;
}


function println(message) {
    print(message + "\n");
}

function dprintln(message) {
    if (debug) {
        console.log(message);
    }
}



function assert(condition, action) {
    if (debug) {
        if (!condition()) {
            dprintln("assert failed, see console for details: " + condition);
            if (action !== undefined) {
                action();
            }
            throw new Error("assert failed");
        } else {
            //println("survived assert " + lambda);
        }
    }
}


println("parsed");

