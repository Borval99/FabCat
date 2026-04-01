#include "Adafruit_PWMServoDriver.h"

//##########SERVO##########
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(); //Servo pwm, indirizzo 0x40

#define SERVOMIN  110 //100 Valori minimi servo
#define SERVOMAX  470 //480 Valori massimi servo

//Inizializzazione Funzioni
int adjustServo(int _valServo, byte _nServo);
void writeServo(byte _nServo, int _valServo);
void sleepMotors();

void Funzione(byte _funz);

//Dichiarazione variabili
volatile byte  fixServoPosition[11] {4, 8, 6, 12, 0, 9, 5, 3, 11, 1, 13};
int r_coscia = 43, r_stinco = 49, t = 0;
double x, y;
byte angoliMotori[11] = {90, 150, 120, 20, 18, 20, 20, 168, 170, 168, 168};

double Diviso[11], Meno[11];
int finale[11];
double _FinalValue[11];
int MaxMot = 0;

//Funzioni
int adjustServo(int _valServo, byte _nServo) {
  if (_valServo < (1 + calibrazione[_nServo])) _valServo = (1 + calibrazione[_nServo]);
  if (_valServo > (179 + calibrazione[_nServo])) _valServo = (179 + calibrazione[_nServo]);
  return ((_valServo * 2) + SERVOMIN);
}

void writeServo(byte _nServo, int _valServo) {
  angoliMotori[_nServo] = _valServo;
  _valServo = abs(-calibrazione2[_nServo] + _valServo);
  _valServo += calibrazione[_nServo];
  _valServo = adjustServo(_valServo, _nServo);
  pwm.setPWM(fixServoPosition[_nServo], 0 , _valServo);
}

void sleepMotors() {
  for (byte y = 0; y < 16; y++)pwm.setPWM(y, 0, 0);
}

void Funzione(byte _funz) {
  if (_funz == 0)sleepMotors();
  else {
    if (pgm_read_byte_near(pointerFunzioni[_funz] + 3 ) == 0) {
      for (unsigned int _lungArray = 0; _lungArray < pgm_read_byte_near(pointerFunzioni[_funz]); _lungArray++) {
        for (byte _motore = 0; _motore < 11; _motore++) {
          if (pgm_read_byte_near(pointerFunzioni[_funz] + _motore + 11 * (_lungArray + 1)) != 200)
            writeServo(_motore, pgm_read_byte_near(pointerFunzioni[_funz] + _motore + 11 * (_lungArray + 1)));
          delay(pgm_read_byte_near(pointerFunzioni[_funz] + 1) * 10);
        }
        delay(pgm_read_byte_near(pointerFunzioni[_funz] + 2) * 10);
      }
    }
    else if (pgm_read_byte_near(pointerFunzioni[_funz] + 3) == 1) {
      for (unsigned int _lungArray = 0; _lungArray < pgm_read_byte_near(pointerFunzioni[_funz]); _lungArray++) {
        MaxMot = 0;
        for (int _motore = 0; _motore < 11; _motore++) {
          if (pgm_read_byte_near(pointerFunzioni[_funz] + _motore + 11 * (_lungArray + 1)) != 200) {
            finale[_motore] = pgm_read_byte_near(pointerFunzioni[_funz] + _motore + 11 * (_lungArray + 1));
          }
          Meno[_motore] = finale[_motore] - angoliMotori[_motore];
          _FinalValue[_motore] = angoliMotori[_motore];
          //SPL(_FinalValue[_motore] );
          if (MaxMot <= abs(Meno[_motore])) MaxMot = abs(Meno[_motore]);
        }
        for (int _motore = 0; _motore < 11; _motore++) {
          if (MaxMot == 0)
            Diviso[_motore] = 0;
          else
            Diviso[_motore] = Meno[_motore] / MaxMot;
        }
        for (int _passaggio = 0; _passaggio < MaxMot; _passaggio++) {
          for (int _motore = 0; _motore < 11; _motore++) {
            writeServo(_motore, (int)(_FinalValue[_motore] + Diviso[_motore] * (_passaggio + 1)));
          }
        }
        delay(pgm_read_byte_near(pointerFunzioni[_funz] + 2) * 10);
      }
    }
  }
}
