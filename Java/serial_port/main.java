import java.util.Scanner;
import java.io.IOException;
import java.lang.InterruptedException;


public class main {
  public static void main(String[] args) throws IOException, InterruptedException {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Inserisci la port a cui connetterti: ");
    String inputPort = scanner.nextLine();
    serialIO serial = new serialIO();
    serial.bindPort(inputPort);
    serial.send("1"); //la prima non la prende :/
    String phrase = null;
    while (phrase != "exit") {
      System.out.print("Numero: ");
      phrase = scanner.nextLine();
      serial.send(phrase);
      Thread.sleep(2000);
    }
  }
}
