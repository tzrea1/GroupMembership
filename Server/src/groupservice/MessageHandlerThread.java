package groupservice;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
/**
 * @description: 处理心跳的线程，接到心跳后，如果来源并不是自己的neighbor就将新的来源设为自己的neighbor，更改拓扑结构
 * @author MXY
 * @date 12/17/22 5:43 PM
 * @version 1.0
 */
public class MessageHandlerThread extends Thread {
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
            String receivedPort=is.readUTF();
            // 如果收到心跳消息
            if (Daemon.HEARTBEAT_MESSAGE.equals(message)) {
                System.out.println("接到心跳信息");
                // 更新组成员列表
                updateMemberListandTime(socket,daemon,receivedPort);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateMemberListandTime(Socket socket,Daemon daemon,String receivedPort) {
        // 获取连接的主机地址
        String host = socket.getInetAddress().getHostAddress();
        String memberId=host+":"+receivedPort;
        // 如果该主机不在组成员列表中，则将其添加到组成员列表中
        if (!daemon.memberList.contains(memberId)) {
            daemon.memberList.add(memberId);
        }
        // 更新客户端的最后心跳时间
        daemon.lastHeartbeatMap.put(memberId, System.currentTimeMillis());
    }
}
