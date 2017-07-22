# Ardutooth

[![Ardutooth](https://img.shields.io/badge/build-passing-brightgreen.svg?style=plastic)](https://github.com/giuseppebrb/Ardutooth) [![Version](https://img.shields.io/badge/version-v1.0.0-blue.svg?style=plastic)](https://github.com/giuseppebrb/Ardutooth) [![Download](https://img.shields.io/badge/download-release-brightgreen.svg?style=plastic)](https://github.com/giuseppebrb/Ardutooth/releases/tag/v1.0)

![Ardutooth Banner](https://image.ibb.co/b6KKua/Ardutooth.jpg)


Ardutooth is a lightweight Android Archive Library (.aar) that allows you to create easily a stable bluetooth connection with an Arduino board.
Once a connection has been established, you can fetch information about it or send data to the board (reading has not been implemented yet).

## How to use it

The first thing you need is to get an instance of ```Arduooth```, preferably insde the ```onCreate()``` callback, by the ```getInstance(activity)``` method:
``` Ardutooth mArdutooth = Ardutooth.getInstance(activity); ```
where the parameter represents the current ```activity```.

After that you can allows user to established a bluetooth connection with ```mArdutooth.setConnection();```.
This simply method will check if bluetooth is supported on the current device, if is it turned on and if it's already connected with another bluetooth device.
If bluetooth is supported but not turned on or disconnected a series of dialogs will guide user through the process.
![Bluetooth off](https://image.ibb.co/ifJeua/Screenshot_20170710_163010.png)
![Bluetooth not connected](https://image.ibb.co/c8jxfF/Screenshot_20170710_163026.png)

You can check if connection has successfully established using ```mArdutooth.isConnected(); ```

Once a stable connection has been established, you can send data to your Arduino throgh the Serial Monitor with these methods:
* ```mArdutooh.sendInt(value)```
* ```mArdutooh.sendLong(value)```
* ```mArdutooh.sendShort(value)```
* ```mArdutooh.sendFloat(value)```
* ```mArdutooh.sendDouble(value)```
* ```mArdutooh.sendBoolean(value)```
* ```mArdutooh.sendChar(value)```
* ```mArdutooh.sendString(value)```

The library offers also methods to retrieve information about Bluetooth module, output stream and socket.

To close communication, simply use ```mArdutooh.disconnect();```

## Arduino Side

You can find a very basic sketch for arduino to the path [/Arduino_Basic_Sketch/Basic_Sketch.ino](https://github.com/giuseppebrb/Ardutooth/blob/master/Arduino_Basic_Sketch/Basic_Sketch.ino). Obviously this is a basic demo but it shows you how arduino could read data through the Serial Monitor and could be a starting point for your own sketch.

## Final Note
You can download the latest ardutooth .aar file [in here](https://github.com/giuseppebrb/Ardutooth/releases)

If you don't know how add an .aar file to your Android Project take a look here: [Add your library as a dependency - Android Developer](https://developer.android.com/studio/projects/android-library.html#AddDependency)

If you want add/edit features, any improvements is really appreciated. Just fork and pull request.
