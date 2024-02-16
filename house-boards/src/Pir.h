#ifndef __PIR__
#define __PIR__
#include "Arduino.h"
#include "JSONSensor.h"
class Pir : public JSONSensor<int>
{
private:
  int pir_pin;
  int led_pin;
  int getMotion();

public:
  Pir(int pir_pin,int led_pin);
  String toJson() {
    return this->getJson(this->getMotion());
  }

};

#endif