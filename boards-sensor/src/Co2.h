#ifndef __CO2__
#define __CO2__

#include <Arduino.h>
#include <pas-co2-ino.hpp>
#include "JSONSensor.h"
#include "observer/Observer.h"
#include "observer/Subject.h"
#include "observer/Event.h"
#include "observer/EventSourceType.h"

class Co2 : public JSONSensor<int>, public Subject<String>
{
private:
  PASCO2Ino cotwo;
  int16_t co2ppm;
  Error_t err;
  int measInterval;
  int state;
  unsigned long lastMeasureTime;
  void startMeasurement();
  enum State {
    IDLE,
    MEASURING,
    DONE
  };

public:
  Co2(); // Default interval to 10 seconds
  void begin();
  void measure();
  void notify();
};

#endif
