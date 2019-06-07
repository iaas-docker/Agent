# Agent module

This is the agent module in charge of handling the docker instances.

## Installation

### Fast lane:
Download the agent from [github release](https://github.com/iaas-docker/Agent/releases/download/1.0/agent-1.0-SNAPSHOT.zip)
Unzip the folder
Define an .env file containing:
```bash
REGISTRY_FQDN = //FQDN in which the registry is hosted.
EMPTY_IMAGE_NAME = //Created while starting the server module, should be named empty-image
QUEUE_URL = //URL of the FIFO queue assigned to this physical machine, must create it beforehand.
AWS_ACCESS_KEY = //AWS access key
AWS_ACCESS_SECRET = //AWS secret 
MORGO_URL = //Same MONGO URL used on the server and the worker.
```
Run ./bin/agent

### Manual lane:
Download [Gradle@>5.3.1](https://gradle.org/install/)

Clone this repo

Define a .env just like the one specified above. 

Run:
```bash
gradle run
```


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)