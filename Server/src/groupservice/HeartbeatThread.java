package groupservice;

import java.io.DataOutputStream;
import java.io.IOException;
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
        List<Socket> sockets =new ArrayList<>();
        try {
            //先以本机两个端口做测试
            while(true){
                sockets.clear();
//                for(int i=0;i<1;i++){
//                    System.out.println("准备向"+Daemon.ipList[(i/2)%3]+": "+Daemon.portList[i%2]+"节点连接并发送心跳");
//                    Socket socket =new Socket(Daemon.ipList[(i/2)%3], Daemon.portList[i%2]);
//                    sockets.add(socket);
//                }
                for(String memberId : daemon.memberList){
                    String[]  strs=memberId.split(":");
                    String ip=strs[0];
                    int port=Integer.parseInt(strs[1]);
//                    System.out.println("准备向"+ip+": "+port+"节点连接并发送心跳");
//                    Socket socket =new Socket(ip, port);
//                    sockets.add(socket);
                    new TransportHeartbeat(ip,port,daemon).start();
                }
                //先以本机两个端口做测试
//                for(int i=0;i<1;i++){
//                    DataOutputStream os = new DataOutputStream(sockets.get(i).getOutputStream());
//                    //向Server传递心跳信息
//                    System.out.println("正在发送心跳");
//                    os.writeUTF(Daemon.HEARTBEAT_MESSAGE);
//                    os.writeUTF(Daemon.portList[(i%2)]+"");
//                    os.flush();
//                    os.close();
//                }
//                for(int i=0;i<sockets.size();i++){
//                    DataOutputStream os = new DataOutputStream(sockets.get(i).getOutputStream());
//                    //向Server传递心跳信息
//                    System.out.println("正在发送心跳");
//                    os.writeUTF(Daemon.HEARTBEAT_MESSAGE);
//                    os.writeUTF(daemon.portId+"");
//                    os.flush();
//                    os.close();
//                }
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
    public String ip;
    public int port;
    Daemon daemon;
    public TransportHeartbeat(String ip,int port,Daemon daemon){
        this.ip=ip;
        this.port=port;
        this.daemon=daemon;
    }
    public void run(){
        try {
            System.out.println("准备向"+ip+": "+port+"节点连接并发送心跳");
            Socket socket =new Socket(ip, port);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            //向Server传递心跳信息
            System.out.println("正在发送心跳");
            os.writeUTF(Daemon.HEARTBEAT_MESSAGE);
            //发送自己的port以便于接收心跳的成员识别，并进行memberList及时间map的更新
            os.writeUTF(daemon.port+"");
            os.flush();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
