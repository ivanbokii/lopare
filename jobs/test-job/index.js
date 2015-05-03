var fs = require('fs');
var type = process.argv[3];

if (type === 'pre') {
  fs.appendFile('./superlog', "pre\n", function() {
    setTimeout(function() {
      process.exit(0);
    }, 2000);
  });
} else if (type === 'post') {
  fs.appendFile('./superlog', "post\n", function() {
    process.exit(0);
  });
} else {
  fs.appendFile('./superlog', "MAIN PROCESS\n", function() {
    process.exit(0);
  });
}

