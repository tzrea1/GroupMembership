package groupservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

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

            // 接收传递过来的MemberList信息
            byte[] buf = new byte[2048];
            int len = inputStream.read(buf);
            byte[] receivedData = Arrays.copyOfRange(buf, 0, len);
            // 是否对memberList进行了改动
            boolean isChanged=false;

            // 标识是否存在于传输来的信息
            boolean allExist[]=new boolean[daemon.memberList.size()];
            Arrays.fill(allExist, false);
            // 记录先前List中的Member名称：和allExist对应
            String nameBackup[]= new String[daemon.memberList.size()];
            for(int i=0;i<daemon.memberList.size();i++){
                nameBackup[i]=daemon.memberList.get(i).getName();
            }

            GossipProto.MemberList receivedMemberList=GossipProto.MemberList.parseFrom(receivedData);

            // 将接收到的MemberList信息Merge到本机memberList
            for (int i=0;i<receivedMemberList.getMemberListList().size();i++) {
                String recievedName=receivedMemberList.getMemberList(i).getName();
                String recievedAddress=receivedMemberList.getMemberList(i).getIp();
                int recievedPort=receivedMemberList.getMemberList(i).getPort();
                long recievedTimestamp=receivedMemberList.getMemberList(i).getTimestamp();
                Member member=new Member(recievedName,recievedAddress,recievedPort,recievedTimestamp);
                // 首先判断member在MemberList里的情况
                int judgeResult=judgeMemberList(member,allExist,nameBackup);
                // member不存在于MemberList
                if(judgeResult==-1){
                    // 将member加入到List中
                    daemon.memberList.add(member);
                    daemon.memberList.sort(null);
                    System.out.println("[RecieveGossip]:"+member.getName()+"加入到List中");
                    isChanged=true;
                }
                // member存在于MemberList中，但时间戳不同
                else if(judgeResult>=0){
                    // 更新时间戳
                    daemon.memberList.get(judgeResult).setTimeStamp(member.getTimeStamp());
                    System.out.println("[RecieveGossip]:"+member.getName()+"时间戳更新");
                    isChanged=true;
                }
                else{

                }
                // 输出当前MemberList信息
                System.out.println("[RecieveGossip]: 当前MemberList成员: ");
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
            }
            // 将不同时存在于两个List的Member Remove
            for (int i=0;i<daemon.memberList.size();i++){
                String name=daemon.memberList.get(i).getName();
                int index=Arrays.asList(nameBackup).indexOf(name);
                if(index!=-1){
                    // 该Member不在两个List中同时存在,移除
                    if(allExist[index]==false){
                        daemon.memberList.remove(i);
                        isChanged=true;
                        System.out.println("[RecieveGossip]: MemberList中，removed");
                    }
                }
            }
            inputStream.close();
            socket.close();

            // 开启一次写Gossip日志的线程
            new logWriteThread(daemon.getDaemonPort(),"gossip",System.currentTimeMillis(),"-","-",-1,isChanged,daemon.memberList).start();

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
    public int judgeMemberList(Member inputMember,boolean[] allExist,String[] nameBackup){
        boolean isExisted=false;
        for(int i=0;i<daemon.memberList.size();i++){
            // 存在相同member
            if(daemon.memberList.get(i).exist(inputMember)){
                isExisted=true;
                // 记录到allExist：标识同时存在于两个List
                int index=Arrays.asList(nameBackup).indexOf(daemon.memberList.get(i).getName());
                if(index!=-1){
                    allExist[index]=true;
                }
                if(daemon.memberList.get(i).getTimeStamp()!=inputMember.getTimeStamp()){
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
