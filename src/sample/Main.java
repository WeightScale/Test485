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

    private String intToHex(int int_, int length) {
        return String.format("%1$" + length + "s", Integer.toHexString(int_).toUpperCase()).replace(' ', '0');
    }

    private String byteToHex(int byte_) {
        if (byte_ < 0)
            byte_ += 256;
        return intToHex(byte_, 2);
    }

    private String intToHex(int int_) {
        return intToHex(int_, 8);
    }

    private String floatToHex(float float_) {
        int bits = Float.floatToIntBits(float_);
        String string = byteToHex((byte) (bits >> 16));
        return byteToHex((byte) (bits >> 24)) + byteToHex((byte) (bits >> 16)) + byteToHex((byte) (bits >> 8)) + byteToHex((byte) bits);
    }

    private byte lrc(String string) {
        byte lrc = 0;
        for (int i = 0; i < string.length(); i += 2) {
            byte b = (byte) Short.parseShort(string.substring(i, i + 2), 16);
            lrc += b;
        }
        return lrc;
    }

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

                    String weightString = intToHex(1985);
                    String speedString = floatToHex(23.65f);
                    String flowString = floatToHex(124.2f);
                    StringBuilder command = new StringBuilder();
                    command.append("010310").append(weightString).append(speedString).append(flowString);
                    byte lrc = lrc(command.toString());
                    String lrcString = byteToHex(lrc);
                    command.insert(0, ':');
                    command.append(lrcString).append('\r').append('\n');

                    if(serialPort != null){
                        if(serialPort.isOpen())
                            serialPort.writeBytes(command.toString().getBytes(),command.length());
                    }
                    Thread.sleep(1000);
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
