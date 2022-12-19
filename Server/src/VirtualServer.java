import groupservice.Daemon;
import localindex.IndexQuery;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class VirtualServer {
    private int port;
    public Daemon groupDaemon;
    // 每台服务器应该存放的xml文件数量
    private final int xmlProperNum=4;
    public VirtualServer(String name, int portID,boolean is_introducer,int deathTime){
        this.port=portID;
        // 初始化组服务
        groupDaemon=new Daemon(name,port+100,is_introducer,deathTime);
    }
    public void receiveQuery() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                DataInputStream is = new DataInputStream(socket.getInputStream());
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());

                //接收来自客户端的type信息
                String type = "";
                type = is.readUTF();

                // 创建Query实例
                Query query=new Query(port);
                //确定接收到了来自客户端的信息
                if (type.length()>0) {
                    if(type.equals("time")){
                        // 发送joinTime
                        String joinTime = query.getJoinTime();
                        System.out.println("[Query] :"+type+" joinTime " + joinTime);
                        os.writeUTF(joinTime);
                        os.flush();
                        // 发送crashTime
                        String crashTime = query.getCrashTime();
                        System.out.println("[Query] :"+type+" crashTime " + crashTime);
                        os.writeUTF(crashTime);
                        os.flush();
                    }
                    else if(type.equals("rate")){
                        List<Long> changedTimestamps=new ArrayList<>();
                        // 得到全部的changedTimestamp
                        query.getChangedTimestamps(changedTimestamps);
                        // 发送changeNum改变次数
                        os.writeUTF(Integer.toString(changedTimestamps.size()));
                        os.flush();
                        // 循环发送changedTimestamp
                        for(int i=0;i<changedTimestamps.size();i++){
                            os.writeUTF(Long.toString(changedTimestamps.get(i)));
                            os.flush();
                        }
                    }
                    else{
                        //向客户端发送查询结果信息
                        String queryResult = query.queryByType(type);
                        System.out.println("[Query] :"+type+" Result " + queryResult);
                        os.writeUTF(queryResult);
                        os.flush();
                    }
                }

                //关闭Socket链接
                is.close();
                os.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
