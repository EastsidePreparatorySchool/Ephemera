/*
 SpaceCritters online - GM, QB, PM
 */


var updateInterval = 50;
var updateIntervalInactive = 500;
var updateIntervalActive = 50;

var textarea = document.getElementById("output");
var adminP = document.getElementById("admin");

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
    request({url: "protected/create?name=" + engineName.value})
            .then(data => {
                if (data !== null) {
                    println("  Response: " + data);
                    listEngines();
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}

function create() {
    request({url: "protected/create?name=" + engineName.value})
            .then(data => {
                if (data !== null) {
                    println("  Response: " + data);
                    listEngines();
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
                    println ("queryAdmin: "+data);
                    if (data === "yes") {
                        adminP.style.display = "inline";
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

function listEngines() {
    request({url: "protected/listengines"})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    data = JSON.parse(data);
                    for (var s in data) {
                        println("Engine: '"+data[s]+"'");
//
//                        var option = document.createElement("option");
//                        option.value = data[s];
//                        var optionText = document.createTextNode(data[s]);
//                        option.appendChild(optionText);
//                        engines.appendChild(option);
                    }
                }
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("  Error: '" + error + "'");
                }
            });
}



function init() {
    makeClientID();
    queryAdmin();
    listEngines();
    getStatus();
    println("initialized");
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

