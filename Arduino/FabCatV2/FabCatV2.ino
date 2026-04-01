#include <Wire.h>
#include <Arduino_APDS9960.h>

#include "Funzioni.h"
#include "MPU.h"

//Debug in servo.h

//Inizializzazione Funzioni
void CheckSwitchState();
void controllaGattoCaduto(unsigned int _time = 1000);
void gambeDavanti(int k);
void gambeIndietro(int k);
void bilanciaAuto(byte _time = 40);
void bilanciaAuto2(int PitchUse,int RollUse,byte _time = 40);
void MovGambeAuto(byte selGamba, int k);
void Fmeow();

unsigned long currentmillisCDD = 0;
unsigned long currentmillisPR = 0;
unsigned long currentmillisBA = 0;
byte Comando = 0;
byte nServo = 0;
byte valServo = 0;
byte Nfunzione;
byte NSwitch;
int Val1;
int Val2;
int PitchUse, RollUse;
int divisP = 30, divisR = 12;

int HeightFrontRight = -60, HeightFrontLeft = -60, HeightBackRight = -60, HeightBackLeft = -60;

bool CheckDropDown = false;
bool WritePeachRoll = false;
bool AutoBalance = false;
bool recivePitchRoll = false;
unsigned int delayWritePR = 8;


void setup() {
  //ARDUINO
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  digitalWrite(3, 1);
#ifdef DEBUG
  Serial.begin(38400); // inizializzo la seriale di debug
  while (!Serial);
#endif
  currentmillisPR = millis();
  SPL("Start Setup");

  Serial1.begin(38400); 
  // inizializza seriale comunicazione
  
  while (!Serial1);
  SPL("Seriale BT: OK");  
  
  if (IMU.begin())
    SPL("IMU: OK");

  if (APDS.begin())
    SPL("APDS: OK");


  pwm.begin();//SERVO inizializzazione
  pwm.setPWMFreq(50); //imposta SERVO frequenza a 50 HZ
  SPL("Servo: OK");

  Funzione(1); //rilassa  i motori

  delay(500);
  Fmeow();

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

  if (APDS.gestureAvailable()) {
    int gesture = APDS.readGesture();
    if (gesture == GESTURE_RIGHT) {
      Funzione(11);
    }
    if (gesture == GESTURE_UP) {
      Funzione(1);
    }
    if (gesture == GESTURE_DOWN) {
      Funzione(5);
    }
    if (gesture == GESTURE_LEFT) {
      Funzione(10);
    }
  }

  //MPUVision(10);
  //bilanciaAuto();
    
  if (Serial1.available() > 0) {
    Comando = Serial1.read();
    SP("Comando: ");
    SPL(Comando);
  }

  //Decodifica Comandi
  if (Comando == 220) {   //movimentazione motori
    nServo = Serial1.read();
    SP("Nservo: ");
    SPL(nServo);
    valServo = Serial1.read();
    SP("ValServo: ");
    SPL(valServo);
    writeServo(nServo, valServo);
    SPL(" OK");
  }
  else if (Comando == 221) {    // Richiamo funzioni EEPROM
    Nfunzione = Serial1.read();
    Funzione(Nfunzione);
  }
  else if (Comando == 222) {    //Decodifica Switch
    NSwitch = Serial1.read();
    if (NSwitch == 0) {
      WritePeachRoll = false;
      SPL("Scrittura PitchRoll: OFF");
    }
    else if (NSwitch == 1) {
      while (!Serial1.available());
      delayWritePR = Serial1.read();
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
      Fmeow();
    }
  }else if (Comando == 223) {
    NSwitch = Serial1.read();
    if(NSwitch == 0){
      recivePitchRoll = false;
      SPL("Ricevo Dati P/R: OFF");
    }else if(NSwitch == 1){
      recivePitchRoll = true;
      SPL("Ricevo Dati P/R: ON");
    }
     if(NSwitch == 2){
      while (!Serial1.available());
      Val1 = Serial1.read();
      while (!Serial1.available());
      Val2 = Serial1.read();
      Val1 -=90;
      Val2 -=90;
      Serial.println((String)"Ricevo Dati P/R:" + (String)Val1 +(String)" " + (String)Val2 );
      
    }
  }
  CheckSwitchState();
  Comando = 0;
  delayMicroseconds(1500);
}

void CheckSwitchState() {
  if (CheckDropDown == true) controllaGattoCaduto();
  if (WritePeachRoll == true) {
    if (millis() - currentmillisPR >= delayWritePR * 25) {
      getPitchRoll(PitchUse, RollUse);
      Serial1.println(BTPitchRollSend(PitchUse, RollUse));
      currentmillisPR = millis();
    }
  }
  if (AutoBalance == true) bilanciaAuto(100);
  if(recivePitchRoll == true)bilanciaAuto2(Val1,Val2,40); 
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

void bilanciaAuto2(int PitchUse,int RollUse,byte _time) {
  if (millis() - currentmillisBA >= _time) {
      writeServo(2, 150);
      HeightBackLeft = map(sqrt(pow(-RollUse+90,2)+pow(PitchUse+90,2)),0,200,-90,-20);
      HeightBackRight = map(sqrt(pow(-RollUse-90,2)+pow(PitchUse+90,2)),0,200,-90,-20);
      HeightFrontLeft = map(sqrt(pow(-RollUse+90,2)+pow(PitchUse-90,2)),0,200,-90,-20);
      HeightFrontRight = map(sqrt(pow(-RollUse-90,2)+pow(PitchUse-90,2)),0,200,-90,-20);
    
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

void bilanciaAuto(byte _time) {
  if (millis() - currentmillisBA >= _time) {
    getPitchRoll(PitchUse, RollUse);
    if (RollUse < -2) {
      HeightBackLeft += 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
      HeightBackRight += 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
      HeightFrontLeft -= 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
      HeightFrontRight -= 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
    }
    else if (RollUse > 2) {
      HeightBackLeft -= 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
      HeightBackRight -= 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
      HeightFrontLeft += 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
      HeightFrontRight += 1 + abs(RollUse / divisR) * abs(RollUse / divisR);
    }
    if (PitchUse < -2) {
      HeightBackLeft -= 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
      HeightBackRight += 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
      HeightFrontLeft -= 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
      HeightFrontRight += 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
    }
    else if (PitchUse > 2) {
      HeightBackLeft += 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
      HeightBackRight -= 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
      HeightFrontLeft += 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
      HeightFrontRight -= 1 + abs(PitchUse / divisP) * abs(PitchUse / divisP);
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

void Fmeow() {
  digitalWrite(3, 0);
  delay(100);
  SPL("meow");
  digitalWrite(3, 1);
}
