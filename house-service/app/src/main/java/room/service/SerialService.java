package room.service;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialService implements SerialPortEventListener {

	private SerialPort serialPort;
	private String receivedMessage;
	private StringBuilder currentMessage = new StringBuilder();
	private boolean messageAvailable = false;

	public SerialService(String port, int rate) throws SerialPortException {
		serialPort = new SerialPort(port);
		serialPort.openPort();
		serialPort.setParams(rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
		serialPort.addEventListener(this);
	}

	public void sendMsg(String msg) throws SerialPortException {
		String messageWithNewline = msg + "\n";
		serialPort.writeString(messageWithNewline);
	}

	public String receiveMsg() throws InterruptedException {
		synchronized (this) {
			while (!messageAvailable) {
				wait();
			}
			String message = receivedMessage;
			receivedMessage = null;
			messageAvailable = false;
			return message;
		}
	}

	public boolean isMsgAvailable() {
		synchronized (this) {
			return messageAvailable;
		}
	}

	public void close() {
		try {
			if (serialPort != null) {
				serialPort.removeEventListener();
				serialPort.closePort();
			}
		} catch (SerialPortException ex) {
			ex.printStackTrace();
		}
	}

	public void serialEvent(SerialPortEvent event) {
		if (event.isRXCHAR()) {
			try {
				String receivedData = serialPort.readString(event.getEventValue());
				receivedData = receivedData.replaceAll("\r", "");
				currentMessage.append(receivedData);

				int index;
				while ((index = currentMessage.indexOf("\n")) >= 0) {
					String message = currentMessage.substring(0, index);
					currentMessage.delete(0, index + 1);

					synchronized (this) {
						receivedMessage = message;
						messageAvailable = true;
						notify();
					}
				}
			} catch (SerialPortException ex) {
				ex.printStackTrace();
				System.out.println("Error in receiving string from COM-port: " + ex);
			}
		}
	}
}
