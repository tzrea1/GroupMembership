package groupservice;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

public class Daemon {
    // 定义心跳消息
    public static String HEARTBEAT_MESSAGE = "I'm still alive";
    // 定义心跳频率（每隔1秒发送一次心跳）
    public static int HEARTBEAT_INTERVAL = 1000;
    // 定义Gossip频率（每隔2秒发送一次Gossip）
    public static int GOSSIP_INTERVAL = 2000;
    // 定义组成员列表
    public LinkedList<Member> memberList = new LinkedList<>();
    // 定义离线检查频率（每隔5秒检查一次）
    static final int OFFLINE_CHECK_INTERVAL = 500;
    // 定义离线超时时间（如果某个节点超过30秒没有发送心跳消息，则认为该节点已经离线）
    static final int OFFLINE_TIMEOUT = 1800;

    // 本机名称
    private String name;
    // 本机公网IP
    private String address;

    // Daemon的1号端口: 用于心跳机制
    private int port;
    // Daemon的2号端口: 用于Gossip机制
    private int portGossip;
    // Daemon的3号端口: 用于Join机制
    private int portJoin;

    // 标识本机是否为introducer
    private boolean isIntroducer=false;
    // 定义节点的最后心跳时间映射
    private Map<String, Long> lastHeartbeatMap = new HashMap<>();
    // 定义节点的在拓扑结构上的neighbors:记录name
    private List<String> neighbors=new ArrayList<>();
    /**
     * @description: Daemon构造函数
     * @param: portId   Daemon的基础端口
    inputIntroducer    是否为introducer
     * @return:
     * @author root
     * @date: 12/17/22 4:20 PM
     */
    public Daemon(String name,int port,boolean isIntroducer) {
        this.name=name;
        this.port = port;
        this.portGossip=port+100;
        this.portJoin=port+200;
        this.isIntroducer=isIntroducer;
        // 获取本机公网IP
        String localIp= null;
        try {
            localIp = getPublicIp();
            this.address=localIp;
            // 将本机Member信息加入memberList
            long timestamp= System.currentTimeMillis();
            Member member=new Member(this.name,this.address,this.port,timestamp);
            // 加入组成员服务列表，并排序
            this.memberList.add(member);
            this.memberList.sort(null);

            // 初始化本机的neighbors
            findNeighbors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @description:  获取本机公网ip，目前是用来将自己的信息加入memberList中
     * @param:
     * @return: java.lang.String
     * @author root
     * @date: 12/17/22 4:23 PM
     */
    public static String getPublicIp() throws Exception{
        Process process = Runtime.getRuntime().exec("dig +short myip.opendns.com @resolver1.opendns.com");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String publicIp = reader.readLine();
        reader.close();
        return publicIp;
    }
    /**
     * @Description TODO: 将本机的neighbors加入List
     * @Author root
     * @Date 2022/12/17 19:18
     * @Version 1.0
     **/
    public void findNeighbors() {
        // 组成员列表中只有本机
        if(memberList.size()==1){
            // 此时不存在neighbors
            return;
        }
        // 组成员列表中只有本机和另一台虚拟机
        else if(memberList.size()==2){
            for(Member member : memberList){
                if(!member.getName().equals(name)){
                    // 确定是另外一台虚拟机
                    neighbors.add(member.getName());
                }
            }
        }
        // 组成员列表中除了本机以外，有2台以上虚拟机
        else{
            // 算法的思路是：将全部虚拟机按照id大小排列成一个圆形，如1-2-3-4-5-6-1
            // 分别找到一个节点的左相邻和右相邻

            // 首先，顺时针寻找右相邻
            int rightNeighborID=Integer.parseInt(name)+1;
            boolean rightNeighborFound=false;
            while(true){
                // 到达最右侧
                if(rightNeighborID==memberList.size()){
                    // 转一圈回到左侧
                    rightNeighborID=0;
                }
                // 遍历列表，确定是否存在当前rightNeighborID
                for(int i=0;i<memberList.size();i++){
                    if(Integer.parseInt(memberList.get(i).getName())==rightNeighborID){
                        rightNeighborFound=true;
                        break;
                    }
                }
                // 确定右相邻
                if(rightNeighborFound==true){
                    break;
                }
                rightNeighborID++;
            }
            // 然后，逆时针寻找左相邻
            int leftNeighborID=Integer.parseInt(name)-1;
            boolean leftNeighborFound=false;
            while(true){
                // 到达最左侧
                if(leftNeighborID==-1){
                    // 转一圈回到左侧
                    leftNeighborID=memberList.size()-1;
                }
                // 遍历列表，确定是否存在当前leftNeighborID
                for(int i=0;i<memberList.size();i++){
                    if(Integer.parseInt(memberList.get(i).getName())==leftNeighborID){
                        leftNeighborFound=true;
                        break;
                    }
                }
                // 确定左相邻
                if(leftNeighborFound==true){
                    break;
                }
                leftNeighborID++;
            }
            // 将左相邻、右相邻加入neighbors
            if(!neighbors.contains(String.valueOf(leftNeighborID)))
                neighbors.add(String.valueOf(leftNeighborID));
            if(!neighbors.contains(String.valueOf(rightNeighborID)))
                neighbors.add(String.valueOf(rightNeighborID));
        }
        return;
    }
    /**
     * @Description TODO: 开启Daemon后台进程
     * @return
     * @Author root
     * @Date 2022/12/17 18:56
     * @Version 1.0
     **/
    public void startDaemon() {
        try {
            // 创建心跳heartbeatServerSocket实例
            ServerSocket heartbeatServerSocket = new ServerSocket(port);
            // 创建Gossip gossipServerSocket实例
            ServerSocket gossipServerSocket = new ServerSocket(portGossip);
            //启动introducer线程
            if(isIntroducer){
                System.out.println("Server as Introducer!");
                ServerSocket introducerServerSocket=new ServerSocket(portJoin);

                new ListenJoinRequest(this,introducerServerSocket).start();
            }
            else{
                // 启动向introducer连接的请求
                new JoinGroup(this).start();
            }

            // 启动心跳线程
            new HeartbeatThread(this).start();

            // 启动离线检查线程
            new OfflineCheckThread(this).start();

            // 启动gossip线程
            new GossipThread(this).start();


            // 循环接收连接请求
            while (true) {
                // 创建接收心跳连接请求的Socket
                Socket socketHeartbeat = heartbeatServerSocket.accept();
                // 创建接收Gossip连接请求的Socket
                Socket socketGossip = gossipServerSocket.accept();
                // 启动心跳连接处理线程
                new HeartbeatHandlerThread(socketHeartbeat,this).start();
                // 启动Gossip处理线程
                new GossipHandlerThread(socketGossip,this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDaemonAddress(){
        return address;
    }

    public String getDaemonName(){
        return name;
    }

    public int getDaemonPort(){
        return port;
    }

    public List<String> getNeighbors(){
        return neighbors;
    }

    public Map<String,Long> getLastHeartbeatMap(){
        return lastHeartbeatMap;
    }


}








