import java.io.IOException;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import com.fazecast.jSerialComm.SerialPort;

public class portDiscovery {
  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.println(colori.underline + colori.blue + "Il programma supporta Windows e sistemi a base Linux, quale si sta usando?" + colori.nocolor);
    System.out.println(colori.bold + colori.violet + "[1]" + colori.blue + " Windows");
    System.out.println(colori.violet + "[2]" + colori.blue + " Linux");
    System.out.println(colori.violet + "[3]" + colori.blue + " Annulla" + colori.green);
    boolean redo = true;
    String os;
    while (redo) {
      System.out.print("Quale sistema operativo stai usando? [1/2/3]: " + colori.nocolor);
      os = scanner.nextLine();
      if (os.equals("1")) {
        os = "Windows";
        cerca(os);
        redo = false;
      } else if (os.equals("2")){
        os = "Linux";
        String user = System.getProperty("user.name");
        System.out.println(colori.blue + colori.bold + "Utente corrente: " + user + colori.nocolor);
        if (user.equals("root")) {
          /******* se l'utente e' root *******/
          cerca(os);
        } else {
          System.out.println(colori.red + colori.underline + "Il programma non ha privilegi di root, e nella magior parte dei casi non ha il permesso di vedere le porte seriali." + colori.green);
          System.out.println("[1] Forza l'esecuzione del programma");
          System.out.println("[2] Chiudi per eseguire come root" + colori.nocolor);
          redo = true;
          while (redo) {
            System.out.print("Scegli un'opzione [1/2]: ");
            String scelta = scanner.nextLine();
            if (scelta.equals("1")) {
              cerca(os);
              redo = false;
            } else if (scelta.equals("2")) {
              redo = false;
            } else {
              System.out.println("Immetti un'opzione valida");
              redo = true;
            }
          }
        }
        redo = false;
      } else if (os.equals("3")) {
        redo = false;
        return;
      } else {
        System.out.println(colori.underline + colori.red + "Immetti un valore valido [1/2/3]" + colori.nocolor);
        redo = true;
      }
    }
  }
  public static void cerca(String os) {
    if (os.equals("Linux")) {
      HashMap<String, String> portsMap = new HashMap<>();

      File serialFolder = new File("/dev/serial/by-id");
      File[] listOfDevices = serialFolder.listFiles();
      if (listOfDevices ==  null) {
        System.out.println("Nessun dispositivo trovato D:");
      } else {
        System.out.println(colori.bold + colori.blue + "Ottengo i risultati...");
        for (int i = 0; i < listOfDevices.length; i++) {
          if (Files.isSymbolicLink(listOfDevices[i].toPath())) {
            try {
              String portName = listOfDevices[i].getName();
              String id = Files
                .readSymbolicLink(listOfDevices[i].toPath())
                .toString()
                .substring(
                  Files.readSymbolicLink(listOfDevices[i].toPath())
                  .toString().lastIndexOf("/") + 1);
                  portsMap.put(id, portName);
                  System.out.println(colori.bold + colori.violet + "[" + i + "] " + colori.blue + "Nome: " + portName + colori.yellow + " Id: " + id);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        System.out.println("Fatto!" + colori.nocolor);
      }
    } else {
      SerialPort[] ports = SerialPort.getCommPorts();
      for (int i = 0; i < ports.length; i++) {
        System.out.println(ports[i]);
      }
    }
  }
}
