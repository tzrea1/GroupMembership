package groupservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
/**
 * @description: 处理心跳的线程，接到心跳后，如果来源并不是自己的neighbor就将新的来源设为自己的neighbor，更改拓扑结构
 * @author MXY
 * @date 12/17/22 5:43 PM
 * @version 1.0
 */
public class HeartbeatHandlerThread extends Thread {
    private Socket socket;
    private Daemon daemon;

    public HeartbeatHandlerThread(Socket socket, Daemon daemon) {
        this.socket = socket;
        this.daemon=daemon;
    }

    private void updateTopo(HeartbeatProto.Member receivedMember){
        if(daemon.getNeighbors().size()<=1){
            daemon.getNeighbors().add(receivedMember.getName());
            return;
        }
        int left, right, mid = Integer.parseInt(daemon.getDaemonName());
        int neb1 = Integer.parseInt(daemon.getNeighbors().get(0));
        int neb2 = Integer.parseInt(daemon.getNeighbors().get(1));
        if (((mid > neb1) && (mid > neb2)) || ((mid < neb1) && (mid < neb2))) {
            if (neb1 < neb2) {
                left = neb2;
                right = neb1;
            } else {
                left = neb1;
                right = neb2;
            }
        } else {
            if (neb1 < neb2) {
                left = neb1;
                right = neb2;
            } else {
                left = neb2;
                right = neb1;
            }
        }
        int joiner=Integer.parseInt(receivedMember.getName());
        if(left<joiner&&joiner<mid){
            daemon.getNeighbors().remove(Integer.toString(left));
            daemon.getNeighbors().add(Integer.toString(joiner));
        } else if (mid<joiner&&joiner<right) {
            daemon.getNeighbors().remove(Integer.toString(right));
            daemon.getNeighbors().add(Integer.toString(joiner));
        } else if (right>mid) {
            daemon.getNeighbors().remove(Integer.toString(left));
            daemon.getNeighbors().add(Integer.toString(joiner));
        } else if (mid>left) {
            daemon.getNeighbors().remove(Integer.toString(right));
            daemon.getNeighbors().add(Integer.toString(joiner));
        }else {
            System.out.println("Neighbors Not Change!");
        }
    }
    @Override
    public void run() {
        try {
            // 获取输入流
            InputStream is = socket.getInputStream();

            // 读取消息
            HeartbeatProto.Member receivedMember=HeartbeatProto.Member.parseFrom(is);

            // 当前心跳消息对应的name不存在于Map中
            if(!daemon.getNeighbors().contains(receivedMember.getName())) {
                updateTopo(receivedMember);
            }

            // 收到心跳消息,更新心跳时间映射
            daemon.getLastHeartbeatMap().put(receivedMember.getName(),System.currentTimeMillis());

            is.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}