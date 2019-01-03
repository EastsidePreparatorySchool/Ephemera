/*
 SpaceCritters online - GM, QB, PM
 */


let aliens = {};
let planets = {};
let stars = [];
let speciesMap = null;
let grid = [];
let fights = [];
let burns = [];
let observers = 0;
let engineList = null;
let attachedName = null;
// global states
let attached = false;
let running = false;




// grid to keep track of stacked aliens
class Grid {
    constructor(width, height) {
        this.grid = Array(width);
        this.width = width;
        this.height = height;
        this.halfWidth = Math.floor(width / 2);
        this.halfHeight = Math.floor(height / 2);
        for (let x = 0; x < width; x++) {
            let col = Array(height);

            this.grid[x] = col;
        }
    }

    addToCell(alien, x, y) {
        x = Math.round(x);
        y = Math.round(y);
        let cell = this.grid[x + this.halfWidth][y + this.halfHeight];
        if (cell === undefined) {
            cell = [];
            this.grid[x + this.halfWidth][y + this.halfHeight] = cell;
        }
        assert(() => (Math.round(alien.getX()) === x));
        assert(() => (Math.round(alien.getY()) === y));
        assert(() => (!cell.includes(alien)));
        let h;
        h = cell.length;
        cell.push(alien);
        dprintln(" cell: added at height: " + h);
        alien.setHeight(h);
        return h;
    }

    removeFromCell(alien, x, y) {
        x = Math.round(x);
        y = Math.round(y);
        let cell = this.grid[x + this.halfWidth][y + this.halfHeight];
        assert(() => (Math.round(alien.getX()) === x));
        assert(() => (Math.round(alien.getY()) === y));
        assert(() => (cell.includes(alien)));
        assert(() => (cell.length > alien.getHeight()), () => dumpAlienAndCell(alien, cell));
        let index = cell.indexOf(alien);
        if (index !== -1) {
            dprintln(" cell: removing at index: " + index + ", length before remove: " + cell.length);
            cell.splice(index, 1);
            if (cell.length === 0) {
                this.grid[x + this.halfWidth][y + this.halfHeight] = undefined;
            } else {
                cell.forEach((a, i) => {
                    a.setHeight(i);
                    assert(() => (a.getHeight() === i));
                });
            }
        }
    }
}

// alien class, use this to make aliens
class Alien {
    constructor(material, x, z, id, speciesId) {
        this.mat = material;
        this.mesh = new THREE.Mesh(cubeGeo, this.mat);
        scene.add(this.mesh);
        this.mesh.position.x = -z;
        this.mesh.position.z = -x;
        this.mesh.position.y = 1;
        this.id = id;
        this.speciesId = speciesId;
        this.orbit = null;
        this.trail = new Trail();
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
        this.mesh.position.y = 2 * height + 1;
    }
    getHeight() {
        return ((this.mesh.position.y - 1) / 2);
    }
}

// star class, use this to make aliens
class Star {
    constructor(x, y, mag) {
        mag = Math.max(mag, 10);
        this.mat = new THREE.MeshBasicMaterial({color: "white", wireframe: false});
        this.mesh = new THREE.Mesh(starGeo, this.mat);
        this.mesh.scale.set(mag / 10, mag / 10, mag / 10);
        scene.add(this.mesh);
        this.mesh.position.x = -y;
        this.mesh.position.z = -x;
        this.mesh.position.y = mag / 10;
    }
}

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
        this.orbit = null;
        this.trail = new Trail();
    }
    move(x, z) {
        this.mesh.position.x = -z;
        this.mesh.position.z = -x;
    }
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
        let mat = this.mat[name];
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
        let color = this.map[name];
        if (color === undefined) {
            color = registerSpecies(name, id, instantiate);
        }
        return color;
    }

    registerSpecies(name, id, instantiate) {
        let color = this.colors[this.count % this.colors.length];
        this.map[name] = color;
        this.mat[name] = new THREE.MeshBasicMaterial({color: color, wireframe: false});
        this.count++;
        if (id > this.maxId) {
            this.maxId = id;
        }
        this.id[name] = id;
        this.name[id] = name;
        this.aliens[id] = 0;
        let displayName = name.substr(name.lastIndexOf(":") + 1);
        let displayQualifier = name.substr(0, name.lastIndexOf(":"));
        if (displayQualifier === "org.eastsideprep.spacecritters:org.eastsideprep.spacecritters.stockelements") {
            displayQualifier = "System";
        }
        let chk = document.createElement("input");
        chk.type = "checkbox";
        chk.checked = instantiate;
        chk.id = "chk" + id;
        chk.onclick = function () {
            processCheck(id);
        };
        species.appendChild(chk);
        let text = document.createElement("span");
        text.style.color = color;
        text.innerText = " " + displayName + ": ";
        text.className = "tooltip";
        species.appendChild(text);
        let text2 = document.createElement("span");
        text2.style.color = color;
        text2.id = "species" + id;
        text2.innerText = "0";
        text2.onmouseover = function () {
            //println("highlight " + id);
            for (let a in aliens) {
                let al = aliens[a];
                if (al.speciesId === id) {
                    al.mesh.scale.set(2, 20, 2);
                }
            }
            ;
        };
        text2.onmouseout = function () {
            //println("highlight off");
            for (let a in aliens) {
                let al = aliens[a];
                if (al.speciesId === id) {
                    al.mesh.scale.set(1, 1, 1);
                }
            }
            ;
        };
        species.appendChild(text2);
        let tip = document.createElement("span");
        tip.className = "tooltiptext";
        tip.innerText = " " + displayQualifier + ":" + id;
        text.appendChild(tip);
        let br = document.createElement("br");
        species.appendChild(br);
        //println("registered species " + name + ", id:" + id);
        return color;
    }
}




