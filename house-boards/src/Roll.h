#ifndef __ROLL__
#define __ROLL__

#include <Servo.h>
#include "Arduino.h"
#include "TimeLib.h"
#include "JSONSensor.h"
#include "observer/Observer.h"
#include "observer/Subject.h"
#include "observer/Event.h"
#include "observer/EventSourceType.h"

class Roll : public JSONSensor<int>, Observer<int>, Subject<String>
{
private:

  Servo *servo;
  int rollState;
  int isDay;
  int pir_state;
  int manual_state;
  void handleEvent(Event<int> *e)
  void updateRollState();

public:
  Roll(int pin);
  void update(Event<int> *e);
  void notify();
};

#endif