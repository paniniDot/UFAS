#ifndef __LIGHT__
#define __LIGHT__

#include "Arduino.h"
#include "JSONSensor.h"

class Light : public JSONSensor
{
private:
  int lightState;
  int pin;
  int pir_state;
  int photoresistor_state;
  int manual_state;
  void handleMessage(Msg* msg);
  void updateLightState();

public:
  Light(int pin);
  void notify();
  void update(Event<Msg> *e);
};

#endif