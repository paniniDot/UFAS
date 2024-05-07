#include <WiFi.h>
#include <PubSubClient.h>
#include "src/Pir.h"
#include "src/PhotoResistor.h"
#include "src/Light.h"
#include "src/Roll.h"
#include "src/MqttManager.h"

#define BUFFER_SIZE 2048
// Pin Definitions
#define PIR_PIN 25
#define PHOTO_RESISTOR_PIN 27
#define LIGHT_PIN 32  // tbd
#define ROLL_PIN 33   // tbd

// WiFi Configuration
const char* ssid = "Fast$wag";
const char* password = "SwAg2k24";

// MQTT Configuration
const char* mqtt_server = "192.168.1.67";
const int mqtt_port = 1883;

// MQTT Topics
const char* topic_light = "room1/light";
const char* topic_roll = "room1/roll";
const char* topic_receive = "mqttserver.to.mqttclient";

// Notification Configuration
unsigned long lastNotifyTime = 0;
const unsigned long notifyInterval = 1000;

// WiFi Client
WiFiClient espClient;
PubSubClient mqttClient(espClient);

// MqttManager and Hardware Objects
MqttManager* mqttManager;

PhotoResistor* resistor;
Pir* pir;
Light* light;
Roll* roll;

void connectToWIFI() {
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
    if (!mqttClient.connect("room2")) {
      delay(500);
    }
  }
  Serial.println("Connected to MQTT server");
  mqttClient.subscribe(topic_receive);
}

void messageReceivedCallback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message received from server: ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

void setup() {
  Serial.begin(19200);
  connectToWIFI();
  mqttClient.setServer(mqtt_server, mqtt_port);
  mqttClient.setBufferSize(BUFFER_SIZE);
  mqttClient.setCallback(messageReceivedCallback);
  connectToMQTT();

  mqttManager = new MqttManager(&mqttClient, BUFFER_SIZE);
  mqttManager->addPublisher(topic_light, "room1/light");
  mqttManager->addPublisher(topic_roll, "room1/roll");

  pir = new Pir(PIR_PIN);
  resistor = new PhotoResistor(PHOTO_RESISTOR_PIN);
  light = new Light(LIGHT_PIN);
  roll = new Roll(ROLL_PIN);
  pir->attach(light);
  pir->attach(roll);
  resistor->attach(light);
  resistor->attach(roll);
  light->attach(mqttManager);
  roll->attach(mqttManager);
}

void loop() {
  if (!mqttClient.connected()) {
    Serial.println("MQTT server connection lost. Reconnecting...");
    connectToMQTT();
  }
  mqttClient.loop();

  unsigned long currentTime = millis();
  if (currentTime - lastNotifyTime >= notifyInterval) {
    light->notify();
    roll->notify();
    lastNotifyTime = currentTime;
  }
}
