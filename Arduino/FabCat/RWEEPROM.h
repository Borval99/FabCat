#include <extEEPROM.h>
#include "Servo.h"

extEEPROM EE(kbits_64, 1, 32, 0x51); //EEPROM VALORI(dimensione,N.Chip,Dimensone Page,Indirizzo)

//Inizializzazione Funzioni
byte Funzione(byte _funz);
unsigned int getFinalAddress(byte _funz);
void scriviEEPROM();

//Dichiarazione variabili
double Diviso[11], Meno[11];
int finale[11];
double _FinalValue[11];
int MaxMot = 0;
int lastFunz = 0;
//EEPROM READ
byte Funzione(byte _funz) {
  Serial.print("Funzione richiamata :");
  Serial.println(_funz);
  if (lastFunz != _funz) {
    Serial.println(lastFunz);
    if(lastFunz == 11 && _funz != 12)Funzione(12);
    if (_funz == 0)sleepMotors();
    else {
      if (EE.read(3 + getFinalAddress(_funz)) == 0) {
        for (unsigned int _lungArray = 0; _lungArray < EE.read(getFinalAddress(_funz)); _lungArray++) {
          for (byte _motore = 0; _motore < 11; _motore++) { //leggi il valore per ogni motore e scrivilo senza contare i primi 11 indirizzi
            if (EE.read(11 + getFinalAddress(_funz) + _motore + 11 * _lungArray) != 200)
              writeServo(_motore, EE.read(11 + getFinalAddress(_funz) + _motore + 11 * _lungArray));
            delay(EE.read(1 + getFinalAddress(_funz)) * 10);
          }
          delay(EE.read(2 + getFinalAddress(_funz)) * 10);
        }
      }
      else if (EE.read(3 + getFinalAddress(_funz)) == 1) {
        for (unsigned int _lungArray = 0; _lungArray < EE.read(getFinalAddress(_funz)); _lungArray++) {
          MaxMot = 0;
          for (int _motore = 0; _motore < 11; _motore++) {
            if (EE.read(11 + getFinalAddress(_funz) + _motore + 11 * _lungArray) != 200){
            finale[_motore] = EE.read(11 + getFinalAddress(_funz) + _motore + 11 * _lungArray);
            }
            Meno[_motore] = finale[_motore] - angoliMotori[_motore];
            _FinalValue[_motore] = angoliMotori[_motore];
            //Serial.println(_FinalValue[_motore] );
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
          delay(EE.read(2 + getFinalAddress(_funz)) * 10);
        }
      }
    }
    lastFunz = _funz ;
  }
  Serial.println(" OK");
}

unsigned int getFinalAddress(byte _funz) {
  unsigned int finalAddress = 100;
  for (int _p = 0; _p < _funz ; _p++)
    finalAddress = finalAddress + 11 * addressFunzioni[_p];    //trovo l indirizzo della funzione
  return finalAddress;
}

//EEPROM WRITE
void scriviEEPROM() {
  for (byte _n = 1; _n < Nfunz; _n++) addressFunzioni[_n] = (*pointerFunzioni[_n - 1] ) + 1;
  EE.write(50, addressFunzioni, Nfunz);
  for (byte _n = 0; _n < Nfunz; _n++) EE.write(getFinalAddress(_n + 1), pointerFunzioni[_n], (*pointerFunzioni[_n] + 1) * 11);
}








//NON CANCELLARE UNICA VERSIONE BUONA SE LO CANCELLATE VI INCULO A SANGUE
// x = (pow(k, 2) * t - sqrt((-pow(k, 2) * (pow(k, 4) - 2 * pow(k, 2) * (pow(r_coscia, 2) + pow(r_stinco, 2) - pow(t, 2)) + pow(r_coscia, 4) - 2 * pow(r_coscia, 2) * (pow(r_stinco, 2) + pow(t, 2)) + pow((pow(r_stinco, 2) - pow(t, 2)), 2)))) - pow(r_coscia, 2) * t + pow(r_stinco, 2) * t + pow(t, 3)) / (2 * (pow(k, 2) + pow(t, 2)));
// y = (pow(k, 4) - pow(k, 2) * pow(r_coscia, 2) + pow(k, 2) * pow(r_stinco, 2) + pow(k, 2) * pow(t, 2) - t * sqrt((-pow(k, 2) * (pow(k, 4) - 2 * pow(k, 2) * (pow(r_coscia, 2) + pow(r_stinco, 2) - pow(t, 2)) + pow(r_coscia, 4) - 2 * pow(r_coscia, 2) * (pow(r_stinco, 2) + pow(t, 2)) + pow((pow(r_stinco, 2) - pow(t, 2)), 2))))) / (2 * k * (pow(k, 2) + pow(t, 2)));
// double degree = abs(atan(y / x) * 180 / PI);
// double degree2 = abs(atan((y - (float)k) / (x - (float)t)) * 180 / PI);
