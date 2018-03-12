var key = {
	down: () => {key[event.keyCode || event.which] = true},
	up: () => {key[event.keyCode || event.which] = false}
};
document.addEventListener('keydown', key.down, false);
document.addEventListener('keyup', key.up, false);

var curser = {
	x: 0,
	y: 0,
	dif: {x: 0, y: 0},
	clicked: false,
	attached: false,

	down: () => {
		this.clicked = true;
		this.x = event.clientX;
		this.y = event.clientY;

		inputHandler.mousedown();
	},
	up: () => {
		this.clicked = false;
		inputHandler.mouseup();
	},
	move: () => {
		if(this.clicked) {
			curser.dif.x = event.clientX - this.x;
			curser.dif.y = this.y - event.clientY;

			inputHandler.mousemove(curser.dif.x,curser.dif.y);

			this.x = event.clientX;
			this.y = event.clientY;
		}
	},
	wheel: () => {inputHandler.scroll(event.wheelDelta / -120)}
};

document.addEventListener('mousemove', curser.move, false);
document.addEventListener('wheel', curser.wheel, false);
document.addEventListener('mousedown', curser.down, false);
document.addEventListener('mouseup', curser.up, false);
