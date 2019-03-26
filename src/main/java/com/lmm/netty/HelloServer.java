package com.lmm.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @description: 实现客户端发送一个请求，服务器会返回hello netty
 */
public class HelloServer {

    public static void main(String[] args) throws Exception{

        //定义一对线程组
        //主线程组,用于接受客户端的连接，但是不做任何处理
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        //从线程组，主线程组会把任务丢给从线程组，让从线程组去做任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //netty服务器的创建，serverBootStrap是一个启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)//设置主从线程组
                    .channel(NioServerSocketChannel.class)//设置nio的双向通道
                    .childHandler(new HelloServerInitializer());//子处理器，用于处理workerGroup

//        启动server，并且设置8088位启动的端口号，同事启动方式为同步
            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();

            //监听关闭的channel，设置为同步方式
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
