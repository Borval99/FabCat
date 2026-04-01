package fablab.fabcat;

import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.StreamConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;

public class Comunicator implements Runnable {
  public static Thread comunicator;
  StreamConnection streamConnection;
  byte[] command;
  public volatile boolean received = true;
  Timer timer = new Timer();
  List<byte[]> commands = new ArrayList<byte[]>();
  boolean timerStarted = false;
  public Comunicator(StreamConnection streamConnection) {
    comunicator = new Thread(this);
    this.streamConnection = streamConnection;
  }
  public void start() {
    comunicator.start();
  }
  public void run() {
    System.out.println("Starting sendMessage...");
    try {
      OutputStream os = streamConnection.openOutputStream();
      System.out.println("Message stream created.");
      while (true) {
        if (received) {
          /*if (timerStarted) {
            timerStarted = false;
            timer.cancel();
            System.out.println("Timer cancellato: ne creo uno nuovo");
            this.timer = new Timer();
          }*/
          //System.out.println("Abbiamo ricevuto i dati, quindi passiamo alla prossima istruzione...");
          if (commands.size() != 0) {
            command = commands.get(0);
            commands.remove(0);
            /*os.write(command.getBytes());*/
            Thread.sleep(70);
            os.write(command);
            String str = "";
            for (int i = 0; i < command.length; i++) {
              if (i == 0) {
                int a = (int) command[i] + 256;
                str += a + " ";
              } else {
                str += (int) command[i] + " ";
              }
            }
            System.out.println("Message written: " + str);
            //running timer task as daemon thread
            /*timer.schedule(new TimerTask() {
              @Override
              public void run() {
                if (!received) {
                  System.out.println("Tempo scaduto per la ricezione del messaggio!");
                  stop("unexpected", command);
                } else {
                  System.out.println("Tempo scaduto ma abbiamo ricevuto i dati");
                }
                timerStarted = false;
              }
            }, 10000);
            timerStarted = true;
            System.out.println("Timer partito!");
            received = false;*/
            /*if (command.equalsIgnoreCase("quit\n")) {
              os.close();
              System.out.println("Comando corrisponde a quit");
              byte[] b = {1};
              stop("expected", b);
            }*/
          } else {
            //System.out.println("Nessun comando trovato, aggiungo 1s di ritardo");
            Thread.sleep(70);
          }
        } else {
          //System.out.println("Aspetto risposta... received: " + received);
        }
      }
    } catch (IOException e) {
      System.out.println(e);
    } catch (InterruptedException e) {
      if (!Main.bluetoothStopped) {
        System.out.println("InterruptedException" + e);
      }
    }
  }
  public void stop(String status, byte[] command) {
    try {
      if (status.equals("expected")) {
        System.out.println("SIGTERM ricevuto!");
        streamConnection.close();
        System.out.println("sreamConnection chiuso per comando: quit");
        System.out.println("Tentativo di terminazione del programma...");
      } else {
        System.out.println("Non abbiamo ricevuto dati");
        commands.add(0, command);
        received = true;
        return;
      }
      System.exit(1); //0 o nulla tira un'eccezione
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
