#ifndef __JSONSENSOR_H__
#define __JSONSENSOR_H__

#include "ArduinoJson.h"
#include "Arduino.h"
#include <NTPClient.h>
#include <WiFiUdp.h>

template <typename T>
class JSONSensor {
private:
    String name;
    WiFiUDP ntpUDP;
    NTPClient timeClient;

protected:
    String getJson(T measure) {
        timeClient.update(); // Update time before sending
        DynamicJsonDocument doc(JSON_OBJECT_SIZE(4));
        doc["name"] = name;
        doc["measure"] = measure;
        doc["timestamp"] = timeClient.getEpochTime();

        String json;
        serializeJson(doc, json);
        return json;
    }

public:
    JSONSensor(const char* name) : name(name), timeClient(ntpUDP, "pool.ntp.org") {
        timeClient.begin();
        timeClient.update();
    }
};

#endif
