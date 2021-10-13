# xvcs-java-sdk
This SDK is client side code that implements that spec of the Xfinity Voice Cloud Services (XVCS) API developed by Voice Relevance Engine for Xfinity (VREX).

This is the XVCS JAVA SDK. It provides a JAVA SDK and example appliation for using the Xfinity Voice Cloud Services (XVCS) API. The developer Guide can be found in DEVELOPER_GUIDE.md

### Useful links:
- XVCS API documentation: https://wiki.rdkcentral.com/display/RDK/Xfinity+Voice+Cloud+Services+-+XVCS

### Modules
* sdk
* auth - has authetication related interfaces (sat module is an implementation)
* sat - implemetation of SAT authentication (used by demo)
* demo 
* test-shared - This shared module has resources shared by sdk tests and demo

### Prereqs

* vrex-backend
    * Running locally or Accessible Endpoint
* vrex-java-sdk
    * Install the module
* Java 1.8 or greater
* Maven

### Package and run the Demo

1. Download the code
   ```
   git clone git@github.com:rdkcentral/xvcs-java-sdk.git
   ```

2. Configure the SDK to use your SAT token secret (Required)
   1. Create a new file named `speech-secrets.properties`  under `vrex-java-sdk/test-shared/src/main/resources/`

   2. add the following entries to the file:
        ```
        auth.secret=ADD_YOUR_CLIENT_ID
        auth.endpoint=https://sat-prod.codebig2.net/oauth/token
        auth.secret=ADD_YOUR_SECRET
        ```
        
NOTE: Steps 3 and 4 can run easily by executing `build-run-demo.sh`. (Run`chmod +x build-run-demo.sh` to make the file executable.)
If you just need to run the demo without building and packaging, you can run `run-demo.sh`.

3. Build
   ```
   mvn clean install -Dmaven.test.skip=true
   cd demo
   mvn clean package
   ```

4. Run
   ```
   java -jar target/sdk-demo-1.0.0-RC1.jar 
   ```
### License
`Apache License - Version 2.0`

Please see LICENSE file under `sdk` module
