//#include <SoftSerialIntAP.h>
#include <SoftwareSerial.h>
#include <Wire.h>

#include "Funzioni.h"
#include "MPU.h"
#include "RWEEPROM.h"
#include "Buzzer.h"


#define SCRIVIEEPROM  // per scrittura su eeprom

//##########SERIAL FOR BLUETOOTH##########
//SoftSerialIntAP serial1(15, 16, 3);
SoftwareSerial serial1(9, 10); // Tx, Rx

//SHORTCUT
#define SP(a) Serial.print(a)
#define SPL(a) Serial.println(a)

//Inizializzazione Funzioni
void CheckSwitchState();
void controllaGattoCaduto(unsigned int _time = 1000);
void gambeDavanti(int k);
void gambeIndietro(int k);
void bilanciaAuto(byte _time = 100);
void MovGambeAuto(byte selGamba, int k);

unsigned long currentmillisCDD = 0;
unsigned long currentmillisPR = 0;
unsigned long currentmillisBA = 0;
byte Comando = 0;
byte nServo = 0;
byte valServo = 0;
byte Nfunzione;
byte NSwitch;
char PitchUse, RollUse;

char HeightFrontRight = -60, HeightFrontLeft = -60, HeightBackRight = -60, HeightBackLeft = -60;

bool CheckDropDown = false;
bool WritePeachRoll = false;
bool AutoBalance = false;
unsigned int delayWritePR = 8;


void setup() {
  //ARDUINO
  Serial.begin(38400); // inizializzo la seriale di debug
  currentmillisPR = millis();
  SPL("Start Setup");



  serial1.begin(38400); // inizializza seriale comunicazione
  //serial1.listen(); // selezione serial1
  SPL("Seriale: OK");

  init_MPU(); //MPU inizializzazione
  SPL("MPU: OK");

  EE.begin(EE.twiClock400kHz);  //EEPROM imposta velocità clock 400kHz
  SPL("EEPROM : OK");

  pwm.begin();//SERVO inizializzazione
  pwm.setPWMFreq(50); //imposta SERVO frequenza a 50 HZ
  SPL("Servo: OK");

#ifdef SCRIVIEEPROM // per scrittura su eeprom
  scriviEEPROM();
  SPL("Scrittura EEPROM: OK");
#endif

  Funzione(1); //rilassa  i motori

  SP("Setup Completed Succesfully in ");
  SP( millis() - currentmillisPR);
  SPL("ms");

//  randomSeed(analogRead(0));
//
//  for (int i = 0; i < 11; i++) {
//    int x = random(30, 150);
//    writeServo(i, x);
//  }

}

void loop() {
  //MPUVision(10);
  //bilanciaAuto();

  if (serial1.available() > 0) {
    Comando = serial1.read();
    SP("Comando: ");
    SPL(Comando);
  }

  //Decodifica Comandi
  if (Comando == 220) {   //movimentazione motori
    nServo = serial1.read();
    SP("Nservo: ");
    SPL(nServo);
    valServo = serial1.read();
    SP("ValServo: ");
    SPL(valServo);
    writeServo(nServo, valServo);
    SPL(" OK");
  }
  else if (Comando == 221) {    // Richiamo funzioni EEPROM
    Nfunzione = serial1.read();
    Funzione(Nfunzione);
  }
  else if (Comando == 222) {    //Decodifica Switch
    NSwitch = serial1.read();
    if (NSwitch == 0) {
      WritePeachRoll = false;
      SPL("Scrittura PitchRoll: OFF");
    }
    else if (NSwitch == 1) {
      delayWritePR = serial1.read();
      WritePeachRoll = true;
      SPL("Scrittura PitchRoll: ON");
      SP("Scrittura PitchRoll Delay: ");
      SP(delayWritePR * 25);
      SPL("ms");
    }
    else if (NSwitch == 10) {
      CheckDropDown = false;
      SPL("Controllo Caduta: OFF");
    }
    else if (NSwitch == 11) {
      CheckDropDown = true;
      SPL("Controllo Caduta: ON");
    }
    else if (NSwitch == 20) {
      AutoBalance = false;
      SPL("Bilanciamento Automatico: OFF");
    }
    else if (NSwitch == 21) {
      AutoBalance = true;
      SPL("Bilanciamento Automatico: ON");
      Funzione(1);
      writeServo(2, 0);
      currentmillisBA = millis();
    }
    else if (NSwitch == 30) {
      startPlayback(meow, sizeof(meow));
      SPL("meow");

    }
  }
  CheckSwitchState();
  Comando = 0;
}

