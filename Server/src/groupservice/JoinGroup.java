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
            System.out.println("Join:将本机proto信息发送给Introducer");
            
            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];
            int len = inputStream.read(buf);
            byte[] receivedData = Arrays.copyOfRange(buf, 0, len);
            GossipProto.MemberList receivedMemberList=GossipProto.MemberList.parseFrom(receivedData);

            System.out.println("Join:接收到Introducer发来的MemberList");
            // 将接收到的MemberList信息加入到本机memberList
            for (int i=0;i<receivedMemberList.getMemberListCount();i++) {
                String recievedName=receivedMemberList.getMemberList(i).getName();
                String recievedAddress=receivedMemberList.getMemberList(i).getIp();
                int recievedPort=receivedMemberList.getMemberList(i).getPort();
                long recievedTimestamp=receivedMemberList.getMemberList(i).getTimestamp();
                // 与本机Member信息不重复
                if(!recievedName.equals(daemon.getDaemonName())){
                    Member recievedMember=new Member(recievedName,recievedAddress,recievedPort,recievedTimestamp);
                    daemon.memberList.add(recievedMember);
                }
            }
            System.out.println("Join:成功加入组成员服务");
            System.out.println("当前组成员列表:");
            for(int i=0;i<daemon.memberList.size();i++){
                System.out.println(daemon.memberList.get(i).getName()+" "+daemon.memberList.get(i).getTimeStamp());
            }
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }
}
