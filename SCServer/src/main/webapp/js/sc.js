/*
 SpaceCritters online - GM, QB, PM
 */


var updateInterval = 50;
var updateIntervalInactive = 1000;
var updateIntervalActive = 50;
var fightLength = 10;

var textarea = document.getElementById("output");
var turnSpan = document.getElementById("turns");
var alienSpan = document.getElementById("numaliens");
var centerDiv = document.getElementById("center");
var species = document.getElementById("species");
var statusP = document.getElementById("status");
var countsP = document.getElementById("counts");
var intervalSpan = document.getElementById("interval");
var observersSpan = document.getElementById("observers");
var memstatsSpan = document.getElementById("memstats");
var livenessSpan = document.getElementById("liveness");
var logsizeSpan = document.getElementById("logsize");
var observerlistP = document.getElementById("observerlist");
var engineName = document.getElementById("enginename");
var engines = document.getElementById("engines");
var startpauseB = document.getElementById("startpause");
var attachP = document.getElementById("attach");
var adminP = document.getElementById("admin");

var aliens = {};
var planets = {};
var stars = [];
var speciesMap = null;
var grid = [];
var fights = [];
var observers = 0;
// global states
var attached = false;
var running = false;

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
var orbitMaterial = null;
var fightMaterial;



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
;
// main functionality accessible from buttons

function attach(engine) {
    if (engine === undefined) {
        engine = engines.value;
    }
    makeClientID();
    request({url: "protected/attach?engine=" + engine + "&clientID=" + getClientID()})
            .then(data => {
                data = JSON.parse(data);
                uiStateChange(true, undefined, data);
            })
            .catch(error => {
                println("Error: " + error);
            });
    ;
}


function updates() {
    var start = (new Date()).getTime();
    request({url: "protected/updates?compact=yes&clientID=" + getClientID()})
            .then(data => {
                if (data !== null) {
                    //println("Raw: "+data.substr(0,100));
                    var end = (new Date()).getTime();
                    reactToServiceTime(start, end);
                    data = JSON.parse(data);
                    try {
                        processUpdates(data);
                    } catch (err) {
                        println("updates: error in processUpdates, " + err);
//                        console.log(err.stack);
//                        console.trace();
                    }
                }
                updateCounts();
            })
            .catch(error => {
                if (error !== null && error.length > 0) {
                    println("Error: '" + error + "'");
                }

                detach();
                println("Server not responding, console detached.");
            });
}

function reactToServiceTime(start, end) {
    var t = end - start;
//    if (t > (updateInterval / 2)) {
//        // if request took more than 50% of our interval time, get slower
//        updateInterval = Math.floor(updateInterval * 1.1);
//    } else if (t < (updateInterval / 20)) {
//        // if it took less that 5% of our interval time, get faster
//        updateInterval = Math.floor(updateInterval / 1.1);
//    }
    intervalSpan.innerText = t;
}

function processUpdates(data) {
    if (!attached) {
        return;
    }
    var requested = false;
    if (data !== null && data.length > 0) {
        for (var i = 0; i < data.length; i++) {
// if 50% processed, file another request for updates
            if (i > (data.length * 0.5) && !requested) {
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
                case ORBIT:
                    drawOrbit(o.id, o.newX, o.newY, o.energy, o.tech, Number(name));
                    break;
                case KILL:
                    //println("Alien id: "+o.id+" died.");
                    killAlien(o);
                    break;
                case FIGHT:
                    showFight(o.newX, o.newY);
                    break;
                case STATECHANGE:
                    //println("Alien id: "+o.id+" died.");
                    println("StateChange: " + (o.id === 0 ? "Paused" : "Running"));
                    uiStateChange(undefined, o.id !== 0, null);
                    break;
                default:
                    println("unknown record type" + o.type);
                    break;
            }
        }
        renderer.render(scene, camera);
    }
    if (!requested) {
        setTimeout(updates, updateInterval);
        requested = true;
        //println ("Requested delayed updates at the end of processUpdates");
    }
}

