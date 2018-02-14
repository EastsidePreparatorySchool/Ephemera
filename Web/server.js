const express = require('express');
var app = express();
app.use(express.static(__dirname+'/public'));
const port = 3000;

//example app.get:
app.get('/example', (req, res)=>{
  // you can access things like cookies with the req object, use res.send() or res.sendFile() to send a response
  res.send("<!DOCTYPE html><html><body>this is an example</body></html>");
  //thats if you want /example to be an accessible page, you could also send plaintext or a json, which could be used by the client
});

app.get('/getAil',(req,res)=>{
  res.send('completed');
});

app.listen(port, (err) => {
  if (err) return console.log('error', err);

  console.log('listening on port ' + port);
});
