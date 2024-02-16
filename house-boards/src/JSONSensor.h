#ifndef __JSONSENSOR_H__
#define __JSONSENSOR_H__

#include "ArduinoJson.h"
#include "Arduino.h"
#include <NTPClient.h>
#include <WiFiUdp.h>

template <typename T>
class JSONSensor {
private:
    String _name;
    WiFiUDP ntpUDP;
    NTPClient timeClient;

protected:
    String getJson(T measure) {
        timeClient.update(); // Update time before sending
        DynamicJsonDocument doc(JSON_OBJECT_SIZE(4));
        doc["name"] = _name;
        doc["measure"] = measure;
        doc["timestamp"] = timeClient.getEpochTime();

        String json;
        serializeJson(doc, json);
        return json;
    }

public:
    JSONSensor(const char* name) : _name(name), timeClient(ntpUDP, "pool.ntp.org", 7200) {
        timeClient.begin();
        timeClient.update();
    }
};

#endif
