# BigJobs
Jobs triggering library Very simple Jobs Orchestrating API.

The project purpose is to manage easily different type of jobs by defining triggers in a very simple way. The need arose by trying to launch two bigquery jobs (GCP) one after the other.

````
bigJobs
  .on(Events.onDone(Jobs.withAttribute("type","example")))
  .then( (evt) -> {
  createCopyJob(bigJobs,projectId);
  } )
  .build();
````



**At the moment the project is a POC and a work in progress.**

**The use of this library is not recommended at this stage of development.**



![Image BigJobs](logo.png)

