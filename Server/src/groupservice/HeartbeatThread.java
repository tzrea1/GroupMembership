package groupservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 向邻居节点发送心跳
 * @author 
 * @date 12/17/22 4:29 PM
 * @version 1.0
 */
public class HeartbeatThread extends Thread {
    //后台进程本身
    Daemon daemon;

    public HeartbeatThread(Daemon daemon) {
        this.daemon=daemon;
    }
    /** 
     * @description: 启动发送心跳线程 
     * @param:  
     * @return: void 
     * @author root
     * @date: 12/17/22 4:40 PM
     */
    @Override
    public void run() {
        //连接到目标主机
        try {
            while(true){
                for(int i=0;i<daemon.memberList.size();i++){
                    Member member=daemon.memberList.get(i);
                    if(daemon.getNeighbors().contains(member.getName())){
                        new TransportHeartbeat(member.getAddress(),member.getPort(),daemon).start();
                    }
                }
                // 等待一段时间
                Thread.sleep(Daemon.HEARTBEAT_INTERVAL);
            }
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }
}
/**
 * @description: 发送心跳的线程，每确定一个接收方，就扔一个此线程用来向他发送心跳，
 * 这样即使接收方已离线，也不会导致上面的主线程exception
 * @author
 * @date 12/17/22 4:43 PM
 * @version 1.0
 */
class TransportHeartbeat extends Thread{

    public String recieverIp;
    public int recieverPort;
    Daemon daemon;
    public TransportHeartbeat(String ip,int port,Daemon daemon){
        this.recieverIp=ip;
        this.recieverPort=port;
        this.daemon=daemon;
    }
    public void run(){
        try {
            System.out.println("[SendHeartbeat]:向"+recieverIp+": "+recieverPort+"节点发送心跳");
            Socket socket =new Socket(recieverIp, recieverPort);
            OutputStream os = socket.getOutputStream();
            //向Server传递心跳信息
            // 封装待发送信息：与平台无关的protobuf格式
            HeartbeatProto.Member sentMessage = HeartbeatProto.Member.newBuilder()
                    .setIp(daemon.getDaemonAddress())
                    .setPort(daemon.getDaemonPort())
                    .setName(daemon.getDaemonName())
                    .setSendingTimestamp(System.currentTimeMillis())
                    .build();
            byte[] data = sentMessage.toByteArray();

            // 发送protobuf
            os.write(data);
            os.flush();
            os.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
