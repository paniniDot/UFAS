#ifndef __ROLL__
#define __ROLL__

#include <ESP32Servo.h>
#include "Arduino.h"
#include "TimeLib.h"
#include "JSONSensor.h"
#include "observer/Observer.h"
#include "observer/Subject.h"
#include "observer/Event.h"
#include "observer/EventSourceType.h"

class Roll : public JSONSensor<int>, public Observer<int>, public Subject<String>
{
private:

  Servo *servo;
  int roll_state;
  int isDay;
  int pir_state;
  int manual_state;

public:
  Roll(int pin);
  void update(Event<int> *e);
  void notify();
};

#endif