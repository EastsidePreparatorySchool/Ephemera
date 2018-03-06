//set up express
const express = require('express');
var app = express();
app.use(express.static(__dirname+'/public'));
const port = 3000;
var cookieParser = require('cookie-parser')
app.use(cookieParser())

//example app.get:
app.get('/example', (req, res)=>{
  // you can access things like cookies with the req object, use res.send() or res.sendFile() to send a response
  res.send("<!DOCTYPE html><html><body>this is an example</body></html>");
  //thats if you want /example to be an accessible page, you could also send plaintext or a json, which could be used by the client
});

const Update = class{
  constructor(type, data){
      this.type = type;
      this.data = data;
  }
}
updates = [];

//alien class, almost identical to client side
const Alien = class{
  constructor(c,x,y,id){
    this.location = {};
    this.location.x = x;
    this.location.y = y;
    this.id = id;
    this.color = c;
    updates.push(new Update("new", this));
  }
  move(x,y){
    this.location = {};
    this.location.x = x;
    this.location.y = y;
    updates.push(new Update("move", {"id":this.id, "x":x, "y":y}));
  }
  kill(){
    updates.push(new Update("kill", {"id":this.id}));
    delete aliens[this.id];
  }
}

//array to contain current aliens
aliens = [];

//gives initial 10 aliens to array for testing
for(i=0; i<10; i++){
  aliens.push(new Alien(0xFF0000,1,i,i));
}

//handles request for all current aliens
app.get('/getcurrent',(req,res)=>{
 res.send(aliens);
});

//handles requests from updates from last tick
app.get('/getupdates', (req,res)=>{
  res.send(updates);
});
var cookies;
app.get('/manage',(req,res)=>{
  cookies = req.cookies;
  if(cookies.password == "ephemeralMein"){
    res.sendFile(__dirname +'/management.html');
  }
  else{
    res.send(`
      <!DOCTYPE html><html><head><script>
        pass = prompt('You are not logged in, please enter the password');
        document.cookie = "password = " + pass;
        location.reload();
      </script></head></html>
  `);
  }
});

//handles request from the manage page to add a new alien
app.get('/testnewail',(req,res)=>{
  cookies = req.cookies;
  if(cookies.password == "ephemeralMein"){
    aliens.push(new Alien(0xFF0000,1,Math.floor(Math.random()*51),aliens.length));
  }
  res.send('did');
  console.log('should do something');
  console.log(aliens);
});

//resets update array variable
function update(){
  updates = [];
}

//start tick and begin listening for requests
var interval = setInterval(update, 600);
app.listen(port, (err) => {
  if (err) return console.log('error', err);

  console.log('listening on port ' + port);
});
