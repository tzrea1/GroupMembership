package groupservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class GossipHandlerThread extends Thread{
    private Socket socket;
    private Daemon daemon;

    /**
     * @Description TODO: 构造函数
     * @param socket
     * @param daemon
     * @Author root
     * @Date 2022/12/17 23:45
     * @Version 1.0
     **/
    public GossipHandlerThread(Socket socket, Daemon daemon) {
        this.socket = socket;
        this.daemon=daemon;
    }

    public void run() {
        try {
            // 获取输入流
            InputStream inputStream = socket.getInputStream();

            // 接收introducer传递过来的MemberList信息
            GossipProto.MemberList receivedMemberList=GossipProto.MemberList.parseFrom(inputStream);

            // 将接收到的MemberList信息Merge到本机memberList
            for (int i=0;i<receivedMemberList.getMemberListCount();i++) {
                String recievedName=receivedMemberList.getMemberList(i).getName();
                String recievedAddress=receivedMemberList.getMemberList(i).getIp();
                int recievedPort=receivedMemberList.getMemberList(i).getPort();
                long recievedTimestamp=receivedMemberList.getMemberList(i).getTimestamp();
                Member member=new Member(recievedName,recievedAddress,recievedPort,recievedTimestamp);
                // 首先判断member在MemberList里的情况
                int judgeResult=judgeMemberList(member);
                // member不存在于MemberList
                if(judgeResult==-1){
                    // 将member加入到List中
                    daemon.memberList.add(member);
                    daemon.memberList.sort(null);
                    System.out.println("Gossip:"+member.getName()+"加入到List中");
                }
                // member存在于MemberList中，但时间戳不同
                if(judgeResult>=0){
                    // 更新时间戳
                    daemon.memberList.get(judgeResult).setTimeStamp(member.getTimeStamp());
                    System.out.println("Gossip:"+member.getName()+"时间戳更新");
                }
            }
            inputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @Description TODO: 判断Member是否存在于本机的MemberList中，是否存在但时间戳不同
     * @return -2——存在且时间戳相同，-1——不存在，[x]——存在但时间戳不同，返回其index
     * @param inputMember
     * @Author root
     * @Date 2022/12/18 00:00
     * @Version 1.0
     **/
    public int judgeMemberList(Member inputMember){
        boolean isExisted=false;
        for(int i=0;i<daemon.memberList.size();i++){
            // 存在相同member
            if(daemon.memberList.get(i).exist(inputMember)){
                isExisted=true;
                if(!daemon.memberList.get(i).equals(inputMember)){
                    // member信息的时间戳不同
                    return i;
                }
            }
        }
        // -2——存在且时间戳相同
        if(isExisted==true){
            return -2;
        }
        // -1——不存在
        else{
            return -1;
        }
    }



}
