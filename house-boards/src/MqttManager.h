#ifndef __MQTT_MANAGER_H__
#define __MQTT_MANAGER_H__

#include <Adafruit_MQTT.h>
#include "Adafruit_MQTT_Client.h"
#include "observer/Observer.h"
#include "observer/EventSourceType.h"
#include "Light.h"
#include "Roll.h"
#include <map>

class MqttManager : public Observer<String> {
private:
    Adafruit_MQTT_Client* mqttClient;
    std::map<String, Adafruit_MQTT_Publish*> topicPublishers;

    String getTopic(EventSourceType sourceType);
    void publishMessage(String topic, String message);
public:
    MqttManager(Adafruit_MQTT_Client* client);

    void addPublisher(String topic, Adafruit_MQTT_Publish* publisher);
    void update(Event<String>* e);
};

#endif
