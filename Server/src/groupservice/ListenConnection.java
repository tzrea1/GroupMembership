package groupservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * @description: introducer监听新节点的加入请求，并利用ListenHandle线程把当前的memberList发送给它
 * @author MXY
 * @date 12/17/22 5:16 PM
 * @version 1.0
 */
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
