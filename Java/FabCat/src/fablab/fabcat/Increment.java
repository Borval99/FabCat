package fablab.fabcat;

import javafx.application.Platform;

public class Increment implements Runnable {
  int motorNumber;
  Bluetooth blue;
  Thread t;
  boolean loop;
  public Increment(Bluetooth bluetooth, int b){
    t = new Thread(this);
    motorNumber = b;
    blue = bluetooth;
  }
  public int stop(){
    loop = false;
    return Main.motorValues[motorNumber];
  }

  public void start(){
    loop = true;
    t.start();
  }

  public void run(){
    while (loop) {
      Main.motorValues[motorNumber] += Main.multiplier;
      if(Main.motorValues[motorNumber]>180) {
        Main.motorValues[motorNumber]--;
        this.stop();
      } else {
        Platform.runLater(new Runnable() {
          @Override public void run() {
            Main.labels[motorNumber].setText(""+Main.motorValues[motorNumber]);
          }
        });
        blue.send(Main.MOTORCMD,(byte) motorNumber, (byte) Main.motorValues[motorNumber]);
        // System.out.print(Main.motorValues[motorNumber]);
      }
      try {
        Thread.sleep(80);
      } catch (InterruptedException e) {
        System.out.println(e);
      }
    }
  }

}
