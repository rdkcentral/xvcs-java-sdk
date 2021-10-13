The following guide has the steps to execute a successful request against the Comcast server.

### Configuration

Configuration is mandatory and must be the very first step in your application. You must provide a `speech-config.yml` and place it under your resources directory.
Here is the sample configuration file.
```
connection:
  websocket:
    url: wss://vrex-sandbox-comcast.vrexcore.net/vrex/speech/websocket #REQUIRED
    #url: ws://localhost:8082/vrex/speech/websocket #REQUIRED
    version: 1 #optional - default: 1

appId: 12345678 #REQUIRED
deviceId: 12345 #Optional, but one must be provided either here or in code
accountId: 12345 #Optional, but one must be provided either here or in code
#customerId: 12345 #optional

auth:
  enabled: true #optional - default: true
  renewInterval: 720 #REQUIRED (in minutes)

logging:
  enabled: true #optional - default: true
  logLevel: DEBUG #optional - default: INFO
```

Note that at minimum you should include the properties marked as (REQUIRED). The rest of the options have default values which can be omitted if they do not need to be overridden.

***

### Authentication
Authentication is required to use the SDK. And the client must implement an authenticator by using the provided auth interfaces provide the configuration values.

First update `speech-config.yml` to include auth.renewInterval value. (as seen in the above code snippet).
Then add a `speech-secrets.properties` in your resources directory and include the endpoint, clientId and secret values.

```
auth.clientId=MY_CLIENT_ID
auth.secret=MY_CLIENT_SECRET
auth.endpoint=https://EXAMPLE_END_POINT.com/oauth/token
```

There are 2 Auth interfaces relevant to the client. `SpeechAuthenticator` and `AuthResponse`

The client should create a class which implements `SpeechAuthenticator` and the following abstract methods:
1. `fetchInitialToken()` - connect and fetch a token synchronously and update the `authResponse` object provided in the interface. Note that you can find the endpoint, clientId and secret in the provided `authConfig` object.
2. `scheduleTokenFetching()` - Since tokens expire, you should provide a scheduling mechanism which fetches tokens and updates `authResponse` field. An en example, this method could reuse `fetchInitialToken()` method using a `ScheduledExecutorService` to run it periodically in a separate thread based on the `renewInterval` value found in `authConfig`.
3. `getAuthResponse()` - returns the authResponse object. Use locks in this method and `scheduleTokenFetching()` to ensure proper read, write behavior.

***

### Initializing the application

Once above configuration and authentication implementation is complete, you can initialize the application as seen below.

```
        SpeechConfigurationManager configManager = SpeechApplication
                .newApplication(THE_CALLING_CLASS.class)
                .withAuthenticator(authenticator)
                .init();
```

The above code assumes that your `speech-config.yml` and `speech-secrets.properties` files exist under the root of `resources` directory. If you would like to specify a different directory and a file name for either, you may do so by calling additional customization methods as seen below.

```
        SpeechConfigurationManager configManager = SpeechApplication
                .newApplication(StreamFromFileDemo.class)
                .withAuthenticator(authenticator)
                .withConfigFile("subdir/myconfig.yml")
                .withSecretsFile("subdir/subdir/secrets.properties")
                .init();
```
The once `init()` is called a `SpeechConfigurationManager` object is returned, which can be passed on to a speech session.
***

### Generate a unique TRX value
A TRX is a unique string value used to identify a single request.
As a example you may use `SpeechUtils.generateTrx()` to generate a random UUID trx.

***

### Creating and customizing the init message

When we eventually connect to the server in an upcoming step, we will be sending an `init` message as the very first message to the server. In this step we will be generating an init message which can generated from a json-message or configured programatically. (or a combination of both)
Init message creation is initiated by calling factory methods available in the `InitPayloadBuilder` class. This class returns an instance of the same type so you can use chaining to fully customize the init message to your liking. Once customization is complete, call `buildMessage` method to retrieve the payload.

- **Json option** - You may create an init payload from json file by calling `InitPayloadBuilder.fromInitJson(JsonNode)` method.

- **Programmatic option** - By calling `InitPayloadBuilder.fromDefaultInitPayload()`, you get a customizable instance of `InitPayloadBuilder` which is preconfigured with a minimal default payload provided by Comcast.

- **Other options** - You may call `InitPayloadBuilder.customizeExistingPayload(InitPayload)` to customize an existing instance of `InitPayload` or `InitPayloadBuilder.copyFromExistingPayload(InitPayload)` to make a deep copy of an existing payload.

Note that message sending and receiving is handled by the library. As a developer, you should only be concerned with customizing and providing these messages to speech sessions.
***

### Create and start a websocket session.

The code below demonstrates how to fully configure and start a websocket session:
```
        SpeechSession
                .newSession(trx, speechConfigManager)
                .withInit(initPayload)
                .withAudio(AudioOption.from(audioInputStream))
                .withResultObserver(observer)
                .startSession();
```

- `SpeechSession.newSession(trx)` is a factory method which returns a configurable instance of the same type.
- Use `withInit` (required) and `withContext` (optional) to configure the session with the relevant messages.
- `.withAudio()` method accepts a `AudioOption` Object which can be initialized with either an `AudioInputStream` or a `PipedInputStream`- -
    - `AudioInputStream` is suitable if you would like to send an existing audio file for processing.
    - `PipedInputStream` is suitable for real time audio streaming to the server. (From a device such as a mic)
        - You should create a `PipedOutputStream` and connect it to the `PipedInputStream` prior to writing to the outputStream in a separate thread.
        - Example:
```
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        // create a thread that writes to the pipedOutputStream
        Runnable r = () -> {//user Implements}
        executor.submit(r);

        SpeechResultObserver observer = new MyResultObserver();

        SpeechSession
                .newSession(trx)
                .withInit(initPayload)
                .withContext(contextMessage)
                .withAudio(AudioOption.from(pipedInputStream))
                .withResultObserver(observer)
                .startSession();
```
- `startSession()` method a starts the websocket session. Session configuration must be finished prior to calling startSession.

***

### Result Handling
This is achieved by passing in an object which implements `SpeechResultObserver` interface. (See above code snippet) The overridden methods get called as soon as results are retrieved from the server. This is not a required step, and you can omit result handling altogether. However, you most likely want to do perform custom operations on the results in addition to seeing them in the logs.

***
