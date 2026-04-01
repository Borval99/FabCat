package fablab.fabcat;

import java.io.IOException;
import java.nio.file.Paths;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.stage.StageStyle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javafx.scene.control.ProgressBar;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import com.intel.bluetooth.BlueCoveImpl;

public class Main extends Application {
	  public static final int NMOTORS = 11;
	  private Bluetooth bluetooth;
	  boolean connectOrDisconnect = true;
	  public volatile static int motorValues[] = {90,90,90,20,20,20,20,140,140,140,140};
	  public static final int motorFunctions[][] = {
	    {0,0,0,0,0,0,0,0,0,0,0},
	    {90,90,90,50,50,50,50,120,120,120,120},
	    {90,90,90,80,80,80,80,112,112,112,112},
	    {90,90,90,90,90,90,90,90,90,90,90},
	    {90,90,90,80,80,80,80,40,40,40,40},
	    {90,150,120,20,18,20,20,168,170,168,168},
	    {90,90,90,50,50,50,50,120,120,120,120},
	    {90,90,90,50,50,50,50,120,120,120,120},
	    {90,90,90,50,50,50,50,120,120,120,120},
	    {90,90,90,50,50,50,50,120,120,120,120},
	    {90,90,90,50,50,50,50,120,120,120,120}
	  };
	  public static final byte MOTORCMD = (byte) 220;
	  public static final byte FNCMD = (byte) 221;
	  public static Label names[] = new Label[NMOTORS];
	  public static Label labels[] = new Label[NMOTORS];
	  public static Button buttons[][] = new Button[NMOTORS][2];
	  public static GridPane motorGrids[] = new GridPane[NMOTORS];
	  public static final int positions[][] = {{0, 1}, {0, 2}, {0, 3}, {1, 1}, {1, 0}, {1, 3}, {1, 4}, {2, 1}, {2, 0}, {2, 3}, {2, 4}};
	  public static final int PLUS = 0;
	  public static final int MINUS = 1;
	  public static final int OFF = 0;
	  public static final int ON = 1;
	  public static Button bottomButtons[] = new Button[5];
	  public static Button moreBottomButtons[] = new Button[5];
	  public static DoubleProperty barUpdater = null;
	  public static StringProperty labelUpdater = null;
	  public static final int NTOGGLE = 2;
	  public static ToggleButton toggleButtons[] = new ToggleButton[NTOGGLE];
	  public static final int toggleButtonsValues[][] = {{0, 1}, {10, 11}};
	  public static TextField numeroAltreFunzioni = null;
	  public static StringProperty pitchValUpdater = new SimpleStringProperty("OFF");
	  public static StringProperty rollValUpdater = new SimpleStringProperty("OFF");
		public static StringProperty speechRecBtnUpdater = new SimpleStringProperty(new String(Character.toChars(0x1F3A4)) + " Comando Vocale");
		public static StringProperty warningUpdater = new SimpleStringProperty("");
		public static boolean setPitchRoll = false;
	  public static int multiplier = 1;
	  public static boolean bluetoothStopped = false;
	  private static boolean autoBalanceCheck = false;
	  private static boolean raddrizzamentoAutomatico = false;
	  private static ToggleButton autoBalance = null;
		public static boolean windowsOrLinux;
		public static Label l = null;
		public static Label warning = null;
	  public void start(Stage primaryStage) {
	    GridPane g = new GridPane();
	    try {
	      System.out.println(Paths.get("").toAbsolutePath().toString());
	      Image image = new Image(new FileInputStream("OpenCatPallinatoMotori_senzaSfondo.png"));
	      System.out.println("IMG1T");
	      ImageView imageView = new ImageView(image);
	      imageView.setFitHeight(500);
	      imageView.setFitWidth(500);
	      imageView.setPreserveRatio(true);
	      g.add(imageView, 0, 0);
	    } catch (FileNotFoundException e) {
	      System.out.println(e);
	    }
	    ProgressBar pb = new ProgressBar();
	    double pct = 0.1;
	    barUpdater = new SimpleDoubleProperty(pct);
	    pb.progressProperty().bind(barUpdater);
	    pb.prefWidthProperty().bind(primaryStage.widthProperty().subtract(20));
	    pb.setStyle("-fx-control-inner-background: #404040;-fx-text-box-border: #404040; -fx-accent: white;");
	    g.add(pb, 0, 1);
	    System.out.println("Font disponibili:");
	    System.out.println(javafx.scene.text.Font.getFamilies());
	    Label log = new Label();
	    log.setFont(Font.font("Impact", FontWeight.SEMI_BOLD, FontPosture.REGULAR, 15));
	    labelUpdater = new SimpleStringProperty("Impostazione struttura...");
	    log.textProperty().bind(labelUpdater);
	    log.setStyle("-fx-background-color: transparent;");
	    log.setTextFill(Color.WHITESMOKE);
	    g.add(log, 0,2);
	    BorderPane pane = new BorderPane();
	    pane.setPadding(new Insets(5,5,5,5));
	    pane.setStyle("-fx-background-color: #404040;");
	    g.setStyle("-fx-background-color: #404040;");
	    pane.setCenter(g);
	    Scene preload = new Scene(pane);
	    primaryStage.setScene(preload);
	    primaryStage.setResizable(false);
	    primaryStage.initStyle(StageStyle.UNDECORATED);
	    primaryStage.getIcons().add(new Image("file:makerStation.jpg"));
	    primaryStage.show();
	    Platform.runLater(new Runnable() {
	      @Override public void run() {
	        barUpdater.set(0.1);
	        labelUpdater.set("Caricamento immagine...");
	        Stage secondaryStage = new Stage();
	        System.out.println("Impostazione struttura...");
	        secondaryStage.setTitle("OpenCat");
	        BorderPane motorButtons = new BorderPane();
	        motorButtons.setPadding(new Insets(5,5,5,5));
	        Scene scene = new Scene(motorButtons);
	        GridPane grid = new GridPane();
	        GridPane superExtraGrid = new GridPane();
	        GridPane superGrid = new GridPane();
	        grid.setPadding(new Insets(5,5,5,5));
	        grid.setHgap(5);
	        grid.setVgap(5);
	        Platform.runLater(new Runnable() {
	          @Override public void run() {
	            barUpdater.set(0.2);
	            labelUpdater.set("Creazione lista dispositivi e aggiunta pulsante di connessione...");
	            System.out.println("Caricamento immagine...");
	            try {
	              Image image = new Image(new FileInputStream("OpenCatPallinatoMotori_senzaSfondo.png"));
	              ImageView imageView = new ImageView(image);
	              imageView.setFitHeight(500);
	              imageView.setFitWidth(450);
	              imageView.setPreserveRatio(true);
	              imageView.setStyle("-fx-border-width:1px;-fx-border-color:silver;-fx-background-radius: 5;");
	              superExtraGrid.add(imageView, 1, 0);
	            } catch (FileNotFoundException e) {
	              System.out.println(e);
	            }
	            Platform.runLater(new Runnable() {
	              @Override public void run() {
	                bluetooth = new Bluetooth();

	                if (!bluetooth.exitAfterDiscovery) {
	                  barUpdater.set(0.3);
	                  labelUpdater.set("Aggiunta pulsanti motori...");
	                  System.out.println("Creazione lista dispositivi e aggiunta pulsante di connessione...");
	                } else {
	                  barUpdater.set(-1);
	                  labelUpdater.set("Associa almeno 1 dispositivo per connetterti!");
	                }
									ArrayList<String> devNames = new ArrayList<String>(Bluetooth.devs.keySet());
		              ComboBox<String> devices = new ComboBox<String>(FXCollections.observableArrayList(devNames));
									//java li vuole qui

									if (!Bluetooth.forceConnectionIfNotPaired) {
										grid.add(devices, 5, 0);
										System.out.println("Devices applicabile!");
								  } else {
										l = new Label("Non applicabile");
										if (windowsOrLinux) {
											l.setPrefWidth(100);
										} else {
											l.setPrefWidth(120);
										}
										grid.add(l, 5, 0);
										System.out.println("Devices non applicabile");
									}
	                StringProperty statusUpdater = new SimpleStringProperty("Bluetooth: non connesso");
	                Label status = new Label();
	                status.textProperty().bind(statusUpdater);
	                status.setPadding(new Insets(0,5,5,0));
	                status.setFont(Font.font("Gadugi", FontWeight.SEMI_BOLD, FontPosture.REGULAR, 13));

	                Button connect = new Button("Connect " + new String(Character.toChars(0x1F431)));
	                connect.setOnMousePressed((event) -> {
	                  if (connectOrDisconnect) {
	                    System.out.println("Selezione: " + devices.getValue());
	                  }
	                  if (devices.getValue() != null && connectOrDisconnect) {
	                    if (connectOrDisconnect) {
	                      connectOrDisconnect = false;
	                      bluetooth.connect(Bluetooth.devs.get(devices.getValue()));
	                      if (bluetooth.succeded) {
	                        connect.setText("Disconnect " + new String(Character.toChars(0x1F480)));
	                        System.out.println("STRC succeded");
	                        status.setTextFill(Color.GREEN);
	                        statusUpdater.set("Bluetooth: connesso " + new String(Character.toChars(0x1F431)));
	                      } else {
	                        connectOrDisconnect = true;
	                        connect.setText("Retry " + new String(Character.toChars(0x1F431)));
	                        System.out.println("STRC fail: stack ^");
	                        status.setTextFill(Color.RED);
	                        statusUpdater.set("Bluetooth: connessione fallita " + new String(Character.toChars(0x1F480)));
	                      }
	                    } else {
	                      if (setPitchRoll) {
	                        System.out.println("Il pitch roll e' attivo allo spegnimento: disattivazione...");
	                        setPitchRoll = false;
	                        System.out.println("Pitchroll: disattivato");
	                        pitchValUpdater.set("OFF");
	                        rollValUpdater.set("OFF");
	                        bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][OFF]);
	                        toggleButtons[0].setSelected(false);
	                      }
	                      if (raddrizzamentoAutomatico) {
	                        System.out.println("Il raddrizzamento automatico e' attivo allo spegnimento: disattivazione...");
	                        raddrizzamentoAutomatico = false;
	                        System.out.println("Raddrizzamento automatico: attivo");
	                        bluetooth.send((byte) 222, (byte) toggleButtonsValues[1][OFF]);
	                        toggleButtons[1].setSelected(false);
	                      }
	                      if (autoBalanceCheck) {
	                        System.out.println("Autobalance attivo alla chiusura del programma: disattivo...");
	                        autoBalanceCheck = false;
	                        bluetooth.send((byte) 222, (byte) 20);
	                        System.out.println("Autobalance: disattivato");
	                        autoBalance.setSelected(false);
	                      }
	                      try {
	                        Thread.sleep(1000);
	                      } catch (InterruptedException e) {
	                        System.out.println(e);
	                      }
	                      bluetoothStopped = true;
	                      //prima perche'il listener va sempre e l'errore non � triggerato dall'interrupt
	                      BlueCoveImpl.shutdownThreadBluetoothStack();
	                      BlueCoveImpl.shutdown();
	                      bluetooth.connected = false;
	                      System.out.println("bluecove spento!");
	                      Comunicator.comunicator.interrupt();
	                      MessageListener.listener.interrupt();
	                      connectOrDisconnect = true;
	                      connect.setText("Connect " + new String(Character.toChars(0x1F431)));
	                      System.out.println("STRC succeded");
	                      status.setTextFill(Color.BLACK);
	                      statusUpdater.set("Bluetooth: non connesso " + new String(Character.toChars(0x1F480)));
	                    }
	                  } else if (connectOrDisconnect && !Bluetooth.forceConnectionIfNotPaired) {
	                    System.out.println("Selezionare un dispositivo valido!");
										} else if (Bluetooth.forceConnectionIfNotPaired){
											if (connectOrDisconnect) {
	                      connectOrDisconnect = false;
	                      bluetooth.connect(Bluetooth.btId);
	                      if (bluetooth.succeded) {
	                        connect.setText("Disconnect " + new String(Character.toChars(0x1F480)));
	                        System.out.println("STRC succeded");
	                        status.setTextFill(Color.GREEN);
	                        statusUpdater.set("Bluetooth: connesso " + new String(Character.toChars(0x1F431)));
	                      } else {
	                        connectOrDisconnect = true;
	                        connect.setText("Retry " + new String(Character.toChars(0x1F431)));
	                        System.out.println("STRC fail: stack ^");
	                        status.setTextFill(Color.RED);
	                        statusUpdater.set("Bluetooth: connessione fallita " + new String(Character.toChars(0x1F480)));
	                      }
	                    } else {
	                      if (setPitchRoll) {
	                        System.out.println("Il pitch roll e' attivo allo spegnimento: disattivazione...");
	                        setPitchRoll = false;
	                        System.out.println("Pitchroll: disattivato");
	                        pitchValUpdater.set("OFF");
	                        rollValUpdater.set("OFF");
	                        bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][OFF]);
	                        toggleButtons[0].setSelected(false);
	                      }
	                      if (raddrizzamentoAutomatico) {
	                        System.out.println("Il raddrizzamento automatico e' attivo allo spegnimento: disattivazione...");
	                        raddrizzamentoAutomatico = false;
	                        System.out.println("Raddrizzamento automatico: attivo");
	                        bluetooth.send((byte) 222, (byte) toggleButtonsValues[1][OFF]);
	                        toggleButtons[1].setSelected(false);
	                      }
	                      if (autoBalanceCheck) {
	                        System.out.println("Autobalance attivo alla chiusura del programma: disattivo...");
	                        autoBalanceCheck = false;
	                        bluetooth.send((byte) 222, (byte) 20);
	                        System.out.println("Autobalance: disattivato");
	                        autoBalance.setSelected(false);
	                      }
	                      try {
	                        Thread.sleep(1000);
	                      } catch (InterruptedException e) {
	                        System.out.println(e);
	                      }
	                      bluetoothStopped = true;
	                      //prima perche'il listener va sempre e l'errore non � triggerato dall'interrupt
	                      BlueCoveImpl.shutdownThreadBluetoothStack();
	                      BlueCoveImpl.shutdown();
	                      bluetooth.connected = false;
	                      System.out.println("bluecove spento!");
	                      Comunicator.comunicator.interrupt();
	                      MessageListener.listener.interrupt();
	                      connectOrDisconnect = true;
	                      connect.setText("Connect " + new String(Character.toChars(0x1F431)));
	                      System.out.println("STRC succeded");
	                      status.setTextFill(Color.BLACK);
	                      statusUpdater.set("Bluetooth: non connesso " + new String(Character.toChars(0x1F480)));
	                    }
										} else {
	                    System.out.println("Disconnessione...");
	                    if (setPitchRoll) {
	                      System.out.println("Il pitch roll e' attivo allo spegnimento: disattivazione...");
	                      setPitchRoll = false;
	                      System.out.println("Pitchroll: disattivato");
	                      pitchValUpdater.set("OFF");
	                      rollValUpdater.set("OFF");
	                      bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][OFF]);
	                      toggleButtons[0].setSelected(false);
	                    }
	                    if (raddrizzamentoAutomatico) {
	                      System.out.println("Il raddrizzamento automatico e' attivo allo spegnimento: disattivazione...");
	                      raddrizzamentoAutomatico = false;
	                      System.out.println("Raddrizzamento automatico: attivo");
	                      bluetooth.send((byte) 222, (byte) toggleButtonsValues[1][OFF]);
	                      toggleButtons[1].setSelected(false);
	                    }
	                    if (autoBalanceCheck) {
	                      System.out.println("Autobalance attivo alla chiusura del programma: disattivo...");
	                      autoBalanceCheck = false;
	                      bluetooth.send((byte) 222, (byte) 20);
	                      System.out.println("Autobalance: disattivato");
	                      autoBalance.setSelected(false);
	                    }
	                    try {
	                      Thread.sleep(1000);
	                    } catch (InterruptedException e) {
	                      System.out.println(e);
	                    }
	                    bluetoothStopped = true;
	                    //prima perche'il listener va sempre e l'errore non � triggerato dall'interrupt
	                    BlueCoveImpl.shutdownThreadBluetoothStack();
	                    BlueCoveImpl.shutdown();
	                    bluetooth.connected = false;
	                    System.out.println("bluecove spento!");
	                    Comunicator.comunicator.interrupt();
	                    MessageListener.listener.interrupt();
	                    connectOrDisconnect = true;
	                    connect.setText("Connect " + new String(Character.toChars(0x1F431)));
	                    System.out.println("STRC succeded");
	                    status.setTextFill(Color.BLACK);
	                    statusUpdater.set("Bluetooth: non connesso " + new String(Character.toChars(0x1F480)));
	                  }
	                });
	                grid.add(connect, 4, 0);
	                Platform.runLater(new Runnable() {
	                  @Override public void run() {
	                    if (bluetooth.exitAfterDiscovery) {
	                      try {
	                        Thread.sleep(2000);
	                      } catch (InterruptedException e) {
	                        System.out.println(e);
	                      }
	                      System.exit(1);
	                    }
	                    barUpdater.set(0.5);
	                    labelUpdater.set("Aggiunta pulsanti funzioni 221...");
	                    System.out.println("Aggiunta pulsanti motori...");
	                    for(int i=0;i<NMOTORS;i++){
	                      final int k = i;
	                      names[i] = new Label("Motor "+i);
	                      labels[i] = new Label(""+motorValues[i]);
	                      buttons[i][PLUS] = new Button("+");
	                      buttons[i][PLUS].setId("smallbutton");
	                      buttons[i][PLUS].setOnMousePressed((event) -> {
	                        if (bluetooth.connected()) {
	                          bluetooth.increment = new Increment(bluetooth, k);
	                          bluetooth.increment.start();
	                        }
	                      });
	                      buttons[i][PLUS].setOnMouseReleased((event) -> {
	                        if (bluetooth.connected()) {
	                          motorValues[k] = bluetooth.increment.stop();
	                        } else {
	                          System.out.println("Non connesso");
	                        }
	                      });
	                      buttons[i][MINUS] = new Button("- ");
	                      buttons[i][MINUS].setId("smallbutton");
	                      buttons[i][MINUS].setOnMousePressed((event) -> {
	                        if (bluetooth.connected()) {
	                          bluetooth.decrement = new Decrement(bluetooth, k);
	                          bluetooth.decrement.start();
	                        } else {
	                          System.out.println("Non connesso!");
	                        }
	                      });
	                      buttons[i][MINUS].setOnMouseReleased((event) -> {
	                        if (bluetooth.connected()) {
	                          motorValues[k] = bluetooth.decrement.stop();
	                        } else {
	                          System.out.println("Non connesso");
	                        }
	                      });
	                      motorGrids[i] = new GridPane();
	                      motorGrids[i].setPadding(new Insets(5,5,5,5));
	                      motorGrids[i].setHgap(5);
	                      motorGrids[i].setVgap(5);
	                      motorGrids[i].add(names[i], 0, 0);
	                      motorGrids[i].add(labels[i], 0, 1);
	                      motorGrids[i].add(buttons[i][PLUS], 1, 0);
	                      motorGrids[i].add(buttons[i][MINUS], 1, 1);
	                      grid.add(motorGrids[i], positions[i][1], positions[i][0]);
	                    }
	                    Platform.runLater(new Runnable() {
	                      @Override public void run() {
	                        barUpdater.set(0.6);
	                        labelUpdater.set("Creazione griglie...");
	                        System.out.println("Aggiunta pulsanti funzioni 221...");
	                        for (int i = 0; i < 5; i++) {
	                          final int k = i;
	                          bottomButtons[i] = new Button("");
	                          bottomButtons[i].setId("bigbutton");
	                          bottomButtons[i].setOnMousePressed((event) -> {
	                            if (bluetooth.connected()) {
	                              bluetooth.send(FNCMD, (byte) k);
	                              for(int j=0;j<NMOTORS;j++) {
	                                motorValues[j] = motorFunctions[k][j];
	                                labels[j].setText(""+motorValues[j]);
	                              }
	                            } else {
	                              System.out.println("Non connesso!");
	                            }
	                          });
	                          grid.add(bottomButtons[i], i, 4);
	                        }
	                        for (int i = 0; i < 5; i++) {
	                          final int k = i;
	                          moreBottomButtons[i] = new Button("");
	                          moreBottomButtons[i].setId("bigbutton");
	                          moreBottomButtons[i].setOnMousePressed((event) -> {
	                            if (bluetooth.connected()) {
	                              bluetooth.send(FNCMD, (byte) (5+k));
	                              for(int j=0;j<NMOTORS;j++) {
	                                motorValues[j] = motorFunctions[k+5][j];
	                                labels[j].setText(""+motorValues[j]);
	                              }
	                            } else {
	                              System.out.println("Non connesso!");
	                            }
	                          });
	                          grid.add(moreBottomButtons[i], i, 5);
	                        }

	                        bottomButtons[0].setText("Sciogli");
	                        bottomButtons[1].setText("Start");
	                        bottomButtons[2].setText("Calib.");
	                        bottomButtons[3].setText("221 - 3");
	                        bottomButtons[4].setText("221 - 4");

	                        moreBottomButtons[0].setText("221 - 5");
	                        moreBottomButtons[1].setText("221 - 6");
	                        moreBottomButtons[2].setText("221 - 7");
	                        moreBottomButtons[3].setText("221 - 8");
	                        moreBottomButtons[4].setText("221 - 9");

	                        Button altreFunzioni = new Button("Altra funzione");
	                        numeroAltreFunzioni = new TextField();
	                        numeroAltreFunzioni.prefWidthProperty().bind(altreFunzioni.widthProperty());
	                        grid.add(numeroAltreFunzioni, 5, 5);
	                        altreFunzioni.setOnMousePressed((event) -> {
	                          if (bluetooth.connected() && numeroAltreFunzioni.getText() != "") {
	                            bluetooth.send((byte) 221, (byte) Integer.parseInt(Main.numeroAltreFunzioni.getText()));
	                          } else {
	                            System.out.println("Non connesso!");
	                          }
	                        });
	                        altreFunzioni.prefWidthProperty().bind(numeroAltreFunzioni.widthProperty());
													grid.add(altreFunzioni, 5, 4);

	                        superGrid.add(status, 0, 1);
	                        Platform.runLater(new Runnable() {
	                          @Override public void run() {
	                            barUpdater.set(0.7);
	                            labelUpdater.set("Impostazione griglie...");
	                            System.out.println("Creazione griglie...");
	                            GridPane toggleGrid = new GridPane();
	                            toggleGrid.setPadding(new Insets(5,5,5,5));
	                            toggleGrid.setPrefWidth(200);
	                            TextField pitchRollDelay = new TextField("200");
	                            toggleButtons[0] = new ToggleButton("");
	                            toggleButtons[0].setOnAction(event -> {
	                              if (toggleButtons[0].isSelected()) {
	                                raddrizzamentoAutomatico = true;
	                                System.out.println("Raddrizzamento automatico: attivo");
	                                bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][ON]);
	                              } else {
	                                raddrizzamentoAutomatico = false;
	                                System.out.println("Raddrizzamento automatico: non attivo");
	                                bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][OFF]);
	                              }
	                            });
	                            toggleButtons[0].prefWidthProperty().bind(toggleGrid.widthProperty());
	                            toggleButtons[0].setText("Pitch/Roll");
	                            toggleButtons[0].setOnAction(event -> {
	                              if (toggleButtons[0].isSelected()) {
	                                byte delay = (byte) Math.round(Integer.parseInt(pitchRollDelay.getText())/25);
	                                if (delay == (byte) 0) {
	                                  delay = (byte) 1;
	                                }
	                                setPitchRoll = true;
	                                System.out.println("Pitchroll: attivato");
	                                bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][ON], delay);
	                              } else {
	                                setPitchRoll = false;
	                                System.out.println("Pitchroll: disattivato");
	                                pitchValUpdater.set("OFF");
	                                rollValUpdater.set("OFF");
	                                bluetooth.send((byte) 222, (byte) toggleButtonsValues[0][OFF]);
	                              }
	                            });
	                            toggleButtons[1] = new ToggleButton("");
	                            toggleButtons[1].setOnAction(event -> {
	                              if (toggleButtons[1].isSelected()) {
	                                raddrizzamentoAutomatico = true;
	                                System.out.println("Raddrizzamento automatico: attivo");
	                                bluetooth.send((byte) 222, (byte) toggleButtonsValues[1][ON]);
	                              } else {
	                                raddrizzamentoAutomatico = false;
	                                System.out.println("Raddrizzamento automatico: non attivo");
	                                bluetooth.send((byte) 222, (byte) toggleButtonsValues[1][OFF]);
	                              }
	                            });
	                            toggleButtons[1].prefWidthProperty().bind(toggleGrid.widthProperty());

	                            toggleButtons[1].setText("Raddrizzamento automatico");
	                            ToggleGroup multiplierGroup = new ToggleGroup();
	                            RadioButton button1x = new RadioButton("1x");
	                            button1x.setToggleGroup(multiplierGroup);
	                            button1x.setSelected(true);
	                            button1x.setPadding(new Insets(0,5,5,5));
	                            button1x.setOnMousePressed((event) -> {
	                              multiplier = 1;
	                            });
	                            RadioButton button2x = new RadioButton("2x");
	                            button2x.setToggleGroup(multiplierGroup);
	                            button2x.setPadding(new Insets(0,5,5,5));
	                            button2x.setOnMousePressed((event) -> {
	                              multiplier = 2;
	                            });
	                            RadioButton button4x = new RadioButton("4x");
	                            button4x.setToggleGroup(multiplierGroup);
	                            button4x.setPadding(new Insets(0,5,5,5));
	                            button4x.setOnMousePressed((event) -> {
	                              multiplier = 4;
	                            });
	                            GridPane radioGrid = new GridPane();
	                            radioGrid.add(button1x, 0, 0);
	                            radioGrid.add(button2x, 1, 0);
	                            radioGrid.add(button4x, 2, 0);
	                            toggleGrid.add(radioGrid, 0, 0);
	                            GridPane pitchroll = new GridPane();
	                            pitchroll.add(toggleButtons[0], 0, 0);
	                            pitchroll.add(pitchRollDelay, 1, 0);
	                            toggleGrid.add(pitchroll, 0, 1);
	                            Label pitch = new Label ("Pitch: ");
	                            Label pitchVal = new Label ();
	                            pitchVal.textProperty().bind(pitchValUpdater);
	                            Label roll = new Label ("Roll: ");
	                            Label rollVal = new Label();
	                            rollVal.textProperty().bind(rollValUpdater);
	                            Label space = new Label("         ");
	                            GridPane pitrol = new GridPane();
	                            pitrol.add(pitch, 0, 0);
	                            pitrol.add(pitchVal, 1, 0);
	                            pitrol.add(space, 2, 0);
	                            pitrol.add(roll, 3, 0);
	                            pitrol.add(rollVal, 4, 0);
	                            toggleGrid.add(pitrol, 0, 2);
	                            toggleGrid.add(toggleButtons[1], 0, 3);
	                            autoBalance = new ToggleButton("Auto bilanciamento");
	                            autoBalance.setOnMousePressed((event) -> {
	                              if (autoBalance.isSelected()) {
	                                bluetooth.send((byte) 222, (byte) 20);
	                                autoBalanceCheck = false;
	                                System.out.println("Autobilanciamento: non attivo");
	                              } else {
	                                bluetooth.send((byte) 222, (byte) 21);
	                                autoBalanceCheck = true;
	                                System.out.println("Autobilanciamento: attivo");
	                              }
	                            });
	                            autoBalance.prefWidthProperty().bind(toggleGrid.widthProperty());
	                            toggleGrid.add(autoBalance, 0, 4);

	                            Button meow = new Button("Meow " + new String(Character.toChars(0x1F431)));
	                            meow.prefWidthProperty().bind(toggleGrid.widthProperty());
	                            meow.setOnMousePressed((event) -> {
	                              bluetooth.send((byte) 222, (byte) 30);
	                              System.out.println("Meow :)");
	                            });
	                            toggleGrid.add(meow, 0, 5);

															Button speechRec = new Button(new String(Character.toChars(0x1F3A4)) + " Comando Vocale");
															speechRec.setOnMousePressed((event) -> {
																if (bluetooth.connected) {
		                              System.out.println("Avvio processo...");
																	SpeechRecognition sr = new SpeechRecognition(bluetooth);
																	sr.start();
																} else {
																	System.out.println("Non connesso!");
																}
															});
															speechRec.prefWidthProperty().bind(toggleGrid.widthProperty());
															speechRec.textProperty().bind(speechRecBtnUpdater);
															toggleGrid.add(speechRec, 0, 6);

															if (Bluetooth.ignoreRdListNotNull) {
																warning = new Label();
																warning.textProperty().bind(warningUpdater);
																if (windowsOrLinux) {
																	warningUpdater.set("Avviso: Hai forzato la connessione ma hai dei dispositivi associati.");
																} else {
																	warningUpdater.set("Avviso: Hai forzato la connessione ma sono stati trovati dei dispositivi.");
																}
																warning.setTextFill(Color.DARKBLUE);
														  	warning.setPadding(new Insets(0,5,5,0));
																warning.setFont(Font.font("Gadugi", FontWeight.SEMI_BOLD, FontPosture.REGULAR, 13));
																superGrid.add(warning, 1, 1);
															}
	                            toggleGrid.setStyle("-fx-border-width:1px;-fx-border-color:silver;-fx-background-radius: 5;");
	                            Platform.runLater(new Runnable() {
	                              @Override public void run() {
	                                barUpdater.set(0.8);
	                                labelUpdater.set("Quasi fatto...");
	                                System.out.println("Impostazione griglie...");
	                                superGrid.add(toggleGrid, 0, 0);
	                                superGrid.add(grid, 1, 0);
	                                superExtraGrid.add(superGrid, 0, 0);
	                                motorButtons.setCenter(superExtraGrid);
	                                secondaryStage.resizableProperty().setValue(Boolean.FALSE);
	                                scene.getStylesheets().add("file:main.css");
	                                Platform.runLater(new Runnable() {
	                                  @Override public void run() {
	                                    barUpdater.set(0.9);
	                                    System.out.println("Quasi fatto...");
	                                    Platform.runLater(new Runnable() {
	                                      @Override public void run() {
	                                        try {
	                                          Thread.sleep(1000);
	                                        } catch (InterruptedException e) {
	                                          System.out.println(e);
	                                        }
	                                        secondaryStage.setScene(scene);
	                                        primaryStage.close();
	                                        secondaryStage.getIcons().add(new Image("file:makerStation.jpg"));
	                                        secondaryStage.show();
	                                        secondaryStage.setOnCloseRequest(event -> {
	                                          System.exit(1);
	                                        });
	                                      }
	                                    });
	                                    Platform.runLater(new Runnable() {
	                                      @Override public void run() {
	                                        barUpdater.set(1.0);
	                                        new Launch();
	                                      }
	                                    });
	                                  }
	                                });
	                              }
	                            });
	                          }
	                        });
	                      }
	                    });
	                  }
	                });
	              }
	            });
	          }
	        });
	        try {
	          Thread.sleep(1300);
	        } catch (InterruptedException e) {
	          System.out.println(e);
	        }
	      }
	    });
	  }

	  public static void main(String[] args) throws IOException {
			try {
				if (!(args[0].equals("false"))) {
					Bluetooth.forceConnectionIfNotPaired = true;
					Bluetooth.btId = args[1];
				}
				System.out.println("argomento 0: " + args[0]);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Il programma necessita di due argomenti! Uscita...");
				System.exit(1);
			}
			String os = System.getProperty("os.name");
			System.out.println("SO rilevato: " + os);
			if (os.equalsIgnoreCase("windows 10")) {
				windowsOrLinux = true;
			} else {
				windowsOrLinux = false;
			}
	    launch(args);
	  }
	}
