package groupservice;

import java.io.*;
import java.net.*;
import java.util.*;

public class Daemon {
    /**
     * ip列表，记录可以访问的Server的ip地址
     */
    public final static String[] ipList = new String[]
            {"212.129.245.31", "1.15.143.17", "101.35.155.147"};
    /**
     * port列表，记录server的3个后台端口
     */
    public final static int[] portList = new int[]
            {9020, 9021, 9022};
    int portId;
    boolean is_introducer=false;

    public Daemon(int portId,int inputIntroducer) {
        this.portId = portId;
        if(inputIntroducer==1){
            is_introducer=true;
        }
        String localIp= null;
        try {
            localIp = getPublicIp();
            String memberId=localIp+":"+(this.portId+"");
            this.memberList.add(memberId);
            this.lastHeartbeatMap.put(memberId, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //add and test by mxy
    // 定义心跳消息
    public static String HEARTBEAT_MESSAGE = "I'm still alive";
    // 定义心跳频率（每隔1秒发送一次心跳）
    public static int HEARTBEAT_INTERVAL = 1000;
    // 定义组成员列表
    public List<String> memberList = new ArrayList<>();
    // 定义离线检查频率（每隔5秒检查一次）
    static final int OFFLINE_CHECK_INTERVAL = 500;
    // 定义离线超时时间（如果某个节点超过30秒没有发送心跳消息，则认为该节点已经离线）
    static final int OFFLINE_TIMEOUT = 1800;
    // 定义节点的最后心跳时间映射
    public Map<String, Long> lastHeartbeatMap = new HashMap<>();
    //获取本机公网ip地址
    public static String getPublicIp() throws Exception{
        Process process = Runtime.getRuntime().exec("dig +short myip.opendns.com @resolver1.opendns.com");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String publicIp = reader.readLine();
        reader.close();
        return publicIp;
    }

    //开始后台进程
    public void startDaemon() {
        try {
            // 创建ServerSocket实例
            ServerSocket serverSocket = new ServerSocket(portId);
            //启动introducer线程
            if(is_introducer){
                System.out.println("yesIntroducer");
                ServerSocket introducerServerSocket=new ServerSocket(portId+100);

                new ListenConnection(this,introducerServerSocket).start();
            }
            //启动向introducer连接的请求
            new JoinGroup(this).start();

            // 启动心跳线程
            new HeartbeatThread(this).start();
            // 启动离线检查线程
            new OfflineCheckThread(this).start();

            // 循环接收连接请求
            while (true) {
                // 接收连接请求
                Socket socket = serverSocket.accept();

                // 启动消息处理线程
                new MessageHandlerThread(socket,this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}








