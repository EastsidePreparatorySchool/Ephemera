/*
SpaceCritters online - GM, QB, PM
*/


var updateInterval = 200;
var updateTimer = null;

var textarea = document.getElementById("output");
var turnSpan = document.getElementById("turns");
var alienSpan = document.getElementById("numaliens");
var centerDiv = document.getElementById("center");
var species = document.getElementById("species");
var statusP = document.getElementById("status");
var countsP = document.getElementById("counts");

var aliens = {};
var planets = {};
var stars = [];
var speciesMap = null; // see init()


//initialize three.js and set all initial values

var scene;
var camera;
var renderer;
var cubeGeo;
var startGeo;
var planetGeo;
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
            println("Engine id: " + data.engine);
            println("Observer id: " + data.observer);
            println("Total game turns: "+ data.turns);
            turnSpan.innerText = data.turns;
            alienSpan.innerText = 0;
            countsP.style.color = "gold";
            statusP.innerHTML = "Attached to<br>&nbsp;Engine: "+data.engine+"<br>&nbsp;Observer: "+data.observer;
            clearInterval(updateTimer);
            updateTimer = setInterval(getMoreUpdates, updateInterval);
            getMoreUpdates();
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
            //println("Error: " + error);
            if (updateTimer !== null) {
                detach();
                println("Server not responding, console detached.");
            }
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

                case "ADDSTAR":
                    addStar(o);
                    break;

                case "ADDPLANET":
                    addPlanet(o);
                    break;
                case "MOVEPLANET":
                    movePlanet(o);
                    break;

                case "ADDSPECIES":
                    addSpecies(o);
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
    countsP.style.color = "black";
    statusP.innerHTML = "";
    species.innerHTML = "";

    speciesMap = new SpeciesMap();

    //println("starting purge ...");

    var a;
    var count = 0;
    for (a in aliens) {
        scene.remove(aliens[a].mesh);
        count++;
    }
    aliens = {};
    //println(" ... killed "+count+" aliens");

    var p;
    count = 0;
    for (p in planets) {
        scene.remove(planets[p].mesh);
        count++;
    }
    planets = {};
    //println(" ... destroyed "+count+" planets");

    var s;
    for (count = 0;count < stars.length; count++) {
        scene.remove(stars[count].mesh);
    }
    stars = [];
    //println(" ... extinguished "+count+" stars");

    renderer.render(scene,camera);
    //println("purge complete.");


    request({url: "detach"});
}


function start() {
    request({url: "start"});
}


function pause() {
    request({url: "pause"});
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
    this.mesh.position.z = -z;
    this.mesh.position.y = 1;
    this.id = id;
  }
  move(x,z){
    this.mesh.position.x = x;
    this.mesh.position.z = -z;
  }
  kill(){
    scene.remove(this.mesh);
    delete aliens[this.id];
  }
};


// star class, use this to make aliens
class Star {
  constructor(x,z){
    this.mat = new THREE.MeshBasicMaterial({color:"white", wireframe:false});
    this.mesh = new THREE.Mesh(starGeo,this.mat);
    scene.add(this.mesh);
    this.mesh.position.x = x;
    this.mesh.position.z = -z;
    this.mesh.position.y = 1;
  }
 
};

// planet class, use this to make aliens
class Planet {
    constructor(x,z, id){
        this.mat = new THREE.MeshBasicMaterial({color:"green", wireframe:false});
        this.mesh = new THREE.Mesh(planetGeo,this.mat);
        scene.add(this.mesh);
        this.mesh.position.x = x;
        this.mesh.position.z = -z;
        this.mesh.position.y = 1;
        this.id = id;
    }
    move(x,z){
        this.mesh.position.x = x;
        this.mesh.position.z = -z;
    }
};

function addStar(content) {
    stars.push(new Star(content.newX,content.newY));
}


function addPlanet(content) {
    planets[content.id] = new Planet(content.newX,content.newY, content.id);
}

function movePlanet(content) {
    var planet = planets[content.id];
    if (planet !== undefined) {
        planet.move(content.newX,content.newY);
    }
}



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

function addSpecies(content) {
    // this first line adds the species to the hashmap as well
    var color = speciesMap.getColor(content.name);

}


class SpeciesMap {
    constructor() {
        this.map = {};
        this.mat = {};
        this.count = 0;
        this.colors = ["lightblue", "yellow", "lightpink", "lightgreen", "orange", "white"];
    }

    getMat(name) {
        var mat = this.mat[name];
        if (mat === undefined) {
            mat = new THREE.MeshBasicMaterial({color:color, wireframe:false});
            this.mat[name] = mat;
        }
        return mat;
    }

    getColor(name) {
//        console.log (this);
//        console.log(this.map);
        var color = this.map[name];
        if (color === undefined) {
            color = this.colors[this.count%this.colors.length];
            this.map[name] = color;
            this.mat[name] = new THREE.MeshBasicMaterial({color:color, wireframe:false});
            this.count++;

            var displayName = name.substr(name.lastIndexOf(":")+1);
            var displayQualifier = name.substr(0,name.lastIndexOf(":"));
            if (displayQualifier === "ephemera.eastsideprep.org:stockelements"){
                displayQualifier = "System";
            }
            displayName += " ("+displayQualifier+")";
            
            var chk = document.createElement("input");
            chk.type = "checkbox";
            chk.checked = true;
            species.appendChild(chk);

            var text = document.createElement("span");
            text.style.color = color;
            text.innerText = " "+displayName;
            species.appendChild(text);

            var br = document.createElement("br");
            species.appendChild(br);
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
    starGeo = new THREE.SphereGeometry(2.0, 32, 32);
    planetGeo = new THREE.SphereGeometry(1.0, 32, 32);


    gridHelper = new THREE.GridHelper(size, divisions, "#500000", "#500000");
    scene.add(gridHelper);

    camera.position.z = 310;
    camera.position.y = 220;
    camera.position.x = 20;
    camera.rotation.x = -Math.PI/4;
    //camera.rotation.y = 0.2;

    light = new THREE.AmbientLight(0x404040);
    scene.add(light);

    renderer.render(scene,camera);


    //listeners for keypresses
    document.addEventListener('keyup', keyUp, false);
    document.addEventListener('keydown', keyDown, false);

    // 
    speciesMap = new SpeciesMap();

    println ("initialized");

    //addSpecies({name:"ephemera.eastsideprep.org:stockelements:test1"});
    //addSpecies({name:"someschool.org:someschmuck:test2"});

}

println ("parsed");

