//#include "Wire.h"

#include "Funzioni.h"
#include "Servo.h"
int start = 0;

void setup() {
//Max 3 funz a volta
  pinMode(3,INPUT);
  pinMode(5,INPUT);
  pinMode(6,INPUT);
  digitalWrite(3,HIGH);
  digitalWrite(5,HIGH);
  digitalWrite(6,HIGH);
  

  pwm.begin();//SERVO inizializzazione
  pwm.setPWMFreq(50); //imposta SERVO frequenza a 50 HZ
 Funzione(0);
}

void loop() {
 if (digitalRead(3) == 0) {  
  delay(1000);
    Funzione(1);
  }
 if (digitalRead(5) == 0) {  
   start = 1;
  }
  
 if (digitalRead(6) == 0) {   
    start = 0;
  }
 if(start == 1){
    Funzione(2);
  }
}
