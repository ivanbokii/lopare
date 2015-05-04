var fs = require('fs');
var type = process.argv[3];

if (type === 'pre') {
  fs.appendFile('./log', "pre\n", function() {
    setTimeout(function() {
      process.exit(0);
    }, 1000);
  });
}

if (type === 'post') {
  fs.appendFile('./log', "post\n", function() {
    process.exit(0);
  });
}

if (type !== 'pre' && type !== 'post'){
  fs.appendFile('./log', "MAIN PROCESS!!\n", function() {
    setTimeout(function() {
      process.exit(0);
    }, 1000);
  });
}

