# Lopare

Lopare is a job runner like crontab but with a support of retries. Written in Clojure.

###Build
To build lopare you need to have [lein](http://leiningen.org) installed in your system.
Run make file in the root of the project and there should be a build directory after build
finishes.

###How to use
To use lopare you need to update `jobs.json` file. There is one test job already in that file
```json
{
  "jobs": [
    {
      "name": "test-job",
      "exec": "node",
      "entry": "index.js",
      "arg": null,
      "retries": 1,
      "schedule": "/5 * * * * * *"
    }
  ]
}
```
This job runs an index.js with node.js every 5 seconds and if it fails, tries to run it one more time. You can also pass any arguments to your jobs using the `arg` property of the job.

All your jobs should go to the `jobs` folder inside lopare directory.
Name of you job's folder should be the same as the name you put into the `jobs.json` file. In the example above it's `test-job`

Lopare saves information about jobs' runs into the `last-runs` folder.

To run lopare type `java -jar lopare.jar`

###Tests
To run tests, navigate to the project's root directory and run `lein midje`

###License
MIT
