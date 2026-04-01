#include "Buzzer.h"

void setup() {
  pinMode(9, INPUT_PULLUP);
  pinMode(10, INPUT_PULLUP);
}

void loop() {

  if (digitalRead(9) == 0) {
    startPlayback(meow, sizeof(meow));

  }
}
