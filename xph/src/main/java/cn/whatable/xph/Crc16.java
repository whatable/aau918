package cn.whatable.xph;

/**
 * 基于Modbus CRC16的校验算法工具类
 */
public class Crc16 {

	/**
	 * 获取源数据和验证码的组合byte数组
	 * 
	 * @param strings
	 *            可变长度的十六进制字符串
	 * @return
	 */
	public static byte[] getData(String... strings) {
		byte[] data = new byte[] {};
		for (int i = 0; i < strings.length; i++) {
			int x = Integer.parseInt(strings[i], 16);
			byte n = (byte) x;
			byte[] buffer = new byte[data.length + 1];
			byte[] aa = { n };
			System.arraycopy(data, 0, buffer, 0, data.length);
			System.arraycopy(aa, 0, buffer, data.length, aa.length);
			data = buffer;
		}
		return getData(data);
	}

	/**
	 * 获取源数据和验证码的组合byte数组
	 * 
	 * @param aa
	 *            字节数组
	 * @return
	 */
	private static byte[] getData(byte[] aa) {
		byte[] bb = getCrc16(aa);
		byte[] cc = new byte[aa.length + bb.length];
		System.arraycopy(aa, 0, cc, 0, aa.length);
		System.arraycopy(bb, 0, cc, aa.length, bb.length);
		return cc;
	}

	/**
	 * 获取验证码byte数组，基于Modbus CRC16的校验算法
	 */
	private static byte[] getCrc16(byte[] arr_buff) {
		int len = arr_buff.length;

		// 预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
		int crc = 0xFFFF;
		int i, j;
		for (i = 0; i < len; i++) {
			// 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
			crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
			for (j = 0; j < 8; j++) {
				// 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
				if ((crc & 0x0001) > 0) {
					// 如果移出位为 1, CRC寄存器与多项式A001进行异或
					crc = crc >> 1;
					crc = crc ^ 0xA001;
				} else
					// 如果移出位为 0,再次右移一位
					crc = crc >> 1;
			}
		}
		return intToBytes(crc);
	}

	/**
	 * 将int转换成byte数组，低位在前，高位在后 改变高低位顺序只需调换数组序号
	 */
	private static byte[] intToBytes(int value) {
		byte[] src = new byte[2];
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}

	/**
	 * 将字节数组转换成十六进制字符串
	 */
	public static String byteTo16String(byte[] data) {
		StringBuffer buffer = new StringBuffer();
		for (byte b : data) {
			buffer.append(byteTo16String(b));
		}
		return buffer.toString();
	}

	/**
	 * 将字节转换成十六进制字符串 int转byte对照表 [128,255],0,[1,128) [-128,-1],0,[1,128)
	 */
	public static String byteTo16String(byte b) {
		StringBuffer buffer = new StringBuffer();
		int aa = (int) b;
		if (aa < 0) {
			buffer.append(Integer.toString(aa + 256, 16) + " ");
		} else if (aa == 0) {
			buffer.append("00 ");
		} else if (aa > 0 && aa <= 15) {
			buffer.append("0" + Integer.toString(aa, 16) + " ");
		} else if (aa > 15) {
			buffer.append(Integer.toString(aa, 16) + " ");
		}
		return buffer.toString();
	}

	public static int getCRC2(byte[] bytes, boolean reversed) {
		// ModBus 通信协议的 CRC ( 冗余循环校验码含2个字节, 即 16 位二进制数。
		// CRC 码由发送设备计算, 放置于所发送信息帧的尾部。
		// 接收信息设备再重新计算所接收信息 (除 CRC 之外的部分）的 CRC,
		// 比较计算得到的 CRC 是否与接收到CRC相符, 如果两者不相符, 则认为数据出错。
		//
		// 1) 预置 1 个 16 位的寄存器为十六进制FFFF(即全为 1) , 称此寄存器为 CRC寄存器。
		// 2) 把第一个 8 位二进制数据 (通信信息帧的第一个字节) 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器。
		// 3) 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位。
		// 4) 如果移出位为 0, 重复第 3 步 ( 再次右移一位); 如果移出位为 1, CRC 寄存器与多项式A001 ( 1010 0000 0000
		// 0001) 进行异或。
		// 5) 重复步骤 3 和步骤 4, 直到右移 8 次,这样整个8位数据全部进行了处理。
		// 6) 重复步骤 2 到步骤 5, 进行通信信息帧下一个字节的处理。
		// 7) 将该通信信息帧所有字节按上述步骤计算完成后,得到的16位CRC寄存器的高、低字节进行交换。
		// 8) 最后得到的 CRC寄存器内容即为 CRC码。

		int CRC = 0x0000ffff;
		int POLYNOMIAL = 0x0000a001;

		int i, j;
		for (i = 0; i < bytes.length; i++) {
			CRC ^= (int) bytes[i];
			for (j = 0; j < 8; j++) {
				if ((CRC & 0x00000001) == 1) {
					CRC >>= 1;
					CRC ^= POLYNOMIAL;
				} else {
					CRC >>= 1;
				}
			}
		}
		// 高低位转换，看情况使用
		if (reversed)
			CRC = ((CRC & 0xFF00) >> 8) | ((CRC & 0x00FF) << 8);
		return CRC;
	}

	public static int getCRC2(byte[] bytes) {
		return getCRC2(bytes, true);
	}

	public static void main(String[] args) {

		byte[] dd = Crc16.getData("01", "03", "00", "00", "00", "00");// "7E", "05", "66"
		String str = Crc16.byteTo16String(dd).toUpperCase();
		System.out.println(str);

		dd = new byte[] { 0x01, 0x03, 0x00, 0x00, 0x00, 0x00 };// 0x7E, 0x05, 0x66
		System.out.println(Integer.toHexString(getCRC2(dd)));
	}

}
