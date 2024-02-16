#include "Roll.h"

Roll::Roll(int pin)
  : JSONSensor("roll") {
  this->servo = new Servo();
  this->servo->attach(pin);
  this->rollState = 0;
  this->pir_state = 0;
  this->isDay = 0;
  this->manual_state = 0;
}

void Roll::update(Event<int> *e) {
  this->handleEvent(e);
  this->updateRollState();
  this->notify();
}

void Roll::handleEvent(Event<int> *e) {
  EventSourceType source = e->getSrcType();
  int measure = e->getEventArgs();
  if (source == EventSourceType::PIR) {
    setTime(timestamp);
    this->pir_state = measure;
    this->isDay = (hour() >= 8 && hour() < 19) ? 1 : 0;
  } else if (source == EventSourceType::MANUAL_ROLL) {
    this->manual_state = measure;
  } 
}

void Roll::updateRollState() {
  if (!this->manual_state) {
    if (this->pir_state && this->isDay) {
      this->rollState = 100;
    } else if (!this->pir_state && !this->isDay) {
      this->rollState = 0;
    }
  }
  this->servo->write(map(this->rollState, 0, 100, 0, 180));
}

void Roll::notify() {
  Event<String> *json = new Event<String>(EventSourceType::ROLL, new String(this->getJson(this->rollState)));
  for(auto observer : this->observers) {
    observer->update(json);
  }
  delete json;
}