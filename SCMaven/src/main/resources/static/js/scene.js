/*
 SpaceCritters online - GM, QB, PM
 */



let scene = new THREE.Scene();;
let camera;
let renderer;
let controls;
let width;
let height;
let cubeGeo = new THREE.BoxGeometry(1.0, 1.0, 1.0);
let sphereGeo = new THREE.SphereGeometry(1.0, 32, 32);
let starGeo = new THREE.SphereGeometry(1.0, 32, 32);
let planetGeo = new THREE.SphereGeometry(0.7, 32, 32);
let gridHelper;
let light;
let size = 5001;
let rotation = 0;


let orbitMaterial = new THREE.LineBasicMaterial({color: "goldenrod"});
let fightMaterial = new THREE.MeshBasicMaterial({color: "red"});
let trailMaterial = new THREE.MeshBasicMaterial({color: "lightblue", wireframe: false});
let autorotateTimeout = null;



let fightLength = 10;
let burnLength = 15;
let trailLength = 15;
let vMultiplier = 10000; // drawvelocity vector 10000 times bigger than they really are
let aMultiplier = 30000; // draw burns (deltaV) 10000 times bigger than they really are
let aSize = 3; // initial size of burn spheres




 function init() {
     width = $('#center').width();
     height = $('#center').height();
     camera = new THREE.PerspectiveCamera(100, width / height, 1, 8000);
     camera.position.set(50, 50, 0);
     camera.rotation.x = -Math.PI / 4;
     renderer = new THREE.WebGLRenderer();
     renderer.setPixelRatio(window.devicePixelRatio);
     renderer.setSize(width, height);
     centerDiv.appendChild(renderer.domElement);


     gridHelper = new THREE.GridHelper(size, size, "#500000", "#500000");
     scene.add(gridHelper);

     //origin lines
     let xa = drawLine(0, 0, size / 2, 0, fightMaterial);
     xa.position.y = 0.5;
     let ya = drawLine(0, 0, 0, size / 2);
     ya.position.y = 0.5;
     scene.add(xa);
     scene.add(ya);

     //setup controls
     controls = new THREE.OrbitControls(camera, renderer.domElement);
     controls.enableDamping = true; // an animation loop is required when either damping or auto-rotation are enabled
     controls.dampingFactor = 0.25;
     controls.screenSpacePanning = false;
     controls.minDistance = 10;
     controls.maxDistance = 6000;
     controls.maxPolarAngle = Math.PI / 2;
     controls.autoRotate = false;
     controls.autoRotateSpeed = 1.0;

     // stop autorotate after the first interaction
     controls.addEventListener('start', () => {
         clearTimeout(autorotateTimeout);
         controls.autoRotate = false;
     });
     // restart autorotate after the last interaction & an idle time has passed
     controls.addEventListener('end', () => {
         autorotateTimeout = setTimeout(() => {
             controls.autoRotate = true;
         }, 120000);
     });

     //lighting
     light = new THREE.AmbientLight(0x404040);
     scene.add(light);
     renderer.render(scene, camera);
     window.addEventListener("resize", onWindowResize, false);
     window.addEventListener("beforeunload", detach);
     animate();
     println("initialized");
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


     //process fight markers
     let newFights = [];
     fights.forEach((f) => {
         let s = f.scale.x;
         if (s < 1) {
             scene.remove(f);
         } else {
             f.scale.set(s - (1 / fightLength), s - (1 / fightLength), s - (1 / fightLength));
             newFights.push(f);
         }
     });
     fights = newFights;


     //process burn markers
     let newBurns = [];
     burns.forEach((b) => {
         let s = b.scale.x;
         if (s < 1) {
             scene.remove(b);
         } else {

             b.position.x -= b.scale.x * Math.cos(-b.rotation.y);
             b.position.z -= b.scale.x * Math.sin(-b.rotation.y);

             let ratio = b.scale.y / b.scale.x;
             let newx = s - (1 / burnLength);
             b.scale.set(newx, newx * ratio, newx * ratio);

             b.position.x += b.scale.x * Math.cos(-b.rotation.y);
             b.position.z += b.scale.x * Math.sin(-b.rotation.y);
             newBurns.push(b);
         }
     });
     burns = newBurns;

     renderer.render(scene, camera);
     //println(" cam:("+camera.position.x+","+camera.position.y+","+camera.position.z+")");
 }

