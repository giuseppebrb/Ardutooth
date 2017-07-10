/**
 * This is a basic Arduino sketch to fetch data sent
 * from an Android application using Ardutooth library.
 * Learn more: https://github.com/giuseppebrb/Ardutooth
 * 
 * @author Giuseppe Barbato
 */

#include <SoftwareSerial.h>

SoftwareSerial mySerial(0, 1); // RX, TX
String inputData;
const int pinLED = 13;

void setup() {
  pinMode(pinLED,OUTPUT); // There's a LED that turn on if there are input data
  Serial.begin(9600);
  mySerial.begin(9600);
}

void loop() { // run over and over
  mySerial.listen();
  if (mySerial.available()){ 
    digitalWrite(pinLED,HIGH);
    while(mySerial.available()){
      delay(10);
      char recieved = (char) mySerial.read();
      inputData += recieved;
      if(recieved == '\n'){
        Serial.print(inputData);
        inputData = "";
        digitalWrite(pinLED, LOW);
        }      
    }
  }
}
