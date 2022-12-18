import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ServerMain {
    private final static String[] ipList = new String[]
            { "212.129.245.31", "1.15.143.17","101.35.155.147"};
    private final static int[] portList = new int[]
            {8820, 8821, 8822};
    /**
     * @Description TODO: 执行初始化流程，包括切分xml和将xml发送给各个虚拟机4
     * @return
     * @Author root
     * @Date 2022/12/11 18:14
     * @Version 1.0
     **/
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String name = null;
        int portSelected = -1;
        int deathTime = -1;
        boolean is_introducer = false;
        while (name == null) {
            System.out.println("请输入虚拟机name:采用数字0、1、2、3....");
            // 接收输入的name
            name = sc.nextLine();
            if (!(name.equals("0") || name.equals("1") || name.equals("2") || name.equals("3") || name.equals("4") || name.equals("5") || name.equals("6") || name.equals("7") || name.equals("8"))) {
                name = null;
                System.out.println("请重新输入");
            }
        }
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
        while (true) {
            System.out.println("是否将该虚拟机设为introducer?  yes：是；no：否");
            //接收输入的端口号
            String input = sc.nextLine();
            if (input.equals("yes")) {
                is_introducer = true;
            }
            if (input.equals("yes") || input.equals("no")) {
                break;
            } else {
                System.out.println("请重新输入");
            }
        }
        while (deathTime == -1) {
            System.out.println("请输入Death时间");
            // deathTime
            String inputStr = sc.nextLine();
            deathTime = Integer.parseInt(inputStr);
            if (deathTime < 0) {
                deathTime = -1;
                System.out.println("请重新输入");
            }
        }

        // 创建查询虚拟机线程: portSelected
        int port = portList[portSelected];
        VirtualServer vs = new VirtualServer(name, port + 100, is_introducer, deathTime);
        // 查询虚拟机线程
        Thread queryThread = new Thread(new Runnable() {
            public void run() {
                vs.receiveQuery();
            }
        });
        // 启动查询虚拟机线程
        queryThread.start();
        System.out.println("虚拟机已启动");

        if (deathTime!=0) {
            //在这里创建daemon的组服务// 组服务后台线程
            Thread daemon = new Thread(new Runnable() {
                public void run() {
                    vs.groupDaemon.startDaemon();
                }
            });
            // 启动组服务后台线程
            daemon.start();
            System.out.println("组服务后台已启动");
        }

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