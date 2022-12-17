package groupservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenConnection extends Thread {
    private Daemon daemon;
    ServerSocket introducerServerSocket;

    public ListenConnection(Daemon daemonTmp, ServerSocket introducerServerSocket) {
        this.daemon = daemonTmp;
        this.introducerServerSocket = introducerServerSocket;
    }

    public void run() {
        while (true) {
            // 接收连接请求
            Socket socket = null;
            try {
                socket = introducerServerSocket.accept();
                // 启动消息处理线程
                new ListenHandle(socket, this.daemon).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
