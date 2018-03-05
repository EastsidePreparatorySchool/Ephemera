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
//array to contain current aliens
aliens = [];

//handles request for all current aliens
app.get('/getcurrent',(req,res)=>{

});

//handles requests from updates from last tick
app.get('/getupdates', (req,res)=>{

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

function update(){
  //this is where stuff to up`date aliens will go
}

//start tick and begin listening for requests
var interval = setInterval(update, 3000);
app.listen(port, (err) => {
  if (err) return console.log('error', err);

  console.log('listening on port ' + port);
});