function drawOrbit(content) {
  let focusX = content.orbit.focus.x,
      focusY = content.orbit.focus.y,
      e = content.orbit.e,
      p = content.orbit.p / 5e4, //TODO: needs to get this value from the server. Could change in Constants
      rotation = content.orbit.rotation,
      id = content.index;
  let a = p / (1 - e * e);
  let b = a * Math.sqrt(1 - e * e);
  let cf = Math.sqrt(a * a - b * b);
  let focus = new THREE.Vector2(focusX, focusY);
  let offset = new THREE.Vector2(cf, 0).rotateAround(new THREE.Vector2(0, 0), rotation);
  let center = focus.sub(offset);

  let mesh = drawEllipse(center.x, center.y, a, b, -rotation);
  scene.add(mesh);


  if (id > 0) {
     // alien
     let al = aliens[id];
     if (al !== undefined) {
         if (al.orbit !== null) {
             scene.remove(al.orbit);
         }
         if (al.vector !== null) {
             scene.remove(al.vector);
         }
         if (vx !== undefined && vy !== undefined) {
             al.vector = drawVelocity(al, vx, vy);
         } else {
             al.vector = null;
         }
         al.orbit = mesh;
         scene.add(al.vector);
         scene.add(al.orbit);
     }
  } else {
     // planet
     let pl = planets[id];
     pl.orbit = mesh;
     scene.add(pl.orbit);
  }
}

 function drawEllipse(centerX, centerY, radiusX, radiusY, rotation) {
     let curve = new THREE.EllipseCurve(
             -centerY, -centerX,
             radiusY, radiusX,
             0, 2 * Math.PI,
             false,
             rotation
             );
     let points = curve.getPoints(200);
     let geometry = new THREE.BufferGeometry().setFromPoints(points);
     // Create the final object to add to the scene
     let ellipse = new THREE.Line(geometry, orbitMaterial);
     ellipse.rotation.x = Math.PI / 2;
     ellipse.position.y = 1.0;
     return ellipse;
 }

 function drawVelocity(alien, vx, vy) {
     return drawLine(-alien.mesh.position.z, -alien.mesh.position.x,
             -alien.mesh.position.z + vx * vMultiplier, -alien.mesh.position.x + vy * vMultiplier,
             fightMaterial);

 }

 function drawLine(x1, y1, x2, y2, material) {
     let vector = new THREE.Geometry();
     vector.vertices.push(
             new THREE.Vector3(-y1, 1, -x1),
             new THREE.Vector3(-y2, 1, -x2),
             );

     let line = new THREE.Line(vector, ((material !== undefined) ? material : orbitMaterial));
     return line;

 }

 class Trail {
     constructor() { this.points = []; }

     addPoint(x, y) {
         let mesh = new THREE.Mesh(starGeo, trailMaterial);
         mesh.scale.set(0.2, 0.2, 0.2);
         mesh.position.x = -y;
         mesh.position.z = -x;
         mesh.position.y = 1.0;
         scene.add(mesh);
         this.points.push(mesh);
         if (this.points.length > trailLength) {
             scene.remove(this.points.shift());
         }
     }

     delete() {
         for (let i = 0; i < this.points.length; i++) {
             scene.remove(this.points[i]);
         }
     }

 }


 function showFight(x, y) {
     let mesh = new THREE.Mesh(planetGeo, fightMaterial);
     mesh.position.x = -y;
     mesh.position.z = -x;
     mesh.position.y = 1;
     mesh.scale.set(5, 5, 5);


     //add mesh to fight list
     scene.add(mesh);
     fights.push(mesh);

     // no need to show more than 100 fights/burns. If there are old ones in here, delete them.
     while (fights.length > 100) {
         scene.remove(fights.shift());
     }
 }

 function showBurn(id, x, y, dvx, dvy) {
     let mesh = new THREE.Mesh(sphereGeo, fightMaterial);
     dvx *= -aMultiplier;
     dvy *= -aMultiplier;

     // from here task is to draw an ellipsoid of linear length with (dvx,dvy),
     // from x, y on the grid in the direction pointed to by dvx, dvy


     let size = Math.sqrt(dvx * dvx + dvy * dvy);

     mesh.scale.set(aSize * size, aSize, aSize);
     mesh.rotation.y = -Math.atan2(-dvx, -dvy);
     //println("rot: " + mesh.rotation.y);


     mesh.position.x = -(y - mesh.scale.x * Math.cos(-mesh.rotation.y));
     mesh.position.z = -(x - mesh.scale.x * Math.sin(-mesh.rotation.y));
     mesh.position.y = 1;

     println("Burn, grid size: " + size + " at " + x + "," + y + "");


     //add mesh to burn list
     scene.add(mesh);
     burns.push(mesh);

     // no need to show more than 100 fights/burns. If there are old ones in here, delete them.
     while (burns.length > 100) {
         scene.remove(burns.shift());
     }
 }
