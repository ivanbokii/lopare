var fs = require('fs');
var type = process.argv[3];

if (type !== 'pre' && type !== 'post'){
  fs.appendFile('./log', new Date().toString() + "\n", function() {
    setTimeout(function() {
      process.exit(0);
    }, 1000);
  });
}

// process.exit(0);

