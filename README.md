    # AXA DIL TEX Drive Library

### Table of Contents

1. [Integration](#integration)
2. [Permissions](#Permission)
3. [TEX Services Configuration](#configuration)
4. [Recording trip](#recording-trip)
5. [Getting score](getting-score)
6. [Logging](#logging)


The AXA DIL Telematic Exchange library is distributed as a self-contained AAR that can be integrated in gradle.

## 1. Integration

The library is hosted on a private repository https://axadil.jfrog.io/axadil/

To use it in your project, you need to declare the maven url that points to the repository address 
along with the credentials inside project level build.gradle.

allprojects {
repositories {
            jcenter()
            maven {
                url 'https://axadil.jfrog.io/axadil/android-tex'
                credentials {
                    username = artifactory_username
                    password = artifactory_password
                }
            }
        }
    }  


artifactory_{username,password} must be set in $HOME/.gradle/gradle.properties.
Then, open build.gradle inside the module where you want to use the library and simply add a dependency:

dependencies {
implementation 'drive-sdk-android-next:sdk:3.0.4'
}



## 2. Permission
###### `Note :`  The following permissions are needed to use the library :

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
    <uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
## 3. TEX Services Configuration

Before using the TEX Drive SDK, a configuration object must be created. 
It defines configuration which scope is the whole SDK.

Once created, this configuration allows you to create a `TexService` object that contains or create instances of the different services of the SDK.

The `TexService` instance that is created should be stored in your `Application`
class, since only one instance of it must exist. 
The decoupling configuration / instance creation was specifically made to be sure that only one instance of `TexService` will be created.

Example of configuration creation:
```java

// Configuration
val appName = "YOUR_APP_NAME"
val clientId = 132454334
val config : TexConfig? = TexConfig.Builder(applicationContext,appName,clientId).enableTrackers().platformHost(Platform.PREPROD).build()
val service: TexService? = TexService.configure(config!!)
```

### 4. Recording trip
Once the tex service created a trip recorder can be retrieved.
```java
val tripRecorder : TripRecorder? = service?.getTripRecorder()

//Start collecting data
val startTime = Date().time
try{
val tripId : TripId? = tripRecorder?.startTracking(startTime)
}catch (e: PermissionException){
}

The tripId is the id of the current trip.

//Stopping data collection
val endTime = Date().time
try{
tripRecorder?.stopTracking(endTime)
}catch (e: PermissionException){
}
```


### 5. Getting score
At the end of the trip a score is calculated. The score can be retreived in two ways
1. Get a scoreRetreiver from service, subscribe to it and automatically get the score at the end of the trip.
```java
val scoreRetriever = service?.scoreRetriever()
scoreRetriever?.getScoreListener()?.subscribe {
it?.let { score ->
//Use the score
}
}
```
2. Get the score manually using a trip id.
* Get a score reteiver first 
```java
val scoreRetriever = service?.scoreRetriever()
```
* Listen up to score.
```java
scoreRetriever?.getScoreListener()?.subscribe {
it?.let { score ->
//Use the score
}
}
```
* Listen up to the ended trip and get the score.
```java
tripRecorder?.endedTripListener()?.subscribe {
scoreRetriever?.retrieveScore(tripId.value)
}
```
### 6. Logging
The library provides a stream of log tha can be enabled by subcribing to the log flux.
It will be up to the client to choose using or not the logging system. For performance issue you should not use it on release.
```java
service?.logStream()?.
subscribeOn(Schedulers.computation())?.subscribe{log->
//Use the log message
}
```
