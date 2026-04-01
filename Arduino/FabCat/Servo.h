#include <Adafruit_PWMServoDriver.h>

//##########SERVO##########
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(); //Servo pwm, indirizzo 0x40

#define SERVOMIN  110 //100 Valori minimi servo
#define SERVOMAX  470 //480 Valori massimi servo

//Inizializzazione Funzioni
int adjustServo(int _valServo, byte _nServo);
void writeServo(byte _nServo, int _valServo);
void sleepMotors();
void MovGambeAuto(byte selGamba, int k);

int r_coscia = 43, r_stinco = 49, t = 0;
double x, y;
byte angoliMotori[11] = {90, 150, 120, 20, 18, 20, 20, 168, 170, 168, 168};

//Funzioni
int adjustServo(int _valServo, byte _nServo) {
  if (_valServo < (1 + calibrazione[_nServo])) _valServo = (1 + calibrazione[_nServo]);
  if (_valServo > (179 + calibrazione[_nServo])) _valServo = (179 + calibrazione[_nServo]);
  return ((_valServo * 2) + SERVOMIN);
}

void writeServo(byte _nServo, int _valServo) {
  angoliMotori[_nServo] = _valServo;
  //Serial.println(angoliMotori[_nServo]);
  _valServo = abs(-calibrazione2[_nServo] + _valServo);
  _valServo += calibrazione[_nServo];
  _valServo = adjustServo(_valServo, _nServo);
  if (_nServo == 2)_nServo = 11;
  pwm.setPWM(_nServo, 0 , _valServo);
}

void sleepMotors() {
  for (int y = 0; y < 16; y++)pwm.setPWM(y, 0, 0);
}

void MovGambeAuto(byte selGamba, int k) {
  x = (sqrt(-pow(k, 2) * (pow(k, 4) - 2 * pow(k, 2) * (pow(r_coscia, 2) + pow(r_stinco, 2)) + pow(r_coscia, 4) - 2 * pow(r_coscia, 2) * pow(r_stinco, 2) + pow(r_stinco, 4)))) / (2 * pow(k, 2));
  y = (pow(k, 4) - pow(k, 2) * pow(r_coscia, 2) + pow(k, 2) * pow(r_stinco, 2)) / (2 * k * pow(k, 2) );
  double degree = abs(atan(y / x) * 180 / PI);
  double degree2 = abs(atan((y - (float)k) / x) * 180 / PI);

  if (selGamba == 2) {
    writeServo(3, degree - 15);
    writeServo(7, 180 - degree2 - 30);
  }
  else if (selGamba == 1) {
    writeServo(4, degree - 15);
    writeServo(8, 180 - degree2 - 30);
  }
  else if (selGamba == 4) {
    writeServo(5, degree - 15);
    writeServo(9, 180 - degree2 - 30);
  }
  else if (selGamba == 3) {
    writeServo(6, degree - 15);
    writeServo(10, 180 - degree2 - 30);
  }
}