void CheckSwitchState() {
  if (CheckDropDown == true) controllaGattoCaduto();
  if (WritePeachRoll == true) {
    if (millis() - currentmillisPR >= delayWritePR * 25) {
      getPitchRoll(PitchUse, RollUse);
      serial1.println(BTPitchRollSend(PitchUse, RollUse));
      currentmillisPR = millis();
    }
  }
  if (AutoBalance == true) bilanciaAuto();
}

void controllaGattoCaduto(unsigned int _time) {
  if (millis() - currentmillisCDD >= _time) {
    getPitchRoll(PitchUse, RollUse);

    if (PitchUse > 30) Funzione(6);
    else if (PitchUse < -30) Funzione(7);
    else if (RollUse < -40) Funzione(8);
    currentmillisCDD = millis();
  }
}

void bilanciaAuto(byte _time) {
  if (millis() - currentmillisBA >= _time) {
    getPitchRoll(PitchUse, RollUse);
    if (RollUse < -2) {
      HeightBackLeft += 1 + pow(abs(RollUse / 7), 2);
      HeightBackRight += 1 + pow(abs(RollUse / 7), 2);
      HeightFrontLeft -= 1 + pow(abs(RollUse / 7), 2);
      HeightFrontRight -= 1 + pow(abs(RollUse / 7), 2);
    }
    else if (RollUse > 2) {
      HeightBackLeft -= 1 + pow(abs(RollUse / 7), 2);
      HeightBackRight -= 1 + pow(abs(RollUse / 7), 2);
      HeightFrontLeft += 1 + pow(abs(RollUse / 7), 2);
      HeightFrontRight += 1 + pow(abs(RollUse / 7), 2);
    }
    if (PitchUse < -2) {
      HeightBackLeft -= 1 + pow(abs(RollUse / 7), 2);
      HeightBackRight += 1 + pow(abs(RollUse / 7), 2);
      HeightFrontLeft -= 1 + pow(abs(RollUse / 7), 2);
      HeightFrontRight += 1 + pow(abs(RollUse / 7), 2);
    }
    else if (PitchUse > 2) {
      HeightBackLeft += 1 + pow(abs(RollUse / 7), 2);
      HeightBackRight -= 1 + pow(abs(RollUse / 7), 2);
      HeightFrontLeft += 1 + pow(abs(RollUse / 7), 2);
      HeightFrontRight -= 1 + pow(abs(RollUse / 7), 2);
    }
    if (HeightFrontRight < -90)HeightFrontRight = -90;
    if (HeightBackRight < -90)HeightBackRight = -90;
    if (HeightFrontLeft < -90)HeightFrontLeft = -90;
    if (HeightBackLeft < -90)HeightBackLeft = -90;
    if (HeightFrontRight > -20)HeightFrontRight = -20;
    if (HeightBackRight > -20)HeightBackRight = -20;
    if (HeightFrontLeft > -20)HeightFrontLeft = -20;
    if (HeightBackLeft > -20)HeightBackLeft = -20;
    MovGambeAuto(1, HeightFrontRight);
    MovGambeAuto(2, HeightFrontLeft);
    MovGambeAuto(3, HeightBackLeft);
    MovGambeAuto(4, HeightBackRight);
    currentmillisBA = millis();
  }
}
