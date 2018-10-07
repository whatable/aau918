package cn.whatable.xph;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
class DataFetchHandler extends ChannelDuplexHandler {

	final static Map<SocketAddress, DataFetchThread> THREADS = new HashMap<>();
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int seq = 1;

	public DataFetchHandler(int seq) {
		this.seq = seq;
	}

	private void startInteraction(ChannelHandlerContext ctx) {
		logger.info(">[[[SERVER]]]< /////// CONNECTED /////// CONNECTED /////// CONNECTED ///////");
		logger.info(">[[[SERVER]]]< START Interaction with {}", ctx.channel().remoteAddress());
		SocketAddress addr = ctx.channel().remoteAddress();
		DataFetchThread t = new DataFetchThread(ctx, seq);
		THREADS.put(addr, t);
		t.start();
	}

	private void stopInteraction(ChannelHandlerContext ctx) {
		logger.info(">[[[SERVER]]]< STOP Interaction with {}", ctx.channel().remoteAddress());
		SocketAddress addr = ctx.channel().remoteAddress();
		Thread t = THREADS.get(addr);
		if (t != null) {
			THREADS.remove(addr);
			if (t.isAlive()) {
				t.interrupt();
			}
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		startInteraction(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		stopInteraction(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) msg;
			byte[] dst = new byte[buf.readableBytes()];
			buf.readBytes(dst);

			logger.info(">[[[SERVER]]]< Data Recieved:: {} (from {})", Crc16.byteTo16String(dst).toUpperCase(),
					ctx.channel().remoteAddress());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext arg0, Throwable e) throws Exception {
		logger.error(">[[[SERVER]]]< EXCEPTION!!:: {}", e.getMessage());
	}
}
