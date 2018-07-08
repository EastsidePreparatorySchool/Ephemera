/*
SpaceCritters online test
*/


var updateTimer = null;
var textarea = document.getElementById("output");


function init() {
    println ("initialized");
}



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
};



function updates () {
    request({url: "updates"})
        .then(data => {
            if (data !== null) {
                data = JSON.parse(data);
                if (data !== null && data.length > 0) {
                    println("Updates: " + data.length + " items");
                }
            }
        })
        .catch(error => {
            println("Error: " + error);
        });
}

function attach() {
    request({url: "attach"})   
        .then(data => {
            data = JSON.parse(data);
            println("Initial state: " + data);
            clearInterval(updateTimer);
            updateTimer = setInterval(getMoreUpdates, 1000);
        })
        .catch(error => {
            println("Error: " + error);
        });;
}


function detach() {
    clearInterval(updateTimer);
    updateTimer = null;
    request({url: "detach"});
}


function start() {
    request({url: "start"});
}


function pause() {
    request({url: "pause"});
}


function shutdown() {
    request({url: "shutdown"});
}




//
// timer refresh
//


function getMoreUpdates() {
    updates();
}




// util
//

function print (message) {
    textarea.value += message;
    textarea.scrollTop = textarea.scrollHeight;
}


function println (message) {
    textarea.value += message + "\n";  
    textarea.scrollTop = textarea.scrollHeight;
}


println ("parsed");