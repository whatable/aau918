package cn.whatable.xph;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.Test;

public class ModbusCentreTest {

	private static final String HOST = "116.62.18.192";
	private static final int PORT = 502;

	private static void serverStart() {
		String[] args = new String[] { "-port=" + PORT };
		try {
			ModbusCentre.main(args);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void clientStart() throws Exception {
		Socket socket = new Socket(HOST, PORT);

		InputStream in = socket.getInputStream();
		byte[] data = new byte[8];
		while (true) {
			in.read(data);
			System.out.println(Crc16.byteTo16String(data));// 01 03 00 00 00 00 45 CA

			OutputStream out = socket.getOutputStream();
			byte[] b = "abcde".getBytes();
			out.write(b);

			Thread.sleep(2000);
			// socket.close();
		}
	}

	@Test
	public static void testTextMessage() {
		new Thread() {
			public void run() {
				serverStart();
			}
		}.start();

		try {
			Thread.sleep(700);
			clientStart();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		// ModbusCentreTest.testTextMessage();

		clientStart();
	}
}
