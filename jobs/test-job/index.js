var fs = require('fs');
var type = process.argv[3];

if (type === 'pre') {
  fs.appendFile('log', "pre\n", function() {
    setTimeout(function() {
      process.exit(1);
    }, 2000);
  });
} else if (type === 'post') {
  fs.appendFile('log', "post\n", function() {
    process.exit(0);
  });
} else {
  fs.appendFile('log', "MAIN PROCESS\n", function() {
    process.exit(0);
  });
}

