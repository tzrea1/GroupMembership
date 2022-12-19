import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.LinkedList;
/**
 * @Description TODO: Client的主类
 * @Author root
 * @Date 2022/12/09 15:56
 * @Version 1.0
 **/
public class ClientMain {
    /**
     * @Description TODO: 向服务器发送query请求（非备份）
     * @Author root
     * @Date 2022/12/15 17:19
     * @Version 1.0
     **/
    public static void sendQuerys(int[] numResults,String type,long[] joinTime,long[] crashTime,LinkedList<Long>[] timeList){
        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                numResults[0] = AccessServer.sendQuery(type, 0,0,0,joinTime,crashTime,timeList[0]);
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                numResults[1] = AccessServer.sendQuery(type, 0,1,1,joinTime,crashTime,timeList[1]);
            }
        });
        Thread thread3 = new Thread(new Runnable() {
            public void run() {
                numResults[2] = AccessServer.sendQuery(type, 1,0,2,joinTime,crashTime,timeList[2]);
            }
        });
        Thread thread4 = new Thread(new Runnable() {
            public void run() {
                numResults[3] = AccessServer.sendQuery(type, 1,1,3,joinTime,crashTime,timeList[3]);
            }
        });
        Thread thread5 = new Thread(new Runnable() {
            public void run() {
                numResults[4] = AccessServer.sendQuery(type, 2,0,4,joinTime,crashTime,timeList[4]);
            }
        });
        Thread thread6 = new Thread(new Runnable() {
            public void run() {
                numResults[5] = AccessServer.sendQuery(type, 2,1,5,joinTime,crashTime,timeList[5]);
            }
        });

        // 启动Send线程
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();

        // 等待线程进行
        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            thread5.join();
            thread6.join();
        } catch (Exception e) {
            System.out.println("thread error");
        }
    }
    /**
     * @Description TODO: Client的主函数，调用其他类，运行Client的逻辑
     * @return
     * @param args
     * @Author root
     * @Date 2022/12/09 15:56
     * @Version 1.0
     **/
    public static void main(String[] args) {
        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入查询Type,可选：1-heartbeat,2-gossip,3-join,4-offline,5-crash,6-查询memberList错误时长占比");

            String input = sc.nextLine();
            // Type
            String type="";
            // 退出标识:exit
            if (input.equals("exit"))
                return;
            else if(input.equals("1")){
                type="heartbeat";
            }
            else if(input.equals("2")){
                type="gossip";
            }
            else if(input.equals("3")){
                type="join";
            }
            else if(input.equals("4")) {
                type="offline";
            }
            else if(input.equals("5")) {
                type="crash";
            }
            else if(input.equals("6")){
                type="rate";
            }
            else{
                System.out.println("输入不合法，请重新输入");
            }
            System.out.println("正在查询.....");
            //创建计时
            long startTime = System.currentTimeMillis();
            // 调用Query.queryByType进行查询
            // 记录每台虚拟机的查询结果：-2为初始值，-1为连接失败
            int[] numResults = {-2, -2, -2, -2, -2, -2};
            long[] joinTime={0, 0, 0, 0, 0, 0};
            long[] crashTime={0, 0, 0, 0, 0, 0};
            // 存储每个Server组成员列表的Change时间
            LinkedList<Long>[] timeList=new LinkedList[6];
            for(int i=0;i<6;i++){
                timeList[i]=new LinkedList<>();
            }
            // 向各个服务器发送查询请求
            if(type.equals("rate")){
                sendQuerys(numResults,"time",joinTime,crashTime,timeList);
            }
            sendQuerys(numResults,type,joinTime,crashTime,timeList);


            // 输出用时
            long endTime = System.currentTimeMillis();
            System.out.println("查询成功! 用时：" + (double) (endTime - startTime) / 1000 + "s");
            // 输出查询结果
            if(type.equals("rate")) {
                // 计算memberList的错误时长占比
                // 记录真实发生变化的timeStamp
                LinkedList<Long> accurateTimestamps = new LinkedList<>();
                for (int i = 0; i < 6; i++) {
                    accurateTimestamps.add(joinTime[i]);
                    accurateTimestamps.add(crashTime[i]);
                }
                // 按照大小进行排序
                Collections.sort(accurateTimestamps);
                // 记录MemberList
                float rates[] = new float[6];
                // 对每个Server计算rate
                for (int i = 1; i < 6; i++) {
                    int beginIndex = accurateTimestamps.indexOf(joinTime[i]);
                    int endIndex = accurateTimestamps.indexOf(crashTime[i]);
                    int index = 0;
                    // 整个生命周期中，memberList错误的时长
                    long errorTime = 0;
                    for (int j = beginIndex+1; j <= endIndex-1; j++) {
                        System.out.println("accurateTimestamps为："+accurateTimestamps);
                        System.out.println(i+"号的joinTime为："+joinTime[i]+" crashTime为"+crashTime[i]);
                        System.out.println(i+"号的timeList为："+timeList[i]);
                        // 真实timestamp与日志timestamp的差值
                        long accurateTime = accurateTimestamps.get(j);
                        long logTime = timeList[i].get(index);
                        errorTime += logTime - accurateTime;
                        index++;
                    }
                    // 整个生命周期长度
                    long liveTime = crashTime[i] - joinTime[i];
                    // 计算得到错误率
                    rates[i] = (float) errorTime / liveTime;
                }
                for (int i = 0; i < rates.length; i++) {
                    System.out.println(i + "号虚拟机的MemberList错误率为：" + rates[i]*100+"%");
                }
            }
            else {
                for(int i=0;i<numResults.length;i++){
                    // 由于有标签头和标签尾，结果除2
                    System.out.println(i+"号虚拟机日志中记录的" + type + "信息条数为：" + numResults[i]/2);
                }
            }
            System.out.println();
        }
    }
}