function drawOrbit(id, focusX, focusY, e, p, rotation) {
    //println("Orbit: (" + focusX + "," + focusY + "), ecc:" + e + ", p:" + p + ", rot:" + rotation);
    var a = p / (1 - e * e);
    var b = a * Math.sqrt(1 - e * e);
    var cf = a - b;
    var focus = new THREE.Vector2(focusX, focusY);
    var offset = new THREE.Vector2(1, 0).rotateAround(new THREE.Vector2(0, 0), rotation);
    focus = focus.sub(offset);

    var mesh = drawEllipse(focus.x, focus.y, a, b, rotation);

    var a = aliens[id];
    if (a !== undefined) {
        if (a.orbit !== null) {
            scene.remove(a.orbit);
        }
        a.orbit = mesh;
        scene.add(a.orbit);
    }
}

function drawEllipse(centerX, centerY, radiusX, radiusY, rotation) {
    var curve = new THREE.EllipseCurve(
            -centerY, -centerX,
            radiusY, radiusX,
            0, 2 * Math.PI,
            false,
            rotation
            );
    var points = curve.getPoints(50);
    var geometry = new THREE.BufferGeometry().setFromPoints(points);
    // Create the final object to add to the scene
    var ellipse = new THREE.Line(geometry, orbitMaterial);
    ellipse.rotation.x = Math.PI / 2;
    return ellipse;
}


function showFight(x, y) {
    var mesh = new THREE.Mesh(planetGeo, fightMaterial);
    mesh.position.x = -y;
    mesh.position.z = -x;
    mesh.position.y = 1;
    mesh.scale.set(5, 5, 5);
    scene.add(mesh);
    fights.push(mesh);
}


