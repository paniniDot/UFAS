#include <WiFi.h>
#include <PubSubClient.h>
#include "src/Pir.h"
#include "src/PhotoResistor.h"
#include "src/Light.h"
#include "src/Roll.h"
#include "src/Sprinkler.h"
#include "src/Co2.h"
#include "src/MqttManager.h"

#define BUFFER_SIZE 2048

// Pin Definitions
#define PIR_PIN 25
#define PHOTO_RESISTOR_PIN 27
#define LIGHT_PIN 32  // tbd
#define ROLL_PIN 33   // tbd
#define SPRINKLER_PIN 34  // tbd

// WiFi Configuration
#define SSID "asus"
#define PASSWORD "0123456789"

// MQTT Configuration
#define MQTT_SERVER "192.168.1.156"
#define MQTT_PORT 1883
#define MQTT_CLIENT_ID "room1/esp1"

// MQTT Topics
#define SEND_TOPIC "house/output/room1"
#define RECEIVE_TOPIC "house/input/room1"
#define RECEIVE_TOPICH "house/input/room1/#"

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
Sprinkler* sprinkler;
Co2* co2;

void connectToWiFi() {
  Serial.print("Connecting to ");
  Serial.println(SSID);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(SSID, PASSWORD);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println("\nWiFi connected. IP address: ");
  Serial.println(WiFi.localIP());
}

void connectToMQTT() {
  Serial.println("Connecting to MQTT server...");
  
  while (!mqttClient.connected()) {
    if (mqttClient.connect(MQTT_CLIENT_ID)) {
      Serial.println("Connected to MQTT server");
      mqttClient.subscribe(RECEIVE_TOPICH);
    } else {
      Serial.print(".");
      delay(500);
    }
  }
}

void messageReceivedCallback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message received from server: ");

  String message;
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  Serial.println(message);
  Serial.println(String(topic));
  Serial.println(String(RECEIVE_TOPIC));

  if (String(topic) == String(RECEIVE_TOPIC) + "/manual_light") {
    Event<int> e(EventSourceType::MANUAL_LIGHT, new int(message.toInt()));
    light->update(&e);
  } else if (String(topic) == String(RECEIVE_TOPIC) + "/light") {
    Event<int> e(EventSourceType::LIGHT, new int(message.toInt()));
    light->update(&e);
  } else if (String(topic) == String(RECEIVE_TOPIC) + "/manual_roll") {
    Event<int> e(EventSourceType::MANUAL_ROLL, new int(message.toInt()));
    roll->update(&e);
  } else if (String(topic) == String(RECEIVE_TOPIC) + "/roll") {
    Event<int> e(EventSourceType::ROLL, new int(message.toInt()));
    roll->update(&e);
  } else if (String(topic) == String(RECEIVE_TOPIC) + "/sprinkler") {
    Event<int> e(EventSourceType::SPRINKLER, new int(message.toInt()));
    sprinkler->update(&e);
  }
}


void setup() {
  Serial.begin(19200);

  connectToWiFi();

  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setBufferSize(BUFFER_SIZE);
  mqttClient.setCallback(messageReceivedCallback);

  connectToMQTT();

  mqttManager = new MqttManager(&mqttClient, BUFFER_SIZE, SEND_TOPIC);
  pir = new Pir(PIR_PIN);
  resistor = new PhotoResistor(PHOTO_RESISTOR_PIN);
  light = new Light(LIGHT_PIN);
  roll = new Roll(ROLL_PIN);
  sprinkler = new Sprinkler(SPRINKLER_PIN);
  co2 = new Co2();
  co2->begin();
  pir->attach(light);
  pir->attach(roll);
  resistor->attach(light);
  resistor->attach(roll);
  light->attach(mqttManager);
  roll->attach(mqttManager);
  co2->attach(mqttManager);
}

void loop() {
  if (!mqttClient.connected()) {
    Serial.println("MQTT server connection lost. Reconnecting...");
    connectToMQTT();
  }
  
  mqttClient.loop();

  unsigned long currentTime = millis();
  if (currentTime - lastNotifyTime >= notifyInterval) {
    pir->notify();
    resistor->notify();
    light->notify();
    roll->notify();
    co2->notify();
    lastNotifyTime = currentTime;
  }
}