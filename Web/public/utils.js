let viewer = {
	focus: new THREE.Vector3(0,0,0),
	r_update: false,
	length: 3,
	z_rot: 0,
	y_rot: 0,
	XZLength: 0,
	r_pos: {x: 7, y: 7, z: 7},
	speed: 0.1,
	curser_speed: 0.007,
	zoom_speed: 0.1,
	zoom_d: 1,
	v_offset: 0.05,

	update: function() {
		if (this.z_rot > (Math.PI / 2) - this.v_offset) {this.z_rot = (Math.PI / 2) - this.v_offset}
		else if (this.z_rot < (Math.PI / -2) + this.v_offset) {this.z_rot = (Math.PI / -2) + this.v_offset}

		if (this.y_rot > Math.PI * 2) {this.y_rot -= Math.PI * 2}
		else if (this.y_rot <= 0) {this.y_rot += Math.PI * 2}

		this.r_pos.y = Math.sin(this.z_rot) * this.length;
		this.XZLength = Math.cos(this.z_rot) * this.length;
		this.r_pos.x = Math.sin(this.y_rot) * this.XZLength;
		this.r_pos.z = Math.cos(this.y_rot) * this.XZLength;

		this.r_update = false;
	},
	zoom: function(length) {

		this.zoom_d += length/50;
		this.length = Math.pow(15, this.zoom_d);

		if (this.length < 0.1) {this.length = 0.1}
		this.r_update = true;
	},
	shift: function(y,z) {
		this.y_rot += y;
		this.z_rot += z;
		this.r_update = true;
	},
	set: function() {
		if (this.r_update) this.update();
		camera.position.x = this.focus.x + this.r_pos.x;
		camera.position.y = this.focus.y + this.r_pos.y;
		camera.position.z = this.focus.z + this.r_pos.z;
		camera.lookAt(this.focus);
	}
};


let inputHandler = {
  keyCheck: function() {
		if (key[38]) {
			viewer.shift(0,0.1);
		} //up
		if (key[40]) {
			viewer.shift(0,-0.1);
		} //down
		if (key[37]) {
			viewer.shift(-0.1,0);
		} //left
		if (key[39]) {
			viewer.shift(0.1,0);
		} //right
		if (key[32]) {} //space
  },

  mouseup: function() {

  },
  mousedown: function() {

  },
  mousemove: function(x,y) {
		viewer.shift(x/-100,y/-100);
  },
  scroll: function(wheel) {
		viewer.zoom(wheel);
  }
};



function OriginLine(data) {
	this.geometry = new THREE.Geometry();
	this.material = new THREE.LineBasicMaterial({color: data.color});
	this.geometry.vertices.push(new THREE.Vector3(0,0,0));
	this.geometry.vertices.push(new THREE.Vector3(data.point[0], data.point[1], data.point[2]));
	this.mesh = new THREE.Line(this.geometry, this.material);
}







function r_set(t_obj, l_obj) {
	t_obj.rotation.x = l_obj.x;
	t_obj.rotation.y = l_obj.y;
	t_obj.rotation.z = l_obj.z;
}
function p_set(t_obj, l_obj) {
	t_obj.position.x = l_obj.x;
	t_obj.position.y = l_obj.y;
	t_obj.position.z = l_obj.z;
}

function rotateXYZ(position, r) {
	rotateZ(position, r.z);
	rotateY(position, r.y);
	rotateX(position, r.x);

	return position;
}
function rotateX(position, r) {
	let y = position.y,
			z = position.z;
	position.y = y*Math.cos(r) - z*Math.sin(r);
	position.z = z*Math.cos(r) + y*Math.sin(r);
}
function rotateY(position, r) {
	let z = position.z,
			x = position.x;
	position.z = z*Math.cos(r) - x*Math.sin(r);
	position.x = x*Math.cos(r) + z*Math.sin(r);
}
function rotateZ(position, r) {
	let x = position.x,
			y = position.y;
	position.x = x*Math.cos(r) - y*Math.sin(r);
	position.y = y*Math.cos(r) + x*Math.sin(r);
}