function listAliens() {
    var a;
    var count = 0;
    for (a in aliens) {
        var alien = aliens[a];
        println(" id:" + alien.id + ", (" + (-alien.mesh.position.z) + "," + (-alien.mesh.position.x) + "), h:" + alien.mesh.position.y);
        if (count++ > 100) {
            break;
        }
    }
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


function uiStateChange(attachState, runState, data) {
    if (attachState !== undefined && attachState !== attached) {
        // the attach state changed
        attached = attachState;
        if (attached) {
            // now attached
            //println("Initial state: " + data);
            document.title = "Game: " + data.engine + " (SpaceCritters)";
            println("Engine id: " + data.engine);
            println("Observer id: " + data.observer);
            println("Total game turns: " + data.turns);
            turnSpan.innerText = data.turns;
            alienSpan.innerText = 0;
            intervalSpan.innerText = updateInterval;
            observersSpan.innerText = data.observers;
            countsP.style.display = "inline";
            statusP.innerHTML = "Attached to<br>&nbsp;Game:&nbsp&nbsp&nbsp" + data.engine
                    + "<br>&nbsp;Observer:&nbsp" + data.observer;
            attachP.style.display = "none";
            speciesMap = new SpeciesMap();
            grid = new Grid(501, 501);
            updateInterval = updateIntervalActive;
            getStatus();
            queryAdmin();
            updates();
        } else {
            // now detached
            println("Last recorded turn: " + turnSpan.innerText);
            document.title = "Game: <detached> (SpaceCritters)";
            countsP.style.display = "none";
            statusP.innerHTML = "";
            species.innerHTML = "";
            observerlistP.innerHTML = "";
            attachP.style.display = "inline";
            speciesMap = null;
            grid = null;
            //println("starting purge ...");

            var a;
            var count = 0;
            for (a in aliens) {
                scene.remove(aliens[a].mesh);
                if (aliens[a].orbit !== null) {
                    scene.remove(aliens[a].orbit);
                }
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
            for (count = 0; count < stars.length; count++) {
                scene.remove(stars[count].mesh);
            }
            stars = [];
            //println(" ... extinguished "+count+" stars");

            fights.forEach((f) => {
                scene.remove(f);
            });
            fights = [];

            renderer.render(scene, camera);
            //println("purge complete.");
            updateInterval = updateIntervalInactive;
        }
    }

    if (runState !== undefined && runState !== running) {
        running = runState;
        if (running) {
            // now running
            startpauseB.innerText = "Pause";
            startpauseB.onclick = pause;
            updateInterval = updateIntervalActive;
        } else {
            // now paused
            startpauseB.innerText = "Start";
            startpauseB.onclick = start;
            updateInterval = updateIntervalInactive;
        }
    }
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

function listEngines() {
    request({url: "protected/listengines"})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    data = JSON.parse(data);
                    engines.innerHTML = "";
                    for (var i = 0; i < data.length; i++) {
                        //println("Engine: '"+data[i].name+"'");

                        var option = document.createElement("option");
                        option.value = data[i].name;
                        var optionText = document.createTextNode(data[i].name);
                        option.appendChild(optionText);
                        engines.appendChild(option);
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
    if (!attached) {
        return;
    }

    request({url: "protected/listobservers?clientID=" + getClientID()})
            .then(data => {
                if (data !== null) {
                    //println ("Raw: "+data);
                    data = JSON.parse(data);
                    var list = "Observers:<br>";
                    for (var i = 0; i < data.length; i++) {
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











// grid to keep track of stacked aliens

class Grid {
    constructor(width, height) {
        this.grid = [];
        this.width = width;
        this.height = height;
        this.halfWidth = Math.floor(width / 2);
        this.halfHeight = Math.floor(height / 2);
        for (var x = 0; x < width; x++) {
            var col = [];
            for (var y = 0; y < height; y++) {
                col.push([]);
            }
            this.grid.push(col);
        }
    }

    addToCell(alien, x, y) {
        x = Math.floor(x);
        y = Math.floor(y);
        var cell = this.grid[x + this.halfWidth][y + this.halfHeight];
        assert(() => (alien.getX() === x));
        assert(() => (alien.getY() === y));
        assert(() => (!cell.includes(alien)));
        var h;
        h = cell.length;
        cell.push(alien);
        dprintln(" cell: added at height: " + h);
        alien.setHeight(h);
        return h;
    }

    removeFromCell(alien, x, y) {
        x = Math.floor(x);
        y = Math.floor(y);
        var cell = this.grid[x + this.halfWidth][y + this.halfHeight];
        assert(() => (alien.getX() === x));
        assert(() => (alien.getY() === y));
        assert(() => (cell.includes(alien)));
        assert(() => (cell.length > alien.getHeight()), () => dumpAlienAndCell(alien, cell));
        var index = cell.indexOf(alien);
        if (index !== -1) {
            dprintln(" cell: removing at index: " + index + ", length before remove: " + cell.length);
            cell.splice(index, 1);
            cell.forEach((a, i) => {
                a.setHeight(i);
                assert(() => (a.getHeight() === i));
            });
        }
    }
}



// alien class, use this to make aliens
class Alien {
    constructor(material, x, z, id) {
        this.mat = material;
        this.mesh = new THREE.Mesh(cubeGeo, this.mat);
        scene.add(this.mesh);
        this.mesh.position.x = -z;
        this.mesh.position.z = -x;
        this.mesh.position.y = 1;
        this.id = id;
        this.orbit = null;
    }

    move(x, y) {
        this.mesh.position.x = -y;
        this.mesh.position.z = -x;
    }

    getX() {
        return -this.mesh.position.z;
    }

    getY() {
        return -this.mesh.position.x;
    }

    getHeight() {
        return this.mesh.position.y / 2;
    }

    kill() {
        scene.remove(this.mesh);
        delete aliens[this.id];
    }

    setHeight(height) {
        this.mesh.position.y = 2 * height;
    }
    getHeight() {
        return (this.mesh.position.y / 2);
    }
}

// star class, use this to make aliens
class Star {
    constructor(x, z) {
        this.mat = new THREE.MeshBasicMaterial({color: "white", wireframe: false});
        this.mesh = new THREE.Mesh(starGeo, this.mat);
        scene.add(this.mesh);
        this.mesh.position.x = -z;
        this.mesh.position.z = -x;
        this.mesh.position.y = 1;
    }

}
;
// planet class, use this to make aliens
class Planet {
    constructor(x, z, id) {
        this.mat = new THREE.MeshBasicMaterial({color: "green", wireframe: false});
        this.mesh = new THREE.Mesh(planetGeo, this.mat);
        scene.add(this.mesh);
        this.mesh.position.x = -z;
        this.mesh.position.z = -x;
        this.mesh.position.y = 1;
        this.id = id;
    }
    move(x, z) {
        this.mesh.position.x = -z;
        this.mesh.position.z = -x;
    }
}
;
function addStar(content) {
    stars.push(new Star(content.newX, content.newY));
}


function addPlanet(content) {
    planets[content.id] = new Planet(content.newX, content.newY, content.id);
}

function movePlanet(content) {
    var planet = planets[content.id];
    if (planet !== undefined) {
        planet.move(content.newX, content.newY);
    }
}



function addAlien(content) {
    dprintln("adding alien " + content.id + " at " + content.newX + "," + content.newY);
    var alien = new Alien(speciesMap.getMat(content.speciesName), content.newX, content.newY, content.id);
    aliens[content.id] = alien;
    grid.addToCell(alien, content.newX, content.newY);
    speciesMap.addAlien(content.speciesId);
}

function moveAlien(content) {
    dprintln("moving alien " + content.id + " from " + content.param1 + "," + content.param2 + " to " + content.newX + "," + content.newY);
    var alien = aliens[content.id];
    if (alien === undefined) {
        dprintln("unknown alien for move");
        return;
    }
    grid.removeFromCell(alien, content.param1, content.param2);
    alien.move(content.newX, content.newY);
    grid.addToCell(alien, content.newX, content.newY);
}

function killAlien(content) {
    dprintln("killing alien " + content.id + " at " + content.newX + "," + content.newY);
    var alien = aliens[content.id];
    if (alien !== undefined) {
        grid.removeFromCell(alien, content.newX, content.newY);
        alien.kill();
        speciesMap.removeAlien(content.speciesId);
        if (alien.orbit !== null) {
            scene.remove(alien.orbit);
        }
    }
}

function addSpecies(content) {
    speciesMap.registerSpecies(content.speciesName, content.speciesId, content.param2);
}


class SpeciesMap {
    constructor() {
        this.map = {};
        this.mat = {};
        this.name = {};
        this.id = {};
        this.maxId = 0;
        this.aliens = {}; // counts
        this.count = 0;
        this.colors = ["lightblue", "yellow", "lightpink", "lightgreen", "orange", "white"];
    }

    getMat(name) {
        var mat = this.mat[name];
        return mat;
    }

    addAlien(id) {
        if (id > this.maxId || id < 1) {
            println("sm.addAlien: invalid id");
            return;
        }
        try {
            this.aliens[id]++;
        } catch (err) {
            println("removeAlien id " + id + " error: " + err.name);
        }
    }

    removeAlien(id) {
        if (id > this.maxId || id < 1) {
            println("sm.removeAlien: invalid id");
            return;
        }
        try {
            this.aliens[id]--;
        } catch (err) {
            println("removeAlien id " + id + " error: " + err.name);
        }
    }

    getColor(name, id, instantiate) {
//        console.log (this);
//        console.log(this.map);
        var color = this.map[name];
        if (color === undefined) {
            color = registerSpecies(name, id, instantiate);
        }
        return color;
    }

    registerSpecies(name, id, instantiate) {
        var color = this.colors[this.count % this.colors.length];
        this.map[name] = color;
        this.mat[name] = new THREE.MeshBasicMaterial({color: color, wireframe: false});
        this.count++;
        if (id > this.maxId) {
            this.maxId = id;
        }
        this.id[name] = id;
        this.name[id] = name;
        this.aliens[id] = 0;
        var displayName = name.substr(name.lastIndexOf(":") + 1);
        var displayQualifier = name.substr(0, name.lastIndexOf(":"));
        if (displayQualifier === "org.eastsideprep.spacecritters:org.eastsideprep.spacecritters.stockelements") {
            displayQualifier = "System";
        }
        var chk = document.createElement("input");
        chk.type = "checkbox";
        chk.checked = instantiate;
        chk.id = "chk" + id;
        chk.onclick = function () {
            processCheck(id);
        };
        species.appendChild(chk);
        var text = document.createElement("span");
        text.style.color = color;
        text.innerText = " " + displayName + ": ";
        text.className = "tooltip";
        species.appendChild(text);
        var text2 = document.createElement("span");
        text2.style.color = color;
        text2.id = "species" + id;
        text2.innerText = "0";
        species.appendChild(text2);
        var tip = document.createElement("span");
        tip.className = "tooltiptext";
        tip.innerText = " " + displayQualifier + ":" + id;
        text.appendChild(tip);
        var br = document.createElement("br");
        species.appendChild(br);
        //println("registered species " + name + ", id:" + id);
        return color;
    }
}

function updateCounts() {
    if (!attached) {
        return;
    }
    try {
        for (var speciesId in speciesMap.aliens) {
            if (speciesId < 1 || speciesId > speciesMap.maxId) {
                println("updateCounts: speciesMap corrupt");
                return;
            }
            var countSpan = document.getElementById("species" + speciesId);
            countSpan.innerText = speciesMap.aliens[speciesId];
        }
    } catch (err) {
        println("error in updateCounts: " + err.name);
    }
}


function processCheck(id) {
    var chk = document.getElementById("chk" + id);
    println("sending request to change state of species id " + id + " to " + (chk.checked ? "on" : "off"));
    request({url: "protected/check?id=" + id
                + "&selected=" + (chk.checked ? "on" : "off")
                + "&clientID=" + getClientID()})
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



//   public void buildTrajectory(Trajectory t, Color c) {
//        Iterator<Vector2> positions = t.toDraw(100);
//        
//        Vector2 past = positions.next();
//        past.x = Scene3D.xFromX(past.x);
//        past.y = Scene3D.zFromY(past.y);
//        
//        PhongMaterial mat = new PhongMaterial(c);
//        getChildren().remove(lines);
//        lines = new Group();
//        while (positions.hasNext()) {
//            Vector2 next = positions.next();
//            next.x = Scene3D.xFromX(next.x);
//            next.y = Scene3D.zFromY(next.y);
//            
//            Box line = new BoxLine(past, next);
//            line.setMaterial(mat);
//            lines.getChildren().add(line);
//            
//            past = next;
//        }
//        
//        Vector2 offset = t.positionOfFocus();
//        offset.x = Scene3D.xFromX(offset.x);
//        offset.y = Scene3D.zFromY(offset.y);
//        lines.getTransforms().add(new Translate(offset.x, 0, offset.y));
//        
//        getChildren().add(lines);
//        
//    }







function init() {
    scene = new THREE.Scene();
    width = $('#center').width();
    height = $('#center').height();
    camera = new THREE.PerspectiveCamera(100, width / height, 0.1, 1000);
    camera.position.set(350, 120, 0);
    camera.rotation.x = -Math.PI / 4;
    renderer = new THREE.WebGLRenderer();
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(width, height);
    centerDiv.appendChild(renderer.domElement);
    cubeGeo = new THREE.BoxGeometry(0.9, 0.9, 0.9);
    starGeo = new THREE.SphereGeometry(2.0, 32, 32);
    planetGeo = new THREE.SphereGeometry(1.0, 32, 32);
    orbitMaterial = new THREE.LineBasicMaterial({color: "goldenrod"});
    fightMaterial = new THREE.LineBasicMaterial({color: "red"});

    gridHelper = new THREE.GridHelper(size, divisions, "#500000", "#500000");
    scene.add(gridHelper);
    controls = new THREE.OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true; // an animation loop is required when either damping or auto-rotation are enabled
    controls.dampingFactor = 0.25;
    controls.screenSpacePanning = false;
    controls.minDistance = 10;
    controls.maxDistance = 500;
    controls.maxPolarAngle = Math.PI / 2;
    light = new THREE.AmbientLight(0x404040);
    scene.add(light);
    renderer.render(scene, camera);
    window.addEventListener("resize", onWindowResize, false);
    window.addEventListener("beforeunload", detach);
    animate();
    makeClientID();
    listEngines();
    println("initialized");
    var parameter = location.search.substring(1);
    // if called from another page with attach param, attach right now
    if (parameter !== null && parameter.length > 0) {
        var fields = parameter.split("=");
        if (fields.length > 1
                && fields[0].toLowerCase() === "attach"
                && fields[1].length > 0) {
            attach(fields[1]);
        }
    }
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
    var newFights = [];
    fights.forEach((f) => {
        var s = f.scale.x;
        if (s < 1) {
            scene.remove(f);
        } else {
            f.scale.set(s - (1/fightLength), s - (1/fightLength), s - (1/fightLength));
            newFights.push(f);
        }
    });
    fights = newFights;
    renderer.render(scene, camera);
}




function submitForm(form) {
    if (!attached) {
        println("upload: must be attached to upload alien jar");
        return false;
    }

    var body = new FormData(form);
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


// 
function makeClientID() {
    window.sessionStorage.setItem("clientID", "" + ((new Date()).getTime()) % 100000);
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


function  dumpAlienAndCell(alien, cell) {
    dprintln("dump: alien:" + alien.id);
    dprintln(" height:" + alien.mesh.position.y / 2);
    dprintln(" cell:");
    cell.forEach((a, i) => dprintln(" i:" + i + ", a:" + a.id));
}


println("parsed");

