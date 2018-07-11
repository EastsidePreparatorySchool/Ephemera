/*
SpaceCritters online - GM, QB, PM
*/


var updateInterval = 200;

var textarea = document.getElementById("output");
var turnSpan = document.getElementById("turns");
var alienSpan = document.getElementById("numaliens");
var centerDiv = document.getElementById("center");
var species = document.getElementById("species");
var statusP = document.getElementById("status");
var countsP = document.getElementById("counts");
var engineName = document.getElementById("enginename");
var engines = document.getElementById("engines");

var aliens = {};
var planets = {};
var stars = [];
var speciesMap = null; 
var grid = [];

const ADDSPECIES = 1;
const ADDSTAR=2;
const ADDPLANET=3;
const MOVEPLANET=4;
const TURN=5;
const ADD=6;
const MOVE=7;
const KILL=8;





//initialize three.js and set all initial values

var scene;
var camera;
var renderer;
var controls;
var width; 
var height;

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
    request({url: "attach?engine="+engines.value})   
        .then(data => {
            data = JSON.parse(data);
            //println("Initial state: " + data);
            println("Engine id: " + data.engine);
            println("Observer id: " + data.observer);
            println("Total game turns: "+ data.turns);
            turnSpan.innerText = data.turns;
            alienSpan.innerText = 0;
            countsP.style.display = "inline";
            statusP.innerHTML = "Attached to<br>&nbsp;Engine:&nbsp&nbsp&nbsp"+data.engine
                                +"<br>&nbsp;Observer:&nbsp"+data.observer;
            updates();
            //println ("Requested updates in attach");
        })
        .catch(error => {
            println("Error: " + error);
        });;
}


function updates () {
    request({url: "updates?compact=yes"})
        .then(data => {
            if (data !== null) {
                //println("Raw: "+data.substr(0,100));
                data = JSON.parse(data);
                processUpdates(data);
            }
        })
        .catch(error => {
            if (error !== null && error.length > 0) {
                println("Error: '" + error+"'");
            }

            detach();
            println("Server not responding, console detached.");
        });
}

function processUpdates(data){
    var requested = false;
    if (data !== null && data.length > 0) {
        for (var i = 0; i< data.length; i++) {
            // if 90% processed, file another request for updates
            if (i > (data.length*0.9) && !requested) {
                setTimeout(updates, updateInterval);
                requested = true;
                //println ("Requested updates in processUpdates");
            }
            var o = data[i];
            //console.log(o);
            switch (o.type) {
                case TURN:
                    //println("Turn "+o.param1+" complete. #Aliens:"+o.param2);
                    turnSpan.innerText = o.param1;
                    alienSpan.innerText = o.param2;
                    break;

                case ADDSTAR:
                    addStar(o);
                    break;

                case ADDPLANET:
                    addPlanet(o);
                    break;
                case MOVEPLANET:
                    movePlanet(o);
                    break;

                case ADDSPECIES:
                    addSpecies(o);
                    break;

                case ADD:
                    //println ("Adding alien id: "+o.id+", species: "+o.name);
                    addAlien(o);
                    break; 
                case MOVE:
                    //println("Move: id: "+o.id+" ("+o.param1 +","+o.param2+") -> ("+o.newX +","+o.newY+")");
                    moveAlien(o);
                    break;
                case KILL:
                    //println("Alien id: "+o.id+" died.");
                    killAlien(o);
                    break;

                default:
                    println ("unknown record type"+o.type);
                    break;
            }
        }
        renderer.render(scene,camera);
    }
    if (!requested) {
         setTimeout(updates, updateInterval);
         requested = true;
         //println ("Requested delayed updates at the end of processUpdates");
     }

}



