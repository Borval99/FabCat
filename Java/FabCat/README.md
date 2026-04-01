# OpenCat

Per fare partire il tutto da windows con jdk 8+ e javafx 8+ installato in \path\to\javafx\:

set PATH_TO_FX="\path\to\javafx"

javac -cp bluecove-2.1.1.jar;. --module-path %PATH_TO_FX% --add-modules javafx.controls Main.java

java -cp bluecove-2.1.1.jar;. --module-path %PATH_TO_FX% --add-modules javafx.controls Main

Per fare partire il tutto da windows con jdk 8:

javac -cp bluecove-2.1.1.jar;.\ Main.java

java -cp bluecove-2.1.1.jar;.\ Main

Per compilare un jar (**solo jdk e jre 8**):

javac -cp bluecove-2.1.1.jar;.\ Main.java

jar cvfm NomeJar.jar manifest.txt *.class

Per eseguire il Jar con la console (**solo jdk e jre 8**):

java -jar NomeJar.jar

**IMPORTANTISSIMO**
se si usa linux:
sudo apt install libbluetooth* bluez* blueman
**DOPO**
assicurarsi che i driver funzionino (es. debian: sudo systemctl status bluetooth)
