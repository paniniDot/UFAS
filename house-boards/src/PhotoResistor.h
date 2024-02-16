#ifndef __PHOTORESISTOR__
#define __PHOTORESISTOR__
#include "Arduino.h"
#include "JSONSensor.h"

class PhotoResistor : public JSONSensor<int>
{

private:
  int pin;
  int isDark();

public:
  PhotoResistor(int pin);
  String toJson() {
    return this->getJson(this->isDark());
  }  
};

#endif