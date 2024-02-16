#include "Light.h"

Light::Light(int pin)
  : JSONSensor("light") {
  this->pin = pin;
  pinMode(this->pin, OUTPUT);
  this->lightState = 0;
  this->pir_state = 0;
  this->photoresistor_state = 0;
  this->manual_state = 0;
}

void Light::update(Event<int> *e) {
  this->handleEvent(e);
  this->updateLightState();
  this->notify();
}

void Light::handleEvent(Event<int> *e) {
  EventSourceType source = e->getSrcType();
  int measure = e->getEventArgs();
  if (source == EventSourceType::MANUAL_LIGHT) {
    this->manual_state = measure;
  } else if (source == EventSourceType::PIR) {
    this->pir_state = measure;
  } else if (source == EventSourceType::PHOTO_RESISTOR) {
    this->photoresistor_state = measure;
  }
}

void Light::updateLightState() {
  if (!this->manual_state) {
    this->lightState = (pir_state && photoresistor_state) ? 1 : 0;
  }
  digitalWrite(this->pin, this->lightState ? HIGH : LOW);
}

void Light::notify() {
  Event<String> *json = new Event<String>(EventSourceType::LIGHT, new String(this->getJson(this->lightState)));
  for(auto observer : this->observers) {
    observer->update(json);
  }
  delete json;
}
