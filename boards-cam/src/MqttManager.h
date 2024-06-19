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
    String room;
    int bufferLength;

    String getTopic(EventSourceType sourceType);
    
public:
    MqttManager(PubSubClient* client, int bufferLength, String room);

    void publishMessage(String topic, String message);
    void update(Event<String>* e);
};

#endif