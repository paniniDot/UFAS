#include "Light.h"

Light::Light(int pin)
  : JSONSensor("light") {
  this->pin = pin;
  pinMode(this->pin, OUTPUT);
  this->light_state = 0;
  this->pir_state = 0;
  this->photoresistor_state = 0;
  this->manual_state = 0;
}

void Light::update(Event<int> *e) {
  EventSourceType source = e->getSrcType();
  int measure = *(e->getEventArgs());
  if (source == EventSourceType::MANUAL_LIGHT) {
    this->manual_state = measure;
  } else if (source == EventSourceType::PIR) {
    this->pir_state = measure;
  } else if (source == EventSourceType::PHOTO_RESISTOR) {
    this->photoresistor_state = measure;
  } else if (source == EventSourceType::LIGHT) {
    if (this->manual_state) {
      this->light_state = measure;
    }
  }
  if (!this->manual_state) {
    this->light_state = (pir_state && photoresistor_state) ? 1 : 0;
  }
  digitalWrite(this->pin, this->light_state ? HIGH : LOW);
  this->notify();
}

void Light::notify() {
  Event<String> *json = new Event<String>(EventSourceType::LIGHT, new String(this->getJson(this->light_state)));
  for (int i = 0; i < this->getNObservers(); i++) {
    this->getObservers()[i]->update(json);
  }
  delete json;
}
