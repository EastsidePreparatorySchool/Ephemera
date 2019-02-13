let textarea = document.getElementById("output");
let turnSpan = document.getElementById("turns");
let alienSpan = document.getElementById("numaliens");
let centerDiv = document.getElementById("center");
let species = document.getElementById("species");
let statusP = document.getElementById("status");
let countsP = document.getElementById("counts");
let intervalSpan = document.getElementById("interval");
let observersSpan = document.getElementById("observers");
let memstatsSpan = document.getElementById("memstats");
let livenessSpan = document.getElementById("liveness");
let logsizeSpan = document.getElementById("logsize");
let sleeptimeSpan = document.getElementById("sleeptime");
let observerlistP = document.getElementById("observerlist");
let engineName = document.getElementById("enginename");
let engines = document.getElementById("engines");
let startpauseB = document.getElementById("startpause");
let attachP = document.getElementById("attach");
let adminP = document.getElementById("admin");
let slowMode = document.getElementById("slowmode");
let debugCheck = document.getElementById("debug");


function debugMode() { debug = debugCheck.checked; }

function reactToServiceTime(start, end) {
  let t = end - start;
  intervalSpan.innerText = t;
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
      
      //updateInterval = updateIntervalActive;
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
      controls.autoRotate = false;

      //println("starting purge ...");

      let a;
      let count = 0;
      for (a in aliens) {
          scene.remove(aliens[a].mesh);
          if (aliens[a].orbit !== null) {
              scene.remove(aliens[a].orbit);
          }
          if (aliens[a].vector !== null) {
              scene.remove(aliens[a].vector);
          }
          aliens[a].trail.delete();
          count++;
      }
      aliens = {};
      //println(" ... killed "+count+" aliens");

      let p;
      count = 0;
      for (p in planets) {
          scene.remove(planets[p].mesh);
          if (planets[p].orbit !== null) {
              scene.remove(planets[p].orbit);
          }
          planets[p].trail.delete();
          count++;
      }
      planets = {};
      //println(" ... destroyed "+count+" planets");

      let s;
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

      // in 2 seconds, start polling to reattach
      setTimeout(() => {
          engineList = null;
          setInterval(reattach, 200);
      }, 2000);
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


function updateCounts() {
  if (!attached) return;
  try {
    for (let speciesId in speciesMap.aliens) {
        if (speciesId < 1 || speciesId > speciesMap.maxId) {
            println("updateCounts: speciesMap corrupt");
            return;
        }
        let countSpan = document.getElementById("species" + speciesId);
        countSpan.innerText = speciesMap.aliens[speciesId];
    }
  } catch (err) {
    println("error in updateCounts: " + err.name);
  }
}


function  dumpAlienAndCell(alien, cell) {
  dprintln("dump: alien:" + alien.id);
  dprintln(" height:" + alien.mesh.position.y / 2);
  dprintln(" cell:");
  cell.forEach((a, i) => dprintln(" i:" + i + ", a:" + a.id));
}
