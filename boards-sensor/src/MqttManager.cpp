#include "WString.h"
#include "MqttManager.h"

MqttManager::MqttManager(PubSubClient* client, int bufferLength, String topic) : mqttClient(client) {
    this->bufferLength = bufferLength;
    this->topic = topic;
}

void MqttManager::update(Event<String>* e) {
    String message = *(e->getEventArgs());
    publishMessage(message);
}

void MqttManager::publishMessage(String message) {
    int fbLen = message.length();
    mqttClient->beginPublish(this->topic.c_str(), fbLen, true);
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
    Serial.println("Message published to " + this->topic);
}