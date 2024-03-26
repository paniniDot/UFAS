#include <WiFi.h>
#include <Adafruit_MQTT.h>
#include "Adafruit_MQTT_Client.h"
#include "src/Pir.h"
#include "src/PhotoResistor.h"
#include "src/Light.h"
#include "src/Roll.h"
#include "src/MqttManager.h"
#include "src/Camera.h"

// Pin Definitions
#define PIR_PIN 25
#define PHOTO_RESISTOR_PIN 27
#define LIGHT_PIN 32  // tbd
#define ROLL_PIN 33   // tbd

// WiFi Configuration
const char* ssid = "Fast$wag";
const char* password = "SwAg2k24";

// MQTT Configuration
const char* mqtt_server = "192.168.1.64";
const int mqtt_port = 1883;

// MQTT Topics
const char* topic_light = "room1/light";
const char* topic_roll = "room1/roll";
const char* topic_cam = "room1/cam";
const char* topic_receive = "mqttserver.to.mqttclient";

// Notification Configuration
unsigned long lastNotifyTime = 0;
const unsigned long notifyInterval = 1000;

// MQTT Client and Publishers
WiFiClient espClient;
Adafruit_MQTT_Client mqttClient(&espClient, mqtt_server, mqtt_port);
Adafruit_MQTT_Publish publisher_light(&mqttClient, topic_light);
Adafruit_MQTT_Publish publisher_roll(&mqttClient, topic_roll);
Adafruit_MQTT_Publish publisher_cam(&mqttClient, topic_cam);
Adafruit_MQTT_Subscribe subscribe_mqtt_server(&mqttClient, topic_receive);

// MqttManager and Hardware Objects
MqttManager* mqttManager;

PhotoResistor* resistor;
Pir* pir;
Light* light;
Roll* roll;
//Camera* camera;

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

void messageReceived(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message received on topic '");
  Serial.print(topic);
  Serial.print("': ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

void setup() {
  Serial.begin(115200);
  connectToWIFI();
  connectToMQTT();
  mqttManager = new MqttManager(&mqttClient);
  mqttManager->addPublisher(topic_light, &publisher_light);
  mqttManager->addPublisher(topic_roll, &publisher_roll);
  mqttManager->addPublisher(topic_cam, &publisher_cam);
  /*
  pir = new Pir(PIR_PIN);
  resistor = new PhotoResistor(PHOTO_RESISTOR_PIN);
  light = new Light(LIGHT_PIN);
  roll = new Roll(ROLL_PIN);
  //camera = new Camera();
  pir->attach(light);
  pir->attach(roll);
  resistor->attach(light);
  resistor->attach(roll);
  light->attach(mqttManager);
  roll->attach(mqttManager);
*/
  subscribe_mqtt_server.setCallback(messageReceived);
  mqttClient.subscribe(&subscribe_mqtt_server);
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
  /*
  unsigned long currentTime = millis();
  if (currentTime - lastNotifyTime >= notifyInterval) {
    //light->notify();
    //roll->notify();
    //camera->notify();
    lastNotifyTime = currentTime;
  }*/
}
