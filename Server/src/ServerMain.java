import java.util.Scanner;
public class ServerMain {
    private final static String[] ipList = new String[]
            { "212.129.245.31", "1.15.143.17","101.35.155.147"};
    private final static int[] portList = new int[]
            {8820, 8821, 8822};
    /**
     * @Description TODO: 执行初始化流程，包括切分xml和将xml发送给各个虚拟机
     * @return
     * @Author root
     * @Date 2022/12/11 18:14
     * @Version 1.0
     **/
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int portSelected = -1;
        int is_introducer=-1;
        while (portSelected == -1) {
            System.out.println("请输入0/1/2选择虚拟机端口：0--8820, 1--8821, 2--8822");
            //接收输入的端口号
            String portStr = sc.nextLine();
            portSelected = Integer.parseInt(portStr);
            if ((portSelected != 0) && (portSelected != 1) && (portSelected != 2)) {
                portSelected = -1;
                System.out.println("请重新输入");
            }
        }
        while (is_introducer == -1) {
            System.out.println("是否将该虚拟机设为introducer?  1：是；2：否");
            //接收输入的端口号
            String input = sc.nextLine();
            is_introducer = Integer.parseInt(input);
            if ((is_introducer != 1) && (is_introducer != 2)) {
                is_introducer = -1;
                System.out.println("请重新输入");
            }
        }
        // 创建查询虚拟机线程: portSelected
        int port = portList[portSelected];
        VirtualServer vs = new VirtualServer(port + 100,is_introducer);
        // 查询虚拟机线程
        Thread queryThread = new Thread(new Runnable() {
            public void run() {
                vs.receiveQuery();
            }
        });
        // 启动查询虚拟机线程
        queryThread.start();
        System.out.println("虚拟机已启动");

        //在这里创建daemon的组服务// 组服务后台线程
        Thread daemon = new Thread(new Runnable() {
            public void run() {
                vs.groupDaemon.startDaemon();
            }
        });
        // 启动组服务后台线程
        daemon.start();
        System.out.println("组服务后台已启动");

        // 创建文件接收线程
        Initialize in = new Initialize();
        // 文件接收线程
        Thread receiveThread = new Thread(new Runnable() {
            public void run() {
                try {
                    in.receiveXml(port);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // 启动文件接收线程
        receiveThread.start();
    }
}