package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Date;

public class Main extends Application {
    private SerialPort serialPort;
    private Thread emulationThread;
    /**
     * Символ конца строки
     */
    private final byte DELIMITER = 0x0A;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        emulationThread = new Thread(() -> {
            int weight = 1000;
            float speed = 0.0F;
            float flow = 0.0F;
            try {
                while (!Thread.interrupted()) {
                    weight += (int) (Math.random() * 10);
                    speed += 2 * Math.random() - 1;
                    speed = Math.abs(speed);
                    flow += 2 * Math.random() - 1;
                    flow = Math.abs(flow);
                    StringBuilder command = new StringBuilder();
                    command.append(":");
                    command.append(weight).append(speed).append(flow);
                    if(serialPort != null)
                        serialPort.writeBytes(command.toString().getBytes(),command.length());
                    Thread.sleep(500);
                }
                System.out.println("Producer");
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        });
        emulationThread.start();

        refreshPort();
    }

    public void refreshPort() {
        if (serialPort != null) {
            serialPort.closePort();
            serialPort = null;
        }
        SerialPort[] ports = SerialPort.getCommPorts();
        String portName = "COM5";
        int speedPort = 9600;

        for (SerialPort port: ports){
            String str = port.getSystemPortName();
            if(port.getSystemPortName().equals(portName)){
                serialPort = port;
                serialPort.setBaudRate(speedPort);
                if(serialPort.openPort()){
                    serialPort.addDataListener(serialPortMessageListener);
                }
                break;
            }
        }

    }

    private SerialPortMessageListener serialPortMessageListener = new SerialPortMessageListener() {

        @Override
        public byte[] getMessageDelimiter() {
            return new byte[] {DELIMITER};
        }

        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return true;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {

        }
    };

    public static void main(String[] args) {
        launch(args);
    }
}
