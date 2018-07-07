/*
SpaceCritters online test
*/


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
            println("Result: " + data);
        })
        .catch(error => {
            println("Error: " + error);
        });
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


function print (message) {
    document.getElementById("output").innerHTML += message;
}



function println (message) {
    document.getElementById("output").innerHTML += message + "<br>";
}


println ("parsed");