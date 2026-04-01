package fablab.fabcat;

import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.*;
import java.util.HashMap;

public class RemoteDeviceDiscovery {
    private static HashMap<String, String> devs = new HashMap<String, String>();

    public static HashMap<String, String> startDiscovery() {
      final Object inquiryCompletedEvent = new Object();

      DiscoveryListener listener = new DiscoveryListener() {
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            System.out.println("Dispositivo trovato HW: " + btDevice.getBluetoothAddress());
            try {
                System.out.println(" Friendly name:" + btDevice.getFriendlyName(false));
                devs.put(btDevice.getFriendlyName(false), "" + btDevice.getBluetoothAddress());
            } catch (IOException cantGetDeviceName) {
              System.out.println("**ERRORE**: nome di " + btDevice.getBluetoothAddress() + " non trovato.");
              System.exit(1);
            }
        }

        public void inquiryCompleted(int discType) {
            System.out.println("Scansione completata!");
            synchronized(inquiryCompletedEvent){
                inquiryCompletedEvent.notifyAll();
            }
            System.out.println(devs.size() + " dispositivo(i) trovato(i)");
        }

        public void serviceSearchCompleted(int transID, int respCode) {
        }

        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        }
    };
    synchronized(inquiryCompletedEvent) {
      boolean started = false;
      try {
        started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
      } catch (IOException e) {
        System.out.println(e);
      }
      if (started) {
          System.out.println("In attesa della scansione...");
          try {
            inquiryCompletedEvent.wait();
          } catch (InterruptedException e) {
            System.out.println(e);
          }
      }
    }
    return devs;
  }
}
