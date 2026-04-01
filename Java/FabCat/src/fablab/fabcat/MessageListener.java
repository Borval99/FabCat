package fablab.fabcat;

/*********** NOTA ***********
Per uscire digitare quit, il programma esce solo se mandiamo quit.
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import javax.microedition.io.StreamConnection;
import javafx.application.Platform;


public class MessageListener implements Runnable {
public static Thread listener;
StreamConnection streamConnection;
Comunicator comunicator;
public MessageListener(StreamConnection streamConnection, Comunicator comunicator) {
  listener = new Thread(this);
  this.streamConnection = streamConnection;
  this.comunicator = comunicator;
}
public void start() {
  listener.start();
}
public void run() {
  System.out.println("Starting listener...");
  try {
    InputStream is = streamConnection.openInputStream();
    System.out.println("Input stream created.");
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    System.out.println("Buffered reader created.");
    try {
      String line = null;
      System.out.println("Listener ready!");
      while (!comunicator.received || (line = br.readLine()) != null) {
        System.out.println("received: " + line);
        if (line.equalsIgnoreCase("quit")) {
          System.out.println("quitting");
          stop();
        }
        if (line.startsWith("220")) {
          System.out.println("Comando con header 220 ricevuto: " + line);
          //da controllare la lunchezza e se rimane della roba
          final String ln = line;
          Platform.runLater(new Runnable() {
            @Override public void run() {
              if (Main.setPitchRoll) {
                Main.pitchValUpdater.set(""+(Integer.parseInt(ln.substring(3, Math.min(ln.length(), 6)))-90));
                Main.rollValUpdater.set(""+(Integer.parseInt(ln.substring(6, Math.min(ln.length(), 9)))-90));
              }
            }
          });
        }
        comunicator.received = true;
      }
    } catch (IOException e) {
      if (!Main.bluetoothStopped) {
        System.out.println("Main.bluetoothStopped: " + Main.bluetoothStopped);
        System.out.println("Errore nella lettura dell'input: " + e);
      }
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException e) {
        System.out.println("Errore nella chiusura dello stream: " + e);
      }
    }
    is.close();
  } catch (IOException e) {
    System.out.println("IOException");
  }
}
public void stop() {
  try {
    System.out.println("SIGTERM ricevuto!");
    streamConnection.close();
    System.out.println("sreamConnection chiuso per comando: quit");
    System.out.println("Tentativo di terminazione del programma...");
    System.exit(1); //0 o nulla tira un'eccezione
  } catch (IOException e) {
    System.out.println(e);
  }
}
}
