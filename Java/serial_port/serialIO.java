import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortDataListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class serialIO {
  SerialPort port = null;

  public void bindPort(String inputPort) {
    port = SerialPort.getCommPort(inputPort);
    port.setComPortParameters(9600, 8, 1, 0);
    port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
    if (port.openPort()) {
      System.out.println("Connessione alla porta eseguita con successo :D");
    } else {
      System.out.println("Errore nella connessione e apertura nella porta D:");
    }
  }

  public void send(String arg) throws IOException {
    port.addDataListener(new SerialPortDataListener() {
    @Override
    public int getListeningEvents() {
      return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }
    @Override
    public void serialEvent(SerialPortEvent event) {
      byte[] newData = event.getReceivedData();
      for (int i = 0; i < newData.length; i++) {
        char carattere = (char)newData[i];
        System.out.print(carattere);
      }
    }
    });
    PrintWriter out = new PrintWriter(port.getOutputStream(), true);

    out.println(arg);
    out.flush();
  }
}
