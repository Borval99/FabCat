package fablab.fabcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import javafx.application.Platform;

public class SpeechRecognition implements Runnable {
  Thread sr;
  private static Bluetooth bluetooth;
  public SpeechRecognition(Bluetooth bluetooth) {
    System.out.println("Avvio SpeechRecognition...");
    sr = new Thread(this);
    this.bluetooth = bluetooth;
  }
  public void start() {
    sr.start();
  }
  public void run() {
    python();
  }

  public static void python() {
    Platform.runLater(new Runnable() {
      @Override public void run() {
        Main.speechRecBtnUpdater.set("Avvio in corso...");
      }
    });
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (Main.windowsOrLinux) {
      processBuilder.command("cmd.exe", "/c", "python python/main.py");
    } else {
      processBuilder.command("python3", "python/main.py");
    }
    try {
      Process process = processBuilder.start();
      Platform.runLater(new Runnable() {
        @Override public void run() {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            System.out.println(e);
          }
          Main.speechRecBtnUpdater.set("In ascolto...");
        }
      });
      int exitVal = process.waitFor();

      StringBuilder output = new StringBuilder();

      BufferedReader reader = new BufferedReader(
      				new InputStreamReader(process.getInputStream()));
      String line;
  		while ((line = reader.readLine()) != null) {
  			output.append(line);
  		}

      if (exitVal == 0) {
        System.out.println("Riconosciuto: " + output);
      } else {
        System.out.println("Non ho riconosciuto nulla");
      }
      if (output.toString().equalsIgnoreCase("spegni")) {
        bluetooth.send((byte) 221, (byte) 0);
        System.out.println("Funzione sciogli eseguita!");
      }
      if (output.toString().equalsIgnoreCase("sveglia")) {
        bluetooth.send((byte) 221, (byte) 1);
        System.out.println("Funzione start eseguita!");
      }
      if (output.toString().equalsIgnoreCase("calibrazione")) {
        bluetooth.send((byte) 221, (byte) 2);
        System.out.println("Funzione calib eseguita!");
      }
      if (output.toString().equalsIgnoreCase("90")) {
        bluetooth.send((byte) 221, (byte) 3);
        System.out.println("Funzione 90 eseguita!");
      }
      if (output.toString().equalsIgnoreCase("dormi")) {
        bluetooth.send((byte) 221, (byte) 5);
        System.out.println("Funzione dormi eseguita!");
      }
	  if (output.toString().equalsIgnoreCase("miagola")) {
        bluetooth.send((byte) 222, (byte) 30);
        System.out.println("Funzione meow eseguita!");
      }
	  if (output.toString().equalsIgnoreCase("saluta")) {
		bluetooth.send((byte) 221, (byte) 10);

        System.out.println("Funzione saluta eseguita!");
      }
	  if (output.toString().equalsIgnoreCase("seduto")) {
        bluetooth.send((byte) 221, (byte) 11);
        System.out.println("Funzione seduto eseguita!");
      }
	  if (output.toString().equalsIgnoreCase("attiva raddrizzamento automatico")) {
        bluetooth.send((byte) 222, (byte) 11);
        System.out.println("Raddrizzamento automatico : Attivato");
      }
	  if (output.toString().equalsIgnoreCase("disattiva raddrizzamento automatico")) {
        bluetooth.send((byte) 222, (byte) 10);
        System.out.println("Raddrizzamento automatico : Disattivato");
      }
      Platform.runLater(new Runnable() {
        @Override public void run() {
          Main.speechRecBtnUpdater.set(new String(Character.toChars(0x1F3A4)) + " Comando Vocale");
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
