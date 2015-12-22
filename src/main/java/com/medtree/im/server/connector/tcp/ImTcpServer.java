package com.medtree.im.server.connector.tcp;

import com.medtree.im.server.connector.tcp.handler.IMChannelInitializer;
import com.medtree.im.server.service.IServerService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by lrsec on 6/25/15.
 */
public class ImTcpServer {

    private static final int EXECUTOR_CORE_SIZE =
            Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
    private static final int CONNECT_TIMEOUT_MILLIS = 3 * 1000;
    private static final int SO_TIMEOUT_MILLIS = 3000;

    private static Logger logger = LoggerFactory.getLogger(ImTcpServer.class);

    @Autowired
    private IServerService serverService;

    @Value("${im.port}")
    private int port;

    public void run() throws Exception{
        logger.debug("Im Tcp Server Started at {}", DateTime.now());

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(EXECUTOR_CORE_SIZE);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new IMChannelInitializer(executorService))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_TIMEOUT, SO_TIMEOUT_MILLIS)
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            serverService.register();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Error in Tcp Server", e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

        logger.debug("Im Tcp Server Stopped at {}", DateTime.now());
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("spring/tcp.xml");

        try {
            ImTcpServer server = (ImTcpServer)appContext.getBean("imTcpServer");
            server.run();
        } catch (Exception e) {
            logger.error("Exception in starting ImTcpServer", e);
        }
    }
}