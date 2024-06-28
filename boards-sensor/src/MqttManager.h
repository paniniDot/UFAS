#ifndef __MQTT_MANAGER_H__
#define __MQTT_MANAGER_H__

#include "Arduino.h"
#include <PubSubClient.h>
#include "observer/Observer.h"
#include "observer/EventSourceType.h"
#include <map>

class MqttManager : public Observer<String> {
private:
    PubSubClient* mqttClient;
    String topic;
    int bufferLength;

    String getTopic(EventSourceType sourceType);
    void publishMessage(String message);    
public:
    MqttManager(PubSubClient* client, int bufferLength, String topic);

    void update(Event<String>* e);
};

#endif