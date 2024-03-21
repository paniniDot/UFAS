#include "WString.h"
#include "MqttManager.h"
#include "Arduino.h"

MqttManager::MqttManager(Adafruit_MQTT_Client* client) : mqttClient(client) {
}

void MqttManager::addPublisher(String topic, Adafruit_MQTT_Publish* publisher) {
    topicPublishers[topic] = publisher;
}

String MqttManager::getTopic(EventSourceType sourceType) {
    switch (sourceType) {
        case ROLL:
            return "room1/roll";
        case LIGHT:
            return "room1/light";
        case CAMERA:
            return "room1/cam";
        default:
            return "unknown";
    }
}

void MqttManager::update(Event<String>* e) {
    String topic = getTopic(e->getSrcType());
    if (topicPublishers.find(topic) != topicPublishers.end()) {
        publishMessage(topic, e->getEventArgs()->c_str());
    } else {
        Serial.println("Publisher not found for the topic");
    }
}

void MqttManager::publishMessage(String topic, String message) {
    if (mqttClient->connected()) {
        if (topicPublishers.find(topic) != topicPublishers.end()) {
            Adafruit_MQTT_Publish* publisher = topicPublishers[topic];
            if (publisher->publish(message.c_str())) {
                Serial.print("Published ");
                Serial.print(topic);
                Serial.println(" value");
            } else {
                Serial.print("Failed to publish ");
                Serial.print(topic);
                Serial.println(" value");
            }
        }
    } else {
        Serial.println("MQTT client not connected");
    }
}
