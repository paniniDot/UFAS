#include <WiFi.h>
#include <Adafruit_MQTT.h>
#include "Adafruit_MQTT_Client.h"
#include <esp_system.h>
#include "src/Pir.h"
#include "src/PhotoResistor.h"
#include "src/Light.h"
#include "src/Roll.h"
#include "MqttManager.h"

#define PIR_PIN 34
#define PHOTO_RESISTOR_PIN 35
#define LIGHT_PIN 36 // tbd
#define ROLL_PIN 39 // tbd

/* wifi network info */
const char* ssid = "asus";
const char* password = "0123456789";
/* MQTT server address */
const char* mqtt_server = "192.168.2.2";
const int mqtt_port = 1883;
/* MQTT topics */
const char* topic_light = "room1/light";
const char* topic_roll = "room1/roll";

unsigned long lastNotifyTime = 0;
const unsigned long notifyInterval = 1000;

/* MQTT client management */
WiFiClient espClient;
Adafruit_MQTT_Client* mqttClient = new Adafruit_MQTT_Client(&espClient, mqtt_server, mqtt_port);

Adafruit_MQTT_Publish* publisher_light = new Adafruit_MQTT_Publish(mqttClient, topic_light);
Adafruit_MQTT_Publish* publisher_roll = new Adafruit_MQTT_Publish(mqttClient, topic_roll);

MqttManager* mqttManager = new MqttManager(mqttClient);
mqttManager->addPublisher(topic_light, publisher_light);
mqttManager->addPublisher(topic_roll, publisher_roll);

/* Hardware objects */
PhotoResistor* resistor;
Pir* pir;
Light* light;
Roll* roll;

void connectToWIFI() {
  delay(100);
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
  Serial.print("WiFi connected. IP address: ");
  Serial.println(WiFi.localIP());
}

void connectToMQTT() {
  Serial.println("Connecting to MQTT server");
  while (!mqttClient.connected()) {
    if (!mqttClient.connect()) {
      delay(500);
    }
  }
  Serial.println("connected");
}

void setup() {
  Serial.begin(115200);
  connectToWIFI();
  connectToMQTT();
  resistor = new PhotoResistor(PHOTO_RESISTOR_PIN);
  pir = new Pir(PIR_PIN);
  light = new Light(PHOTO_RESISTOR_PIN);
  roll = new Roll(ROLL_PIN);
  pir->attach(light);
  pir->attach(roll);
  resistor->attach(light);
  resistor->attach(roll);
  light->attach(mqttManager);
  roll->attach(mqttManager);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi connection lost. Reconnecting...");
    connectToWIFI();
  }
  if (!mqttClient.ping()) {
    Serial.println("MQTT server connection lost");
    connectToMQTT();
  }
  unsigned long currentTime = millis();
  if (currentTime - lastNotifyTime >= notifyInterval) {
    light->notify();
    roll->notify();
    lastNotifyTime = currentTime;
  }
}