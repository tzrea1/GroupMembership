package groupservice;

/**
 * @description: 离线检查类，如果检查到有neighbor离线，就更新自己的neighbor
 * @author MXY
 * @date 12/17/22 5:44 PM
 * @version 1.0
 */
public class OfflineCheckThread extends Thread {
    private Daemon daemon;

    public OfflineCheckThread(Daemon daemon) {
        this.daemon=daemon;
    }
    public void run() {
        while (daemon.isRunning) {
            try {
                // 获取当前时间
                long currentTime = System.currentTimeMillis();
                // 遍历组成员列表
                for (int i=0;i<daemon.memberList.size();i++) {
                    // member是neighbor且存在于LastHeartbeatMap中
                    Member member=daemon.memberList.get(i);
                    if(daemon.getNeighbors().contains(member.getName())&&daemon.getLastHeartbeatMap().get(member.getName())!=null ){
                        System.out.println("[OfflineCheck]:正在检查邻居节点"+member.getName()+"是否离线");
                        // 获取节点的最后心跳时间
                        long lastHeartbeatTime = daemon.getLastHeartbeatMap().get(member.getName());

                        // 如果节点已经超过离线超时时间没有发送心跳消息，则认为该节点已经离线
                        if (currentTime - lastHeartbeatTime > daemon.OFFLINE_TIMEOUT) {
                            // 从组成员列表中删除该节点
                            daemon.memberList.remove(i);
                            System.out.println("[OfflineCheck]: "+member.getName()+"节点离线");
                            // 更新拓扑结构
                            daemon.findNeighbors();

                            // 更新心跳Map映射
                            daemon.getLastHeartbeatMap().remove(member.getName());

                            System.out.println("[OfflineCheck]: neighbor更新为: "+daemon.getNeighbors());
                            // 输出当前MemberList信息
                            System.out.println("[OfflineCheck]: 当前MemberList成员: ");
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
                            // 开启一次写Offline日志的线程 :对象保存的是离线的Server
                            new logWriteThread(daemon.getDaemonPort(),"offline",System.currentTimeMillis(),member.getName(),member.getAddress(),member.getPort(),true,daemon.memberList).start();
                        }
                    }
                }
                // 等待一段时间
                Thread.sleep(daemon.OFFLINE_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
