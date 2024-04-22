#ifndef __MQTT_MANAGER_H__
#define __MQTT_MANAGER_H__

#include "Arduino.h"
#include <PubSubClient.h>
#include "observer/Observer.h"
#include "observer/EventSourceType.h"
#include "Light.h"
#include "Roll.h"
#include <map>

class MqttManager : public Observer<String> {
private:
    PubSubClient* mqttClient;
    std::map<String, String> topicPublishers;
    int bufferLength;

    String getTopic(EventSourceType sourceType);
    void publishMessage(String topic, String message);
public:
    MqttManager(PubSubClient* client, int bufferLength);

    void addPublisher(String topic, String publisher);
    void update(Event<String>* e);
};

#endif