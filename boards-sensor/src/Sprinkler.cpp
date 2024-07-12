#include "Sprinkler.h"

Sprinkler::Sprinkler(int pin)
  : JSONSensor("sprinkler") {
  this->pin = pin;
  pinMode(this->pin, OUTPUT);
}

void Sprinkler::update(Event<int> *e) {
  this->updateSprinklerState(e);
}

void Sprinkler::updateSprinklerState(Event<int> *e) {
  int measure = *(e->getEventArgs());
  digitalWrite(this->pin, measure ? HIGH : LOW);
}
