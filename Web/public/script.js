var scene = new THREE.Scene();
var camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000 );

var renderer = new THREE.WebGLRenderer();
renderer.setSize( window.innerWidth, window.innerHeight );
document.body.appendChild( renderer.domElement );
var cubeGeo = new THREE.BoxGeometry( 1, 1, 1 );
var silvMat = new THREE.MeshBasicMaterial({color:0xC0C0C0, wireframe:true});
var cubeMesh = new THREE.Mesh(cubeGeo,silvMat);
scene.add(cubeMesh);



const alien  = class{
  constructor(l,c,x,y){
    this.mat = new THREE.MeshBasicMaterial({color:c, wireframe:false});
    this.geo = new THREE.BoxGeometry( 1, 1, 1 );
    this.mesh = new THREE.Mesh(this.geo,this.mat);
    scene.add(this.mesh);
    this.mesh.posision.x = x;
    this.mesh.posision.y = y;
  }
  move(x,y){
    this.mesh.posision.x += x;
    this.mesh.posision.y += y;
  }
};



camera.position.z = 5;
camera.position.y = 5;
//camera.rotation.x = -Math.PI/4;
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
  if(key[65]) { //a
    cubeMesh.position.x -= 0.05;
    //console.log("left");
  }

  if(key[87]) { //w
    cubeMesh.position.z -= 0.05;
    //console.log("up");
  }

  if(key[68]) { //d
    cubeMesh.position.x += 0.05;
    //console.log("right");
  }

  if(key[83]) { //s
    cubeMesh.position.z += 0.05;
    //console.log("down");
  }
  if(key[69]){
    cubeMesh.position.y += 0.05;
  }
  if(key[81]){
    cubeMesh.position.y -= 0.05;
  }
  if(key[37]){
    rotation += 0.7;
    if(rotation>360){
      rotation = 0;
    }
  }
  if(key[39]){
    rotation -= 0.7;
    if(rotation<0){
      rotation = 360;
    }
  }
}
}
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
  renderer.render( scene, camera );
}
var interval = setInterval(tick,30);
