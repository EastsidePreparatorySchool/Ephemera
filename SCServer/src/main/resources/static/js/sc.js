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




function attach() {
    request({url: "attach"})   
        .then(data => {
            data = JSON.parse(data);
            //println("Initial state: " + data);
            println("Observer id: " + data[0]);
            println("Total game turns: "+ data[1]);
            println("Number of aliens: "+data[2]);
            clearInterval(updateTimer);
            updateTimer = setInterval(getMoreUpdates, 1000);
        })
        .catch(error => {
            println("Error: " + error);
            clearInterval(updateTimer);
        });;
}


function updates () {
    request({url: "updates"})
        .then(data => {
            if (data !== null) {
                //println("Raw: "+data.substr(0,100));
                data = JSON.parse(data);
                processUpdates(data);
            }
        })
        .catch(error => {
            println("Error: " + error);
        });
}

function processUpdates(data){
    if (data !== null && data.length > 0) {
        for (var i = 0; i< data.length; i++) {
            var o = data[i];
            switch (o.type) {
                case "TURN":
                    println("Turn "+o.param1+" complete. #Aliens:"+o.param2);
                    break;
                case "MOVE":
                    println("Move: ("+o.param1 +","+o.param2+") -> ("+o.newX +","+o.newY+")");
                    break;
                default:
                    println ("uknown record");
                    break;
            }
        }
    }
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
    clearInterval(updateTimer);
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