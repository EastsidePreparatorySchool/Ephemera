/*
SpaceCritters online - GM, QB, PM
*/


var updateInterval = 200;
var updateTimer = null;

var textarea = document.getElementById("output");
var turnSpan = document.getElementById("turns");
var alienSpan = document.getElementById("numaliens");
var centerDiv = document.getElementById("center");

var aliens = {};
var speciesMap;


//initialize three.js and set all initial values

var scene;
var camera;
var renderer;
var cubeGeo;
var gridHelper;
var light;

var size = 501;
var divisions = 501;
var rotation = 0;




// key handlers

function keyUp(event) {
    key[event.which || event.keyCode] = false;
}

function keyDown(event) {
    key[event.which || event.keyCode] = true;
    //console.log(event.which);
}


//will, in the future handle keypresses

var key = {
    check: () => {
        //put things that happen while keys are pushed here
        }
}



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
};


// main functionality accessible from buttons

function attach() {
    request({url: "attach"})   
        .then(data => {
            data = JSON.parse(data);
            //println("Initial state: " + data);
            println("Observer id: " + data[0]);
            println("Total game turns: "+ data[1]);
            println("Number of aliens: "+data[2]);
            turnSpan.innerText = data[1];
            alienSpan.innerText = data[2];
            clearInterval(updateTimer);
            updateTimer = setInterval(getMoreUpdates, updateInterval);
        })
        .catch(error => {
            clearInterval(updateTimer);
            updateTimer = null;
            println("Error: " + error);
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
            clearInterval(updateTimer);
            updateTimer = null;
            println("Error: " + error);
        });
}

function processUpdates(data){
    if (data !== null && data.length > 0) {
        for (var i = 0; i< data.length; i++) {
            var o = data[i];
            switch (o.type) {
                case "TURN":
                    //println("Turn "+o.param1+" complete. #Aliens:"+o.param2);
                    turnSpan.innerText = o.param1;
                    alienSpan.innerText = o.param2;
                    break;
                case "ADD":
                    //println ("Adding alien id: "+o.id+", species: "+o.name);
                    addAlien(o);
                    break;
                case "MOVE":
                    //println("Move: id: "+o.id+" ("+o.param1 +","+o.param2+") -> ("+o.newX +","+o.newY+")");
                    moveAlien(o);
                    break;
                case "KILL":
                    //println("Alien id: "+o.id+" died.");
                    killAlien(o);
                    break;
                default:
                    println ("unknown record");
                    break;
            }
        }
        renderer.render(scene,camera);
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




// utilities
//

function print (message) {
    message = textarea.value+message;
    textarea.value = message.substr(-2048);
    textarea.scrollTop = textarea.scrollHeight;
}


function println (message) {
   print(message+"\n");
}

//
// PM alien code follows



// alien class, use this to make aliens
class Alien {
  constructor(c,x,z,id){
    this.mat = new THREE.MeshBasicMaterial({color:c, wireframe:false});
    this.mesh = new THREE.Mesh(cubeGeo,this.mat);
    scene.add(this.mesh);
    this.mesh.position.x = x;
    this.mesh.position.z = z;
    this.mesh.position.y = 0.5;
    this.id = id;
  }
  move(x,z){
    this.mesh.position.x = x;
    this.mesh.position.z = z;
  }
  kill(){
    scene.remove(this.mesh);
    delete aliens[this.id];
  }
};




function addAlien(content) {
    aliens[content.id] = new Alien(speciesMap.getColor(content.name),content.newX,content.newY);
}

function moveAlien(content) {
    var alien = aliens[content.id];
    if (alien !== undefined) {
        alien.move(content.newX,content.newY);
    }
}
        
function killAlien(content) {
    var alien = aliens[content.id];
    if (alien !== undefined) {
          alien.kill();
    }
}


class SpeciesMap {
    constructor() {
        this.map = {};
        this.count = 0;
        this.colors = ["red", "blue", "yellow", "green", "orange", "purple"];
    }

    getColor(name) {
        console.log (this);
        console.log(this.map);
        var color = this.map[name];
        if (color === undefined) {
            this.count++;
            color = this.colors[this.count%this.colors.length];
            this.map[name] = color;
        }
        return color;
    }
}



//
// PM:
// welcome to code purgatory, where all my code that i think might be useful in the future but does not work right now goes
// 

/*

// THE CAMERA CURRENTLY DOES NOT WORK
    camera.rotation.y = 0;
    camera.position.z = 5*Math.cos((rotation*Math.PI)/180);
    camera.position.x = 5*Math.sin((rotation*Math.PI)/180);
    camera.rotation.x = 0;
    camera.rotation.y = (rotation*Math.PI)/180;
    camera.rotation.x = -Math.PI/4;
    camera.rotation.x = (rotation*Math.PI)/180;
    camera.rotation.z = -(rotation*Math.PI)/180;

*/

//
// end purgatory
//



function init() {
    scene = new THREE.Scene();
    var width = $('#center').width();
    var height = $('#center').height();

    camera = new THREE.PerspectiveCamera(75, width/ height, 0.1, 1000 );
    renderer = new THREE.WebGLRenderer();
    renderer.setSize(width, height);
    centerDiv.appendChild(renderer.domElement);

    cubeGeo = new THREE.BoxGeometry(0.9, 0.9, 0.9);


    gridHelper = new THREE.GridHelper(size, divisions);
    scene.add(gridHelper);

    camera.position.z = 100;
    camera.position.y = 100;
    camera.rotation.x = -Math.PI/4;

    light = new THREE.AmbientLight(0x404040);
    scene.add(light);

    renderer.render(scene,camera);


    //listeners for keypresses
    document.addEventListener('keyup', keyUp, false);
    document.addEventListener('keydown', keyDown, false);

    // 
    speciesMap = new SpeciesMap();

    println ("initialized");
}

println ("parsed");

