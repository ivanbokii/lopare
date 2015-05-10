var fs = require('fs');
var type = process.argv[3];

if (type === 'pre') {
  fs.appendFile('./log', 'PRE STEP \n', function() {
    process.exit(0);
  });
}

if (type === 'post') {
  fs.appendFile('./log', 'POST STEP \n', function() {
    process.exit(0);
  });
}

if (type !== 'pre' && type !== 'post'){
  fs.appendFile('./log', new Date().toString() + "\n", function() {
    process.exit(Math.floor(Math.random() * 100) % 2);
  });
}

