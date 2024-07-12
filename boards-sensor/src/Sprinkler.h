#ifndef __SPRINKLER_H__
#define __SPRINKLER_H__

#include "Arduino.h"
#include "JSONSensor.h"
#include "observer/Observer.h"
#include "observer/Subject.h"
#include "observer/Event.h"
#include "observer/EventSourceType.h"

class Sprinkler : public JSONSensor<int>, public Observer<int> {
private:
  int pin;
  void updateSprinklerState(Event<int> *e);

public:
    Sprinkler(int pin);
    void update(Event<int> *e);
};

#endif