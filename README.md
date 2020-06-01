# Association Service
## General Description 
This service is responsible to creating and providing relationships between "elements".  The exact nature of what each element
represents is intentionally left generic in order to allow this service to back data organization for potentially many clients.
The current api is contained in the [protobuf file](src/main/protobuf/associations/association_service.proto)
### API
- Create Element: make a new element (TODO: perhaps extend request with map<string, string> for fully generic properties)
- Get Elements: Query based off id or list of properties and receive 0-n elements(TODO: add pagination)
- Set Association: Associate two elements with one another and label association.  This will create elements as well if the 
requested elements do not exist
- Get Associations: Query associations by selecting a root element and what associations you want to see, returns a paginated 
response of Associations

## Stream Overview
TODO: set up framework for streaming out events describing events which occurred and listen for events describing changes from outside the system.
This will be a bit more theoretical because there is no outside system :)
