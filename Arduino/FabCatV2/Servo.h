
//DEBUG
//#define DEBUG
#ifdef DEBUG
#define SP(a) Serial.print(a)
#define SPL(a) Serial.println(a)
#else
#define SP(a) (a)
#define SPL(a) (a)
#endif

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

void Funzione(byte _funz);
unsigned int getFinalAddress(byte _funz);
void scriviEEPROM();

//Dichiarazione variabili
volatile byte  fixServoPosition[11]{4,8,6,12,0,9,5,3,11,1,13};
int r_coscia = 43, r_stinco = 49, t = 0;
double x, y;
byte angoliMotori[11] = {90, 150, 120, 20, 18, 20, 20, 168, 170, 168, 168};

double Diviso[11], Meno[11];
int finale[11];
double _FinalValue[11];
int MaxMot = 0;
int lastFunz = 0;

//Funzioni
int adjustServo(int _valServo, byte _nServo) {
  if (_valServo < (1 + calibrazione[_nServo])) _valServo = (1 + calibrazione[_nServo]);
  if (_valServo > (179 + calibrazione[_nServo])) _valServo = (179 + calibrazione[_nServo]);
  return ((_valServo * 2) + SERVOMIN);
}

void writeServo(byte _nServo, int _valServo) {
  angoliMotori[_nServo] = _valServo;
//  SP(_nServo);
//  SP("  ");
//  SPL(angoliMotori[_nServo]);
  _valServo = abs(-calibrazione2[_nServo] + _valServo);
  _valServo += calibrazione[_nServo];
  _valServo = adjustServo(_valServo, _nServo);
  pwm.setPWM(fixServoPosition[_nServo], 0 , _valServo);
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

void Funzione(byte _funz) {
  SP("Funzione richiamata :");
  SPL(_funz);
  if (lastFunz != _funz || _funz==9||_funz==8|| _funz==7|| _funz==6) {
    //SPL(lastFunz);
    if (lastFunz == 11 && _funz != 12)Funzione(12);
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
            SPL(_FinalValue[_motore] );
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
    lastFunz = _funz ;
  }
  SPL(" OK");
}










//NON CANCELLARE UNICA VERSIONE BUONA SE LO CANCELLATE VI INCULO A SANGUE
// x = (pow(k, 2) * t - sqrt((-pow(k, 2) * (pow(k, 4) - 2 * pow(k, 2) * (pow(r_coscia, 2) + pow(r_stinco, 2) - pow(t, 2)) + pow(r_coscia, 4) - 2 * pow(r_coscia, 2) * (pow(r_stinco, 2) + pow(t, 2)) + pow((pow(r_stinco, 2) - pow(t, 2)), 2)))) - pow(r_coscia, 2) * t + pow(r_stinco, 2) * t + pow(t, 3)) / (2 * (pow(k, 2) + pow(t, 2)));
// y = (pow(k, 4) - pow(k, 2) * pow(r_coscia, 2) + pow(k, 2) * pow(r_stinco, 2) + pow(k, 2) * pow(t, 2) - t * sqrt((-pow(k, 2) * (pow(k, 4) - 2 * pow(k, 2) * (pow(r_coscia, 2) + pow(r_stinco, 2) - pow(t, 2)) + pow(r_coscia, 4) - 2 * pow(r_coscia, 2) * (pow(r_stinco, 2) + pow(t, 2)) + pow((pow(r_stinco, 2) - pow(t, 2)), 2))))) / (2 * k * (pow(k, 2) + pow(t, 2)));
// double degree = abs(atan(y / x) * 180 / PI);
// double degree2 = abs(atan((y - (float)k) / (x - (float)t)) * 180 / PI);
