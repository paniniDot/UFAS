#include "CO2.h"

Co2::Co2(int interval)
  : JSONSensor("co2"), measInterval(interval) {
  this->co2ppm = 0;
  this->err = XENSIV_PASCO2_OK;
}

void Co2::begin() {
  // Initialize the i2c interface used by the sensor
  Wire.begin();
  Wire.setClock(I2C_FREQ_HZ);

  // Initialize the sensor
  err = cotwo.begin();
  if (XENSIV_PASCO2_OK != err) {
    Serial.print("initialization error: ");
    Serial.println(err);
  }
}

void Co2::measure() {
  // Trigger a one shot measurement
  err = cotwo.startMeasure();
  if (XENSIV_PASCO2_OK != err) {
    Serial.print("error: ");
    Serial.println(err);
    return;
  }

  // Wait for the value to be ready
  delay(measInterval * 1000);

  do {
    err = cotwo.getCO2(co2ppm);
    if (XENSIV_PASCO2_OK != err) {
      Serial.print("error: ");
      Serial.println(err);
      break;
    }
  } while (0 == co2ppm);

  Serial.print("co2 ppm value: ");
  Serial.println(co2ppm);

  this->notify();
}

void Co2::notify() {
  Event<String> *json = new Event<String>(EventSourceType::CO52, new String(this->getJson(this->co2ppm)));
  for (int i = 0; i < this->getNObservers(); i++) {
    this->getObservers()[i]->update(json);
  }
  delete json;
}
