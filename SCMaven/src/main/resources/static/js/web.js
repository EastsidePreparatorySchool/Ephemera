/*
 SpaceCritters online - GM, QB, PM
 */


 let debug = false;

 let updateInterval = 50;
 let updateIntervalInactive = 1000;
 let updateIntervalActive = 50;



 // contants for update record types need to be in sync with SCGameLogEntry.java
 const ADDSPECIES = 1;
 const ADDSTAR = 2;
 const ADDPLANET = 3;
 const MOVEPLANET = 4;
 const TURN = 5;
 const ADD = 6;
 const MOVE = 7;
 const KILL = 8;
 const STATECHANGE = 9;
 const ORBIT = 10;
 const FIGHT = 11;
 const BURN = 12;





function makeClientID() {
   window.sessionStorage.setItem("clientID", "" + ((new Date()).getTime()) % 100000);
}

function getClientID() {
   return window.sessionStorage.getItem("clientID");
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
}


let comm = {
  sessionIdentifier: '',
  open: () => {
    let socket = new WebSocket('ws://' + location.hostname + ':' + location.port + '/socket');

    socket.onclose = () => {
      console.log('lost connection to server');
      setTimeout(comm.open, 2000);
    }
    socket.onmessage = (message) => {
      try {
        let data = JSON.parse(message.data);
        comm.process(data);
      } catch (err) {
        println(err);
        console.log(err);
        console.log("raw: " + message);
      }
    }

    this.socket = socket;
  },
  send: (s) => {
    socket.send(s);
  },
  attach: (engine) => {
    comm.send("ATTACH:" + engine);
  },
  process: (data) => {
    switch(data.type) {
      case undefined: //an array of records
        for(let i = 0; i<data.length; i++) comm.process(data[i]);
        break;
      case 'MessageRecord':
        comm.processMessage(data);
        break;
      case 'StarRecord':
        comm.processStar(data);
        break;
      case 'PlanetRecord':
        comm.processPlanet(data);
        break;
      case 'SpeciesRecord':
        comm.processSpecies(data);
        break;
      default:
        println('recieved unknown json');
        break;
    }
  },
  processSpecies: (data) => {
    println('SpeciesRecord recieved!');
  },
  processPlanet: (data) => {
    println('PlanetRecord recieved!');
  },
  processStar: (data) => {
    println('Star Record recieved!');
  },
  processMessage: (data) => {
    //console.log('Recieved a message!');
    let action = data.content.split(':')[0],
        parameter = data.content.substr(action.length+1);

    console.log(data.content.split(':')[0]);
    switch(action) {
      case 'HANDSHAKE':
        comm.sessionIdentifier = parameter;
        println('HANDSHAKE: connected to websocket');
        break;
      case 'ATTACHED':
        println('ATTACHED: connected to engine ' + parameter);
        comm.send('GETSTATE');
        break;
      case 'DEBUG': case 'DEBUGERR':
        println(data.content);
        break;
      default:
        println('recieved un-parsed message: ' + data.content);
    }
  }
};


 function attach(engine) {
     if (engine === undefined) engine = engines.value;
     comm.attach(engine);
 }

 function detach() {
     if (!attached) {
         return;
     }
     uiStateChange(false, undefined, null);
     request({url: "protected/detach?clientID=" + getClientID()}).then(data => {
     }).catch(error => {
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

 function start() {
     request({url: "protected/start?clientID=" + getClientID()})
             .then(data => {
                 if (data !== null) {
                     println("  Response: " + data);
                 }
                 //updates();
                 //println ("Requested updates in start");
             })
             .catch(error => {
                 if (error !== null && error.length > 0) {
                     println("  Error: '" + error + "'");
                 }
             });
 }

 function reattach() {
     listEngines();
     if (engineList !== null) {
         engineList.forEach((e) => {
             if (e.name === attachedName) {
                 println("reattaching to " + attachedName);
                 println(location);
                 location.reload();
             }
         });
     }
     println("waiting to reattach ...");
 }


 function listEngines() { //gets list of engines, puts in engineList, populates dropdown
   request({url: "listengines"})
   .then(data => {
     if (data !== null) {
       data = JSON.parse(data);

       if (!attached) engineList = data; //unsure of this line

       //clear dropdown
       engines.innerHTML = "";
       //repopulate dropdown
       for (let i = 0; i < data.length; i++) {
         println("Engine: '"+data[i]+"'");
         let option = document.createElement("option");
         option.value = data[i];
         option.innerHTML = data[i];

         //add thing to dropdown
         engines.appendChild(option);
       }
     }
   }).catch(error => {
       if (error !== null && error.length > 0) println("  Error: '" + error + "'");
   });
 }

 function listObservers() {
     if (!attached) {
         return;
     }

     request({url: "protected/listobservers?clientID=" + getClientID()})
             .then(data => {
                 if (data !== null) {
                     //println ("Raw: "+data);
                     data = JSON.parse(data);
                     let list = "Observers:<br>";
                     for (let i = 0; i < data.length; i++) {
                         list += data[i].name + ":" + data[i].maxRead + "<br>";
                     }
                     observerlistP.innerHTML = list;
                     observersSpan.innerText = data.length;
                     setTimeout(listObservers, 10000);
                 }
             })
             .catch(error => {
                 if (error !== null && error.length > 0) {
                     println("  Error: '" + error + "'");
                 }
             });
 }
 function getStatus() {
     if (!attached) {
         return;
     }

     request({url: "protected/status?clientID=" + getClientID()})
             .then(data => {
                 if (data !== null) {
                     data = JSON.parse(data);
                     logsizeSpan.innerText = data.logSize;
                     memstatsSpan.innerText = data.memStats;
                     livenessSpan.innerText = data.isAlive ? "alive" : "dead";
                     sleeptimeSpan.innerText = data.sleepTime;
                     setTimeout(getStatus, 1000);
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
                         observerlistP.style.display = "inline";
                         listObservers();
                     }
                 }
             })
             .catch(error => {
                 if (error !== null && error.length > 0) {
                     println("  Error: '" + error + "'");
                 }
             });
 }



 function pause() {
     request({url: "protected/pause?clientID=" + getClientID()})
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



function updates() {
  let start = (new Date()).getTime();
  request({url: "protected/updates?compact=yes&clientID=" + getClientID()})
    .then(data => {
       if (data !== null) {
           let end = (new Date()).getTime();
           reactToServiceTime(start, end);
           data = JSON.parse(data);
           try {
               processUpdates(data);
           } catch (err) {
               println("updates: error in processUpdates, " + err);
           }
       }
       updateCounts();

    }).catch(error => {
       if (error !== null && error.length > 0) {
           println("Error: '" + error + "'");
       }

       detach();
       println("Server not responding, console detached.");
    });
}

function slowmode() {
  request({url: "protected/slowmode?"
     + "clientID=" + getClientID()
     + "&state=" + (slowMode.checked ? "on" : "off")
  }).catch(error => {
  });
}



function processCheck(id) {
  let chk = document.getElementById("chk" + id);
  println("sending request to change state of species id " + id + " to " + (chk.checked ? "on" : "off"));
  request({url: "protected/check?id=" + id
      + "&selected=" + (chk.checked ? "on" : "off")
      + "&clientID=" + getClientID()
  }).then(data => {
    if (data !== null) {
        println("  Response: " + data);
    }
  }).catch(error => {
    if (error !== null && error.length > 0) {
        println("  Error: '" + error + "'");
    }
  });
}


function submitForm(form) {
    if (!attached) {
        println("upload: must be attached to upload alien jar");
        return false;
    }

    let body = new FormData(form);
    request({method: "POST", url: "protected/upload?clientID=" + getClientID(), body: body})
            .then(data => {
                println("file upload successful");
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("Error: '" + error + "'");
                }

                detach();
                println("Server not responding, console detached.");
            });
    return false;
}
