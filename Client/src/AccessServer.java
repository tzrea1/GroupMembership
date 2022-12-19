import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @Description TODO: Client访问Server时使用此类
 * @Author root
 * @Date 2022/12/09 15:52
 * @Version 1.0
 **/
public class AccessServer {
    /**
     * ip列表，记录可以访问的Server的ip地址
     */
    private final static String[] ipList = new String[]
            { "212.129.245.31", "1.15.143.17","101.35.155.147"};
    /**
     * port列表，记录可以访问的Server的端口
     */
    private final static int[] portList = new int[]
            {8920, 8921, 8922};
    /**
     * @Description TODO: 向指定的Server发送查询信息（Type），并获得查询结果
     * @return
     * @Author root
     * @Date 2022/12/09 15:55
     * @Version 1.0
     **/
    public static int sendQuery(String type, int ipSelected, int portSelected, int serverName, long[] joinTime, long[] crashTime, LinkedList<Long> timeList){
        int num=0;
        try {
            //创建Socket链接
            Socket socket = new Socket(ipList[ipSelected], portList[portSelected]);
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            //向Server传递type信息
            os.writeUTF(type);
            os.flush();

            // 接收服务端的查询信息
            if(type.equals("time")){
                // 得到joinTime信息并记录
                String joinTimeStr=is.readUTF();
                joinTime[serverName]=Long.parseLong(joinTimeStr);
                // 得到crashTime信息并记录
                String crashTimeStr=is.readUTF();
                crashTime[serverName]=Long.parseLong(crashTimeStr);
            }
            else if(type.equals("rate")){
                // 得到changedNum信息并记录
                int changedNum=Integer.parseInt(is.readUTF());
                // 循环接收changeTimestamp
                for(int i=0;i<changedNum;i++){
                    String readTimestamp= is.readUTF();
                    //System.out.println("timeList "+ readTimestamp);
                    timeList.add(Long.parseLong(readTimestamp));
                }
                timeList.sort(null);
            }
            else{
                String queryResult = is.readUTF();
                // 查询到的次数
                num = Integer.parseInt(queryResult);
            }

            //关闭Socket链接
            is.close();
            os.close();
            socket.close();

        } catch (IOException e) {
            //e.printStackTrace();
            return -1;
        }
        return num;
    }
}
