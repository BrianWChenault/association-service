# Association Service
## General Description 
This service is responsible to creating and providing relationships between "elements".  The exact nature of what each element
represents is intentionally left generic in order to allow this service to back data organization for potentially many clients.
The current api is contained in the [protobuf file](src/main/protobuf/associations/association_service.proto)
### API
- Create Element: make a new element
- Get Elements: Query based off id or list of properties and receive 0-n elements(TODO: add pagination)
- Set Association: Associate two elements with one another and label association.  This will create elements as well if the 
requested elements do not exist
- Get Associations: Query associations by selecting a root element and what associations you want to see, returns a paginated 
response of Associations

## Stream Overview
TODO: set up framework for streaming out events describing events which occurred and listen for events describing changes from outside the system.
This will be a bit more theoretical because there is no outside system :)

## How to Run
In order to run unit tests you will need a neo4j server running locally.  I have provided a [script](docker_environment.sh) which 
will spin one up in your docker environment.  You will need to have docker [installed](https://docs.docker.com/get-docker/) 
on your machine for this to work.  Once the server is running `sbt test` will run the unit tests.  You can even view the results of the tests
and run neo4j queries by using the [web interface](http://localhost:7474/browser/) built into the neo4j image now running on your machine
### Next Step
I need to make sure akka http is working properly, and then write a script to help wrap grpc_cli so that you can make requests to the 
service via the command line.  Then I will need to write a script to publish events to a kafka topic and have the running service observe and
persist that information to the database.
