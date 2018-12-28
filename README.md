# Data4Health - Android Client

This repository contains the Android Studio project of our client for Data4Health. As we formally specified in our RASD document, we 
saw Data4Help, Track4Run and AutomatedSOS and thought that they would fit quite well in a virtual scenario where TrackMe was offering a 
vast suite of services, with these 3 being just some of them; we called the project Data4Health and proceeded to build it as an
Android App that would have contained all the services.

This here project contains two modules: the smartphone app, and the Wear OS app (which runs on wearable devices)

## Getting Started

You can start by importing this project in your Android Studio IDE. You can download Android Studio and the Android SDK from here: https://developer.android.com/studio/

Keep in mind that the download and installation could take you a while. Once you're in Android Studio, before running the app make sure you
have a server running from our Flask server project. At the moment, the app is configured to connect to a remote server we are hosting online. You need to make sure that the two strings: "WEBSERVICE_IP" and "WEBSERVICE_PORT" in
the "Endpoints" class found in the "frassonlancellottilodi.data4health.utils" package are right before running the app. We suggest 
running the server with the
```
--host=<<your computer's IP>>
```
option to easily find your server on your local network, especially if you are debugging via a real Android device.
If you are running the server on localhost and you are using an emulator as your device, you can change the "WEBSERVICE_IP" value to:
```
public static final String WEBSERVICE_IP = "http://10.0.2.2";

```
This will tell your emulator to connect to your localhost. You can find more information about this online.

### Setting up an emulator

To test the app, you need to run it on an Android Device. The app SHOULD be compatible with anything running android 5.0 or above, but 
we really only tested on Android 9.0 devices. You can debug on a real device by enabling developer options and USB debugging on your 
phone, or by setting up an emulator. We really suggest you to use the former option, as running an emulator can be quite an expensive task 
for your computer. 

If you do wish to do so, make sure to have Virtualization enabled and to have AT LEAST 8GB of RAM, but 16GB would be better.
Then, create a virtual device with the AVD manager: we suggest using an API 28 x86 Image without Play Store support (which for some reason
precludes you from fully enabling hardware acceleration). Use the recommended options for the other settings. 

If you do need to launch the Wear OS module, you need to do it on a real smartwatch. We know this is not ideal, but there are two problems
with emulating Wear OS devices:

A) Emulated watches can't be paired with emulated smartphones (you need to pair a real phone with a virtual watch, and this does allow you to
launch the app if you wish, it will just lose most functionality)

B) Emulated watches (at least those available through the basic AVD manager) do not support the virtual sensors needed to actually work
with the phone app. You can still technically access a virtual accelerometer, but no heartrate and step sensors are available.

### You're good to go!

At this point you're ready to launch the app and use Data4Health on your devices. 

Note that due to the quick implementation, 
if you want to test the watch app you need to have the smartphone app open on the "HomeActivity" screen before launching the 
wearable module on the watch. This is due to problems with the synchronization of the Data Layer that could be fixed but require
some extra work. We also didn't account for network instabilities and server unreachability, so any network error will have undesired
effects on the system.

## Running the tests

We included a quick UI test in our project. UI tests are in the "androidTest" directory, while Unit Tests need to be put in the
"test" folder. We didn't implement much in the way of tests because our client is heavily reliant on UI code and there's little to no
code that would require a separate Unit Test. Most tests would have to be UI tests and would need to take Android code and
network interactions into account. 

Still, the UI test you'll find in the androidTest directory was made using Espresso (more on that here: https://developer.android.com/training/testing/espresso/)
which allows for in depth testing of the Android UI. Espresso does have an automatic test recording and generation feature, but
in our experience it didn't work at all and refused to launch properly, instead freezing after the first few touches on the test device.

Due to time constraints, we tested most of our systems manually and have not implemented a complete suite of automatic tests at this point in time.

## Final notes

We don't expect everyone to be familiar with the Android SDK, so we'll try to be always reachable during the Testing phase of the course
to provide explanations and support with running our project and understanding the basics. Feel free to contact us if needed.

## Author 

Jacopo Frasson

