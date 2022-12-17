package groupservice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HeartbeatThread extends Thread {
    Daemon daemon;

    public HeartbeatThread(Daemon daemon) {
        this.daemon=daemon;
    }

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
                    System.out.println("准备向"+ip+": "+port+"节点连接并发送心跳");
                    Socket socket =new Socket(ip, port);
                    sockets.add(socket);
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
                for(int i=0;i<sockets.size();i++){
                    DataOutputStream os = new DataOutputStream(sockets.get(i).getOutputStream());
                    //向Server传递心跳信息
                    System.out.println("正在发送心跳");
                    os.writeUTF(Daemon.HEARTBEAT_MESSAGE);
                    os.writeUTF(daemon.portId+"");
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
