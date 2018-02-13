var scene = new THREE.Scene();
var camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000 );
var renderer = new THREE.WebGLRenderer();
renderer.setSize(window.innerWidth, window.innerHeight);
document.body.appendChild(renderer.domElement);

var cubeGeo = new THREE.BoxGeometry(0.9, 0.9, 0.9);

var size = 501;
var divisions = 501;

var gridHelper = new THREE.GridHelper(size, divisions);
scene.add(gridHelper);

camera.position.z = 100;
camera.position.y = 100;
camera.rotation.x = -Math.PI/4;

var light = new THREE.AmbientLight(0x404040);
scene.add(light);

document.addEventListener('keyup', keyUp, false);
document.addEventListener('keydown', keyDown, false);
function keyUp(event) {
  key[event.which || event.keyCode] = false;
}
function keyDown(event) {
  key[event.which || event.keyCode] = true;
//console.log(event.which);
}
var rotation = 0;
var key = {
check: () => {
  //put things that happen while keys are pushed here
}
}


const alien  = class{
  constructor(c,x,z){
    this.mat = new THREE.MeshBasicMaterial({color:c, wireframe:false});
    this.mesh = new THREE.Mesh(cubeGeo,this.mat);
    scene.add(this.mesh);
    this.mesh.position.x = x;
    this.mesh.position.z = z;
    this.mesh.position.y = 0.5;
  }
  move(x,z){
    this.mesh.position.x += x;
    this.mesh.position.z += z;
  }
  kill(){
    scene.remove(this.mesh);
  }
};




function tick(){
  /*cubeMesh.position.x += 0.01;
  cubeMesh.position.z += 0.01;*/
  key.check();


  //camera.rotation.y = 0;
  /*THE CAMERA CURRENTLY DOESNT WORK
  camera.position.z = 5*Math.cos((rotation*Math.PI)/180);
  camera.position.x = 5*Math.sin((rotation*Math.PI)/180);
  camera.rotation.x = 0;
 camera.rotation.y = (rotation*Math.PI)/180;
 camera.rotation.x = -Math.PI/4;
*/
  //this needs to be fixed
/*  camera.rotation.x = (rotation*Math.PI)/180;
  camera.rotation.z = -(rotation*Math.PI)/180;*/
  //console.log('tick');
  renderer.render(scene,camera);
}
var interval = setInterval(tick,30);
