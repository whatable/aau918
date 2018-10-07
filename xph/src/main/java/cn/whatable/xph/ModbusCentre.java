package cn.whatable.xph;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ModbusCentre {

	private static Logger logger = LoggerFactory.getLogger(ModbusCentre.class);
	private static int PORT = 502;
	private static int SEQ = 1;

	public static void start() throws InterruptedException {
		logger.info(">[[[SERVER]]]< ------------------------------------------------------------ ");
		logger.info(">[[[SERVER]]]<  ");
		logger.info(">[[[SERVER]]]< MODBUS SERVER READY TO GO @ " + new Date());
		logger.info(">[[[SERVER]]]<  ");
		logger.info(">[[[SERVER]]]< ------------------------------------------------------------ ");
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new DataFetchHandler(SEQ));

			// Start the server.
			ChannelFuture f = b.bind(PORT).sync();
			logger.info(">[[[SERVER]]]< Start Listening @ port: {} ", PORT);

			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws InterruptedException {

		if (args != null && args.length > 0) {
			for (String arg : args) {
				arg = arg.toUpperCase();
				if (arg.startsWith("-PORT=")) {
					try {
						PORT = Integer.parseInt(arg.substring("-PORT=".length()));
					} catch (NumberFormatException e) {
					}
				} else if (arg.startsWith("-ADD=")) {
					try {
						SEQ = Integer.parseInt(arg.substring("-ADD=".length()));
					} catch (NumberFormatException e) {
					}
				}
			}
		}

		ModbusCentre.start();

	}
}