function detach() {
    countsP.style.display = "none";
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


function create() {
    request({url: "create?name="+engineName.value})
        .then(data => {
              if (data !== null) {
                  println("  Response: "+data);
                  listEngines();
              }
          })
          .catch(error => {
              if (error !== null && error.length > 0) {
                  println("  Error: '" + error+"'");
              }
          });
}

function start() {
    request({url: "start"})
        .then(data => {
              if (data !== null) {
                  println("  Response: "+data);
              }
              updates();
              //println ("Requested updates in start");
          })
          .catch(error => {
              if (error !== null && error.length > 0) {
                  println("  Error: '" + error+"'");
              }
          });
}

function listEngines() {
    request({url: "listengines"})
        .then(data => {
              if (data !== null) {
                //println ("Raw: "+data);
                data = JSON.parse(data);

                engines.innerHTML = "";
                for (var s in data) {
                    //println("Engine: '"+data[s]+"'");
                            
                    var option = document.createElement("option");
                    option.value = data[s];
                    var optionText = document.createTextNode(data[s]);
                    option.appendChild(optionText);
                    engines.appendChild(option);
                }
              }
          })
          .catch(error => {
              if (error !== null && error.length > 0) {
                  println("  Error: '" + error+"'");
              }
          });
}



function pause() {
    request({url: "pause"})
        .then(data => {
               if (data !== null) {
                   println("  Response: "+data);
               }
           })
           .catch(error => {
               if (error !== null && error.length > 0) {
                   println("  Error: '" + error+"'");
               }
           });
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



// grid to keep track of stacked aliens

class Grid {
    constructor (width, height) {
        this.grid = [];
        this.width = width;
        this.height = height;
        this.halfWidth = Math.floor(width/2);
        this.halfHeight = Math.floor(height/2);
        
        for (var x = 0; x< width; x++) {
            var col = []
            for (var y = 0; y< height; y++) {
                col.push([]);
            }
            this.grid.push(col);
       }
    }

    addToCell(alien, x, y) {
        //console.log(x+", "+y+", "+alien);

        x = Math.floor(x);
        y = Math.floor(y);
        var cell = this.grid[x+this.halfWidth][y+this.halfHeight];
        cell.push(alien);
        return cell.length;
    }

    removeFromCell(alien, x, y) {
        //console.log(x+", "+y+", "+alien);

        x = Math.floor(x);
        y = Math.floor(y);
        var cell = this.grid[x+this.halfWidth][y+this.halfHeight];
        var index = cell.indexOf(alien);
        if (index !== undefined) {
            cell.splice(index, 1);
        }
    }
}



// alien class, use this to make aliens
class Alien {
    constructor(material,x,z,id){
        this.mat = material; 
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

    setHeight(height) {
        this.mesh.position.y = 2*height;
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
    var alien = new Alien(speciesMap.getMat(content.name),content.newX,content.newY)
    aliens[content.id] = alien;
    grid.addToCell(alien, content.newX, content.newY);
}

function moveAlien(content) {
    var alien = aliens[content.id];
    if (alien === undefined) {
        return;
    }

    alien.move(content.newX,content.newY);
    grid.removeFromCell(alien, content.param1, content.param2);
    var height = grid.addToCell(alien, content.newX, content.newY);
    alien.setHeight(height);
}
        
function killAlien(content) {
    var alien = aliens[content.id];
    if (alien !== undefined) {
          alien.kill();
    }
}

function addSpecies(content) {
    // this first line adds the species to the hashmap as well
    speciesMap.getColor(content.name, content.param1);

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
            getColor(name);
            mat = this.mat[name];
        }
        return mat;
    }

    getColor(name, instantiate) {
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
            chk.checked = instantiate;
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
    width = $('#center').width();
    height = $('#center').height();

    if (!Detector.webgl) {
        Detector.addGetWebGLMessage();
    }

    camera = new THREE.PerspectiveCamera(75, width/ height, 0.1, 1000 );
    camera.position.set(310, 220, 0);
    camera.rotation.x = -Math.PI/4;

    renderer = new THREE.WebGLRenderer();
    renderer.setPixelRatio( window.devicePixelRatio );
    renderer.setSize(width, height);
    centerDiv.appendChild(renderer.domElement);

    cubeGeo = new THREE.BoxGeometry(0.9, 0.9, 0.9);
    starGeo = new THREE.SphereGeometry(2.0, 32, 32);
    planetGeo = new THREE.SphereGeometry(1.0, 32, 32);


    gridHelper = new THREE.GridHelper(size, divisions, "#500000", "#500000");
    scene.add(gridHelper);


    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true; // an animation loop is required when either damping or auto-rotation are enabled
    controls.dampingFactor = 0.25;
    controls.screenSpacePanning = false;
    controls.minDistance = 100;
    controls.maxDistance = 500
    controls.maxPolarAngle = Math.PI / 2;

    light = new THREE.AmbientLight(0x404040);
    scene.add(light);

    renderer.render(scene,camera);


    //listeners for keypresses
    //document.addEventListener('keyup', keyUp, false);
    //document.addEventListener('keydown', keyDown, false);
    window.addEventListener( 'resize', onWindowResize, false );

    animate();

    // 
    speciesMap = new SpeciesMap();
    grid = new Grid(501,501);


    //addSpecies({name:"ephemera.eastsideprep.org:stockelements:test1"});
    //addSpecies({name:"someschool.org:someschmuck:test2"});
    //grid.addToCell("hah1", -250,-250);
    //grid.addToCell("hah2", -250,250);
    //grid.addToCell("hah3", 250,-250);
    //println(""+grid.addToCell("hah4", 250,250));
    //println(""+grid.addToCell("hah5", 250,250));

    listEngines();

    println ("initialized");
}

function onWindowResize() {
    width = $('#center').width();
    height = $('#center').height();
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
}




function animate() {
    requestAnimationFrame(animate);

    // required if controls.enableDamping or controls.autoRotate are set to true
    controls.update();

    renderer.render(scene, camera);
}


println ("parsed");

