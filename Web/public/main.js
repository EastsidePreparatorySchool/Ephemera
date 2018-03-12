var scene = new THREE.Scene();
var camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000 );

var renderer = new THREE.WebGLRenderer();
renderer.setSize( window.innerWidth, window.innerHeight );
document.body.appendChild( renderer.domElement );



var cubeGeo = new THREE.BoxGeometry( 1, 1, 1 );
var silvMat = new THREE.MeshBasicMaterial({color:0xC0C0C0, wireframe:true});
var cubeMesh = new THREE.Mesh(cubeGeo,silvMat);
scene.add(cubeMesh);










function render() {
  requestAnimationFrame(render);

  inputHandler.keyCheck();
  viewer.set();

  renderer.render(scene, camera);
}
