const express = require('express');
app = express();
app.use(express.static(__dirname+'/public'));
const port = 3000;

app.listen(port, (err) => {
  if (err) return console.log('error', err);

  console.log('listening on port ' + port);
});
