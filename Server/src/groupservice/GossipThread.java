package groupservice;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description TODO: 向neighbors发送Gossip信息
 * @Author root
 * @Date 2022/12/17 22:54
 * @Version 1.0
 **/
public class GossipThread extends Thread {
    //后台进程本身
    Daemon daemon;

    /**
     * @Description TODO: 构造函数
     * @Author root
     * @Date 2022/12/17 22:57
     * @Version 1.0
     **/
    public GossipThread(Daemon daemon) {
        this.daemon=daemon;
    }

    /**
     * @Description TODO: 运行Gossip线程
     * @return
     * @Author root
     * @Date 2022/12/17 22:58
     * @Version 1.0
     **/
    @Override
    public void run() {
        //连接到目标主机
        try {
            while(true){
                for(int i=0;i<daemon.memberList.size();i++){
                    Member member=daemon.memberList.get(i);
                    if(daemon.getNeighbors().contains(member.getName())){
                        new SendGossip(member.getAddress(),member.getPortGossip(),daemon).start();
                    }
                }
                // 等待一段时间
                Thread.sleep(Daemon.GOSSIP_INTERVAL);
            }
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class SendGossip extends Thread{
    public String recieverIp;
    public int recieverPort;
    Daemon daemon;
    public SendGossip(String ip,int port,Daemon daemon){
        this.recieverIp=ip;
        this.recieverPort=port;
        this.daemon=daemon;
    }
    public void run(){
        try {
            // 向Server传递Gossip信息
            // 封装待发送信息：与平台无关的protobuf格式
            // 将MemberList封装为protobuf形式
            GossipProto.MemberList.Builder memListBuilder= GossipProto.MemberList.newBuilder();
            for (Member member:daemon.memberList) {
                memListBuilder.addMemberList(GossipProto.Member.newBuilder()
                        .setIp(member.getAddress())
                        .setName(member.getName())
                        .setPort(member.getPort())
                        .setTimestamp(member.getTimeStamp())
                        .build());
            }
            GossipProto.MemberList memberList=memListBuilder.build();
            byte[] data = memberList.toByteArray();
            if(!Arrays.equals(daemon.gossipBackup,data)){
                System.out.println("[SendGossip]:信息有更新，新旧字节数分别为："+data .length+" "+daemon.gossipBackup.length);
                System.out.println("[SendGossip]:向"+recieverIp+": "+recieverPort+"节点发送Gossip");
                Socket socket =new Socket(recieverIp, recieverPort);
                OutputStream os = socket.getOutputStream();
                // 发送protobuf
                os.write(data);
                os.flush();
                os.close();
                socket.close();
            }
            // 将本次发送的内容记录到backup中
            daemon.gossipBackup=data;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
