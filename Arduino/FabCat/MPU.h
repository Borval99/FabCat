#define MPU 0x68  // Indirizzo MPU-6050


double AcX, AcY, AcZ;
int Pitch = 0, Roll = 0;
int Nletture = 0;
unsigned long Output;

//Inizializzazione Funzioni
void init_MPU();
void FunctionsMPU();
double FunctionsPitchRoll(double A, double B, double C);
void MPUVision(byte _speed);
int getPitch();
int getRoll();
void getPitchRoll(char* Pitch, char* Roll) ;
unsigned long BTPitchRollSend(long Pitch, int Roll);

//Funzioni
void init_MPU() {
  Wire.begin();
  Wire.beginTransmission(MPU);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  delay(1);
  Wire.endTransmission(true);
  delay(30);
}

void FunctionsMPU() {
  Wire.beginTransmission(MPU);
  Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
  Wire.endTransmission(false);
  Wire.requestFrom(MPU, 6, true); // request a total of 14 registers
  AcX = Wire.read() << 8 | Wire.read(); // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)
  AcY = Wire.read() << 8 | Wire.read(); // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
  AcZ = Wire.read() << 8 | Wire.read(); // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
}

double FunctionsPitchRoll(double A, double B, double C) {
  double DatoA, DatoB, Value;
  DatoA = A;
  DatoB = (B * B) + (C * C);
  DatoB = sqrt(DatoB);

  Value = atan2(DatoA, DatoB);
  Value = Value * 180 / 3.14;

  return Value;
}

void MPUVision(byte _speed) {

  FunctionsMPU(); // Acquisisco assi AcX, AcY, AcZ.

  Roll += FunctionsPitchRoll(AcX, AcY, AcZ) - 2.1; //Calcolo angolo Roll
  Pitch += FunctionsPitchRoll(AcY, AcX, AcZ) + 0.8; //Calcolo angolo Pitch

  if (Nletture > _speed) {
    Serial.print("Pitch: "); Serial.print(Pitch / _speed);
    Serial.print("\t");
    Serial.print("Roll: "); Serial.print(Roll / _speed);
    Serial.print("\n");
    Nletture = 0;
    Pitch = 0, Roll = 0;
  }
  Nletture++;
}

void getPitchRoll(char &Pitch, char &Roll) {
  FunctionsMPU(); // Acquisisco assi AcX, AcY, AcZ.

  Pitch = (int)(FunctionsPitchRoll(AcY, AcX, AcZ) + 0.8); //Calcolo angolo Pitch
  Roll = (int)(FunctionsPitchRoll(AcX, AcY, AcZ) - 2.1); //Calcolo angolo Roll
}

unsigned long BTPitchRollSend(long Pitch, int Roll) {
  Output = 220090090;
  Output += (Pitch * 1000) + Roll;
  return Output;
}
