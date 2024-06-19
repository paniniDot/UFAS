#include "WString.h"
#include "MqttManager.h"

MqttManager::MqttManager(PubSubClient* client, int bufferLength, String room) : mqttClient(client) {
    this->bufferLength = bufferLength;
    this->room = room;
}

String MqttManager::getTopic(EventSourceType sourceType) {
    switch (sourceType) {
        case ROLL:
            return this->room + "/roll";
        case LIGHT:
            return this->room + "/light";
        case CAMERA:
            return this->room + "/cam";
        default:
            return "unknown";
    }
}

void MqttManager::update(Event<String>* e) {
    String topic = getTopic(e->getSrcType());
    String message = *(e->getEventArgs());
    publishMessage(topic, message);
}


void MqttManager::publishMessage(String topic, String message) {
    int fbLen = message.length();
    mqttClient->beginPublish(topic.c_str(), fbLen, true);
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
    Serial.println("Message published to " + topic);
}