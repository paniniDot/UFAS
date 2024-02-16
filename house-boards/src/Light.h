#ifndef __LIGHT__
#define __LIGHT__

#include "Arduino.h"
#include "JSONSensor.h"
#include "observer/Observer.h"
#include "observer/Subject.h"
#include "observer/Event.h"
#include "observer/EventSourceType.h"

class Light : public JSONSensor<int>, Observer<int>, Subject<String>
{
private:
  int pin;
  int pir_state;
  int photoresistor_state;
  int manual_state;
  void handleEvent(Event<int> *e)
  void updateLightState();

public:
  Light(int pin);
  void update(Event<int> *e);
  void notify();
};

#endif