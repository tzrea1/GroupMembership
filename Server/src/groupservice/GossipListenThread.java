package groupservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GossipListenThread extends Thread{
    private Daemon daemon;
    /**
     * @Description TODO: 构造函数
     * @return
     * @param daemon
     * @Author root
     * @Date 2022/12/18 17:17
     * @Version 1.0
     **/
    public GossipListenThread(Daemon daemon) {
        this.daemon=daemon;
    }

    @Override
    public void run() {
        // 创建心跳heartbeatServerSocket实例
        ServerSocket gossipServerSocket = null;
        try {
            gossipServerSocket = new ServerSocket(daemon.getPortGossip());
            while (true) {
                // 创建接收心跳连接请求的Socket
                Socket socketGossip = gossipServerSocket.accept();
                // 启动心跳连接处理线程
                new GossipHandlerThread(socketGossip, daemon).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
