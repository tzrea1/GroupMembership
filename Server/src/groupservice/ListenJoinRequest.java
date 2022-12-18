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
public class ListenJoinRequest extends Thread {
    private Daemon daemon;
    ServerSocket introducerServerSocket;

    public ListenJoinRequest(Daemon daemonTmp, ServerSocket introducerServerSocket) {
        this.daemon = daemonTmp;
        this.introducerServerSocket = introducerServerSocket;
    }

    public void run() {
        while (true) {
            // 接收连接请求
            try {
                Socket socket = introducerServerSocket.accept();
                // 启动消息处理线程
                new JoinRequestHandler(socket, this.daemon).start();
                System.out.println("接收到加入请求，对其进行处理!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
