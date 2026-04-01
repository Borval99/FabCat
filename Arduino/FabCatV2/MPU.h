#include <Arduino_LSM9DS1.h>

#include "Servo.h"

float  AcX, AcY, AcZ;

int Pitch = 0, Roll = 0;
float pitchAdjust = 0, rollAdjust = 0;
int Nletture = 0;
unsigned long Output;

//Inizializzazione Funzioni
void getAccelValue();
float FunctionsPitchRoll(float A, float B, float C);
void MPUVision(byte _speed);
void getPitchRoll(long* Pitch, int* Roll) ;
unsigned long BTPitchRollSend(long Pitch, int Roll);

//Funzioni

void getAccelValue() {
  if (IMU.accelerationAvailable()) {
    IMU.readAcceleration(AcY, AcX, AcZ);
    AcX=-AcX;
    AcY=-AcY;
  }
}
float FunctionsPitchRoll(float A, float B, float C) {
  float DatoA, DatoB, Value;
  DatoA = A;
  DatoB = (B * B) + (C * C);
  DatoB = sqrt(DatoB);

  Value = atan2(DatoA, DatoB);
  Value = Value * 180 / 3.14;

  return Value;
}

void MPUVision(byte _speed) {

  getAccelValue();

  Roll += FunctionsPitchRoll(AcX, AcY, AcZ) + rollAdjust; //Calcolo angolo Roll
  Pitch += FunctionsPitchRoll(AcY, AcX, AcZ) + pitchAdjust; //Calcolo angolo Pitch

  if (Nletture > _speed) {
    SP("Pitch: "); Serial.print(Pitch / _speed);
    SP("\t");
    SP("Roll: "); Serial.print(Roll / _speed);
    SP("\n");
    Nletture = 0;
    Pitch = 0, Roll = 0;
  }
  Nletture++;
}

void getPitchRoll(int &Pitch, int &Roll) {

  getAccelValue();

  Roll = FunctionsPitchRoll(AcX, AcY, AcZ) + rollAdjust; //Calcolo angolo Roll
  Pitch = FunctionsPitchRoll(AcY, AcX, AcZ) + pitchAdjust; //Calcolo angolo Pitch


}

unsigned long BTPitchRollSend(long Pitch, int Roll) {
  Output = 222090090;
  Output += (Pitch * 1000) + Roll;
  return Output;
}
