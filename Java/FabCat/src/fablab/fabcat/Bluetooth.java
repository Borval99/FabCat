package fablab.fabcat;

import java.io.IOException;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.util.HashMap;
import java.util.ArrayList;

public class Bluetooth {
  //   Dispositivo trovato: Cat2019
  // HW: 001891D687F9
  // Dispositivo trovato: FabCat
  // HW: 98D331F74156
  //PER LINUX: sudo apt install libbluetooth* bluez* blueman
  public Comunicator comunicator;
  public MessageListener listener;
  StreamConnection streamConnection;
  Increment increment = null;
  Decrement decrement = null;
  public boolean connected = false;
  public boolean succeded = false;
  public boolean exitAfterDiscovery = false;
  public static boolean forceConnectionIfNotPaired = false;
  public static String btId = "";
  public static HashMap<String, String> devs = new HashMap<String, String>();
  public static boolean ignoreRdListNotNull = false;
  private static RemoteDevice[] rdList = null;
  public Bluetooth() {
    System.out.println("Rilevazione dispositivi bluetooth in corso... Attendere prego");
    try {
      if (Main.windowsOrLinux) {
        rdList = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
      } else {
        RemoteDeviceDiscovery rd = new RemoteDeviceDiscovery();
        devs = rd.startDiscovery();
        if (devs.isEmpty()) {
          System.out.println("Hai installato i driver e acceso il bluetooth? (controllare che il programma abbia i permessi di accedere al bluetooth)");
        }
      }
      System.out.println("Lista raw completata!");
      if (((Main.windowsOrLinux && rdList == null) || (!Main.windowsOrLinux && devs.size() == 0)) && !forceConnectionIfNotPaired) {
        System.out.println("Nessun dispositivo bluetooth associato!");
        System.out.println("Per connetterti devi avere almeno 1 dispositivo bluetooth associato.");
        exitAfterDiscovery = true;
      } else if (!forceConnectionIfNotPaired && Main.windowsOrLinux) {
        for (int i = 0; i < rdList.length; i++) {
          System.out.println("Dispositivo trovato: " + rdList[i].getFriendlyName(false));
          System.out.println("HW: " + rdList[i]);
          devs.put(rdList[i].getFriendlyName(false), "" + rdList[i]);
        }
      } else if (forceConnectionIfNotPaired) {
        System.out.println("Nessun dispotivo trovato: Ignoro.");
        if ((Main.windowsOrLinux && rdList != null) || (!Main.windowsOrLinux && devs.size() != 0)) {
          ignoreRdListNotNull = true;
        }
      }
      Main.bluetoothStopped = false;
     } catch (IOException e) {
       System.out.println("IOException: " + e);
     }
     System.out.println("Avvio GUI...");
  }
  public void send(byte b1, byte b2, byte b3) {
    if (connected) {
      byte[] command = {b1,b2,b3};
      comunicator.commands.add(command);
    } else {
      System.out.println("Non connesso!");
    }
  }
  public void send(byte b1, byte b2) {
    if (connected) {
      byte[] command = {b1,b2};
      comunicator.commands.add(command);
    } else {
      System.out.println("Non connesso!");
    }
  }
  public void send(byte b1) {
    if (connected) {
      byte[] command = {b1};
      comunicator.commands.add(command);
    } else {
      System.out.println("Non connesso!");
    }
  }
  public void connect(String dev) {
    this.succeded = false;
    try {
      String bluetoothUrl;
      bluetoothUrl = "btspp://" + dev + ":1;authenticate=false;encrypt=false;master=false";
      System.out.println("Connessione a " + bluetoothUrl + " ...");
      streamConnection = (StreamConnection) Connector.open(bluetoothUrl);
      this.succeded = true;
      comunicator = new Comunicator(streamConnection);
      listener = new MessageListener(streamConnection, comunicator);
      listener.start();
      comunicator.start();
      connected = true;
    } catch (IOException e) {
      System.out.println(e);
    }
  }
  public boolean connected() {
    return this.connected;
  }
}
