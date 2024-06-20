#define CAMERA

#include <WiFi.h>
#include <PubSubClient.h>
#include <HardwareLib.h>

// Buffer size Definition
#define BUFFER_SIZE 2048

// WiFi Configuration
#define SSID "asus"
#define PASSWORD "0123456789"

// MQTT Configuration
#define MQTT_SERVER "192.168.2.226"
#define MQTT_PORT 1883
#define ROOM "house/room1"

// MQTT Topics
#define CLIENT_ID ROOM"/esp2"
#define TOPIC_RECEIVE "mqttserver.to.mqttclient"

// Notification Configuration
unsigned long lastNotifyTime = 0;
const unsigned long notifyInterval = 100;

// WiFi Client
WiFiClient espClient;
PubSubClient mqttClient(espClient);

// MqttManager and Hardware Objects
MqttManager* mqttManager;
Camera* camera;

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
    if (mqttClient.connect(CLIENT_ID)) {
      Serial.println("Connected to MQTT server");
      mqttClient.subscribe(TOPIC_RECEIVE);
    } else {
      Serial.print(".");
      delay(500);
    }
  }
}

void messageReceivedCallback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message received from server: ");

  for (unsigned int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

void setup() {
  Serial.begin(115200);
  
  connectToWiFi();
  
  mqttClient.setServer(MQTT_SERVER, MQTT_PORT);
  mqttClient.setBufferSize(BUFFER_SIZE);
  mqttClient.setCallback(messageReceivedCallback);
  
  connectToMQTT();

  mqttManager = new MqttManager(&mqttClient, BUFFER_SIZE, ROOM);
  camera = new Camera();
  camera->attach(mqttManager);
}

void loop() {
  if (!mqttClient.connected()) {
    Serial.println("MQTT server connection lost. Reconnecting...");
    connectToMQTT();
  }
  
  mqttClient.loop();

  unsigned long currentTime = millis();
  if (currentTime - lastNotifyTime >= notifyInterval) {
    camera->notify();
    lastNotifyTime = currentTime;
  }
}