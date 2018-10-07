package cn.whatable.xph;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

class DataFetchThread extends Thread {
	private byte[] instruct;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChannelHandlerContext ctx = null;

	private boolean stop = false;

	public DataFetchThread(ChannelHandlerContext ctx, int seq) {
		this.ctx = ctx;

		instruct = new byte[] { (byte) seq, 0x03, 0, 0, 0, 0 };
		int crc = Crc16.getCRC2(instruct);
		instruct = Arrays.copyOf(instruct, instruct.length + 2);
		instruct[instruct.length - 1] = (byte) (crc & 0xff);
		instruct[instruct.length - 2] = (byte) ((crc >> 8) & 0xff);
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

			if (stop)
				break;
			ByteBuf buf = ctx.alloc().buffer(8);
			buf.writeBytes(instruct);
			ctx.writeAndFlush(buf);

			logger.info(">[[[SERVER]]]< Send Instruct of DATA-FETCH:: {} (to {})",
					Crc16.byteTo16String(instruct).toUpperCase(), ctx.channel().remoteAddress());
		}
	}

	@Override
	public void interrupt() {
		stop = true;
		super.interrupt();
	}

}
