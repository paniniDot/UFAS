#include "WString.h"
#include "MqttManager.h"

MqttManager::MqttManager(PubSubClient* client, int bufferLength) : mqttClient(client) {
    this->bufferLength = bufferLength;
}

void MqttManager::addPublisher(String topic, String publisher) {
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
        String message = *(e->getEventArgs());
        publishMessage(topic, message);
    } else {
        Serial.println("Publisher not found for the topic");
    }
}


void MqttManager::publishMessage(String topic, String message) {
    if (mqttClient->connected()) {
        if (topicPublishers.find(topic) != topicPublishers.end()) {
            String publisher = topicPublishers[topic];
            int fbLen = message.length();
            mqttClient->beginPublish(publisher.c_str(), fbLen, true);
            String str = "";
            for (size_t n = 0; n < fbLen; n = n + bufferLength) {
                if (n + bufferLength < fbLen) {
                    str = message.substring(n, n + bufferLength);
                    mqttClient->write((uint8_t*)str.c_str(), bufferLength);
                } else if (fbLen % bufferLength > 0) {
                    size_t remainder = fbLen % bufferLength;
                    str = message.substring(n, n + remainder);
                    mqttClient->write((uint8_t*)str.c_str(), remainder);
                }
            }
            mqttClient->endPublish();
            Serial.println("Message published to " + publisher);
        }
    } else {
        Serial.println("MQTT client not connected");
    }
}