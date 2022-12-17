package groupservice;

//离线检查类
public class OfflineCheckThread extends Thread {
    private Daemon daemon;

    public OfflineCheckThread(Daemon daemon) {
        this.daemon=daemon;
    }
    public void run() {
        while (true) {
            try {
                // 获取当前时间
                long currentTime = System.currentTimeMillis();
                // 遍历组成员列表
                for (String memberId : daemon.memberList) {
                    System.out.println(memberId);
                    // 获取节点的最后心跳时间
                    long lastHeartbeatTime = daemon.lastHeartbeatMap.get(memberId);

                    // 如果节点已经超过离线超时时间没有发送心跳消息，则认为该节点已经离线
                    if (currentTime - lastHeartbeatTime > daemon.OFFLINE_TIMEOUT) {
                        // 从组成员列表中删除该节点
                        daemon.memberList.remove(memberId);
                        System.out.println("有节点离线");
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