function processUpdates(data) {
    if (!attached) {
        return;
    }
    let requested = false;
    if (data !== null && data.length > 0) {
        for (let i = 0; i < data.length; i++) {
// if 50% processed, file another request for updates
            if (i > (data.length * 0.5) && !requested) {
                //setTimeout(updates, updateInterval);
                requested = true;
                //println ("Requested updates in processUpdates");
            }
            let o = data[i];
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
                    drawOrbit(o.id, o.newX, o.newY, o.energy, o.tech, Number(o.name), o.param1, o.param2);
                    break;
                case KILL:
                    //println("Alien id: "+o.id+" died.");
                    killAlien(o);
                    break;
                case FIGHT:
                    showFight(o.newX, o.newY);
                    break;
                case BURN:
                    showBurn(o.id, o.param1, o.param2, o.energy, o.tech);
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
    if (!requested) requested = true;
}




function addStar(content) {
    stars.push(new Star(content.newX, content.newY, content.param1));
}


function addPlanet(content) {
    planets[content.id] = new Planet(content.newX, content.newY, content.id);
}

function movePlanet(content) {
    let planet = planets[content.id];
    if (planet !== undefined) {
        planet.move(content.newX, content.newY);
        planet.trail.addPoint(content.param1, content.param2);
    } else {
        println("planet index " + content.id + " undefined");
    }
}



function addAlien(content) {
    dprintln("adding alien " + content.id + " at " + content.newX + "," + content.newY);
    let alien = new Alien(speciesMap.getMat(content.speciesName), content.newX, content.newY, content.id, content.speciesId);
    aliens[content.id] = alien;
    grid.addToCell(alien, content.newX, content.newY);
    speciesMap.addAlien(content.speciesId);
}

function moveAlien(content) {
    dprintln("moving alien " + content.id + " from " + content.param1 + "," + content.param2 + " to " + content.newX + "," + content.newY);
    let alien = aliens[content.id];
    if (alien === undefined) {
        dprintln("unknown alien for move");
        return;
    }
    grid.removeFromCell(alien, content.param1, content.param2);
    alien.move(content.newX, content.newY);
    grid.addToCell(alien, content.newX, content.newY);
    if (alien.orbit !== null) {
        alien.trail.addPoint(content.param1, content.param2);
    }
}

function killAlien(content) {
    dprintln("killing alien " + content.id + " at " + content.newX + "," + content.newY);
    let alien = aliens[content.id];
    if (alien !== undefined) {
        grid.removeFromCell(alien, content.newX, content.newY);
        alien.kill();
        speciesMap.removeAlien(content.speciesId);
        if (alien.orbit !== null) {
            scene.remove(alien.orbit);
        }
        if (alien.vector !== null) {
            scene.remove(alien.vector);
        }
        alien.trail.delete();
    }
}

function addSpecies(content) {
    speciesMap.registerSpecies(content.speciesName, content.speciesId, content.param2);
}
