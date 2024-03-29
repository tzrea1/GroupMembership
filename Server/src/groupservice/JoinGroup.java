package groupservice;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * @description: 新节点向introducer发送加入申请，接收introducer发过来的memberList
 * @author MXY
 * @date 12/17/22 5:24 PM
 * @version 1.0
 */
public class JoinGroup extends Thread{
    private final String introducerName="0";
    private final String introducerIp="212.129.245.31";
    private final int introducerPort=9220;
    private Daemon daemon;
    /**
     * @Description TODO: 构造函数
     * @return
     * @param daemon
     * @Author root
     * @Date 2022/12/17 21:01
     * @Version 1.0
     **/
    public JoinGroup(Daemon daemon){
        this.daemon=daemon;
    }

    public void run(){
        //创建Socket链接
        Socket socket = null;
        try {
            socket = new Socket(introducerIp, introducerPort);
            OutputStream outputStream = socket.getOutputStream();
            // 将本机信息封装为protobuf
            HeartbeatProto.Member sentMessage = HeartbeatProto.Member.newBuilder()
                    .setIp(daemon.getDaemonAddress())
                    .setPort(daemon.getDaemonPort())
                    .setName(daemon.getDaemonName())
                    .setSendingTimestamp(System.currentTimeMillis())
                    .build();
            byte[] data = sentMessage.toByteArray();
            // 将protobuf信息发送给introducer
            //sentMessage.writeTo(outputStream);
            outputStream.write(data);
            outputStream.flush();
            System.out.println("[SendJoin]:将本机proto信息发送给Introducer");

            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];
            int len = inputStream.read(buf);
            byte[] receivedData = Arrays.copyOfRange(buf, 0, len);
            GossipProto.MemberList receivedMemberList=GossipProto.MemberList.parseFrom(receivedData);

            System.out.println("[SendJoin]:接收到Introducer发来的MemberList");
            // 将接收到的MemberList信息加入到本机memberList
            for (int i=0;i<receivedMemberList.getMemberListList().size();i++) {
                String recievedName = receivedMemberList.getMemberList(i).getName();
                String recievedAddress = receivedMemberList.getMemberList(i).getIp();
                int recievedPort = receivedMemberList.getMemberList(i).getPort();
                long recievedTimestamp = receivedMemberList.getMemberList(i).getTimestamp();
                Member recievedMember = new Member(recievedName, recievedAddress, recievedPort, recievedTimestamp);
                daemon.memberList.add(recievedMember);
                daemon.memberList.sort(null);
            }
            // 输出当前MemberList信息
            System.out.println("[SendJoin]: 当前MemberList成员: ");
            for(int j=0;j<daemon.memberList.size();j++){
                String members="";
                members+=daemon.memberList.get(j).getName();
                members+=" ";
                members+=daemon.memberList.get(j).getAddress();
                members+=" ";
                members+=daemon.memberList.get(j).getPort();
                members+=" ";
                members+=daemon.memberList.get(j).getTimeStamp();
                System.out.println(members);
            }
            // 找到neighbors
            System.out.println("[SendJoin]:当前Neighbors:");
            daemon.findNeighbors();
            for(int i=0;i<daemon.getNeighbors().size();i++){
                System.out.println(daemon.getNeighbors().get(i));
            }
            outputStream.close();
            inputStream.close();
            socket.close();
            System.out.println("[SendJoin]:成功加入组成员服务");
            // 开启一次写Join日志的线程
            new logWriteThread(daemon.getDaemonPort(),"join",System.currentTimeMillis(),introducerName,introducerIp,introducerPort,true,daemon.memberList).start();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }
}
