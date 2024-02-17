#ifndef __MQTT_MANAGER_H__
#define __MQTT_MANAGER_H__

#include <Adafruit_MQTT.h>
#include "Adafruit_MQTT_Client.h"
#include "observer/Observer.h"
#include "src/Light.h"
#include "src/Roll.h"
#include <map>

class MqttManager : public Observer<String> {
private:
    Adafruit_MQTT_Client* mqttClient;
    std::map<String, Adafruit_MQTT_Publish*> topicPublishers;

    const char* getTopic(EventSourceType sourceType);
    void publishMessage(const char* topic, const char* message);
public:
    MqttManager(Adafruit_MQTT_Client* client);

    void addPublisher(const char* topic, Adafruit_MQTT_Publish* publisher);
    void update(Event<String>* e) override;
};

#endif
