package groupservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Daemon {
    /**
     * ip列表，记录可以访问的Server的ip地址
     */
    public final static String[] ipList = new String[]
            {"212.129.245.31", "1.15.143.17", "101.35.155.147"};
    /**
     * port列表，记录server的3个后台端口
     */
    public final static int[] portList = new int[]
            {9020, 9021, 9022};
    int portId;

    public Daemon(int portId) {
        this.portId = portId;
    }

    //add and test by mxy
    // 定义心跳消息
    public static String HEARTBEAT_MESSAGE = "I'm still alive";
    // 定义心跳频率（每隔1秒发送一次心跳）
    public static int HEARTBEAT_INTERVAL = 1000;
    // 定义组成员列表
    public List<String> memberList = new ArrayList<>();
    // 定义离线检查频率（每隔5秒检查一次）
    static final int OFFLINE_CHECK_INTERVAL = 500;
    // 定义离线超时时间（如果某个节点超过30秒没有发送心跳消息，则认为该节点已经离线）
    static final int OFFLINE_TIMEOUT = 1800;
    // 定义节点的最后心跳时间映射
    static final Map<String, Long> lastHeartbeatMap = new HashMap<>();

    //开始后台进程
    public void startDaemon() {
        try {
            // 创建ServerSocket实例
            ServerSocket serverSocket = new ServerSocket(portId);
            // 启动离线检查线程
            new OfflineCheckThread(this).start();
            // 启动心跳线程
            new HeartbeatThread().start();

            // 循环接收连接请求
            while (true) {
                // 接收连接请求
                Socket socket = serverSocket.accept();

                // 启动消息处理线程
                new MessageHandlerThread(socket,this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class HeartbeatThread extends Thread {

    public HeartbeatThread() {
    }

    @Override
    public void run() {
        //连接到目标主机
        List<Socket> sockets =new ArrayList<>();
        try {
            //先以本机两个端口做测试
            while(true){
                sockets.clear();
                for(int i=0;i<1;i++){
                    System.out.println("准备向"+Daemon.ipList[(i/2)%3]+": "+Daemon.portList[i%2]+"节点连接并发送心跳");
                    Socket socket =new Socket(Daemon.ipList[(i/2)%3], Daemon.portList[i%2]);
                    sockets.add(socket);
                }
                //先以本机两个端口做测试
                for(int i=0;i<1;i++){
                    DataOutputStream os = new DataOutputStream(sockets.get(i).getOutputStream());
                    //向Server传递心跳信息
                    System.out.println("正在发送心跳");
                    os.writeUTF(Daemon.HEARTBEAT_MESSAGE);
                    os.flush();
                    os.close();
                }
                // 等待一段时间
                Thread.sleep(Daemon.HEARTBEAT_INTERVAL);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MessageHandlerThread extends Thread {
    private Socket socket;
    private Daemon daemon;

    public MessageHandlerThread(Socket socket,Daemon daemon) {
        this.socket = socket;
        this.daemon=daemon;
    }

    @Override
    public void run() {
        try {
            // 获取输入流
            DataInputStream is = new DataInputStream(socket.getInputStream());
            // 读取消息
            String message = is.readUTF();
            // 如果收到心跳消息
            if (Daemon.HEARTBEAT_MESSAGE.equals(message)) {
                System.out.println("接到心跳信息");
                // 更新组成员列表
                updateMemberListandTime(socket,daemon);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //////////////////////有问题///////////////
    private void updateMemberListandTime(Socket socket,Daemon daemon) {
        // 获取连接的主机地址
        String host = socket.getInetAddress().getHostAddress();

        // 如果该主机不在组成员列表中，则将其添加到组成员列表中
        if (!daemon.memberList.contains(host)) {
            daemon.memberList.add(host);
        }
        // 更新客户端的最后心跳时间
        daemon.lastHeartbeatMap.put(host, System.currentTimeMillis());
    }
}

//离线检查类
class OfflineCheckThread extends Thread {
    private Daemon daemon;

    public OfflineCheckThread(Daemon daemon) {
        this.daemon=daemon;
    }
    public void run() {
        while (true) {
            try {
                // 获取当前时间
                long currentTime = System.currentTimeMillis();
                // 遍历组成员列表
                for (String host : daemon.memberList) {
                    // 获取节点的最后心跳时间
                    long lastHeartbeatTime = daemon.lastHeartbeatMap.get(host);

                    // 如果节点已经超过离线超时时间没有发送心跳消息，则认为该节点已经离线
                    if (currentTime - lastHeartbeatTime > daemon.OFFLINE_TIMEOUT) {
                        // 从组成员列表中删除该节点
                        daemon.memberList.remove(host);
                        System.out.println("有节点离线");
                    }
                }

                // 等待一段时间
                Thread.sleep(daemon.OFFLINE_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
