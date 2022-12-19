import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;
import javax.xml.stream.*;
import java.io.File;
/**
 * @Description TODO: 查询功能类，包含：1.按姓名查询 2.按姓名和年份查询 两种查询功能。
 * @Author root
 * @Date 2022/12/09 16:00
 * @Version 1.0
 **/
public class Query {
    // 当前虚拟机端口
    private static int port;
    // log日志文件路径
    private static String LOG_Path;
    // log日志文件名称
    private static ArrayList<String> logFileNames=new ArrayList<String>();
    /**
     * @Description TODO: Query的构造函数
     * @return
     * @param portSelected
     * @Author root
     * @Date 2022/12/11 17:35
     * @Version 1.0
     **/
    Query(int portSelected){
        port=portSelected+100;
        LOG_Path = "/mnt/log/"+port;
        // 获取log文件的名称
        File dir = new File(LOG_Path);

        File[] xmlFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        // 存储log文件的名称
        for (File xmlFile : xmlFiles) {
            logFileNames.add(xmlFile.getName());
        }
    }

    /**
     * @Description TODO: 开启终端，执行传入的命令行，获得执行结果
     * @return
     * @param commandStr
     * @Author root
     * @Date 2022/12/09 16:00
     * @Version 1.0
     **/
    public static String exeCmd(String commandStr) {
        //执行Linux的Cmd命令
        String result = null;
        try {
            String[] cmd = new String[]{"/bin/sh", "-c", commandStr};
            Process ps = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                //执行结果加上回车
                sb.append(line);
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @Description TODO: 按照Type名进行查询
     * @return
     * @param type
     * @Author root
     * @Date 2022/12/09 17:28
     * @Version 1.0
     **/
    public static String queryByType(String type) {
        //根据Type进行查询
        String command = "";
        command = "grep -wo \"" + type + "\" " + LOG_Path+"/"+type+".log" + " |wc -l"; //按type查询，非模糊搜索
        String result = exeCmd(command);//命令执行结果
        return result;
    }
    /**
     * @Description TODO: 获取crashTime
     * @return
     * @Author root
     * @Date 2022/12/19 12:36
     * @Version 1.0
     **/
    public String getCrashTime() {
        String crashTime="";
        String path=LOG_Path+"/crash.log";
        // 创建 BufferedReader 对象
        BufferedReader reader = null;
        // 定义字符串变量来存储当前行的内容
        try {
            reader = new BufferedReader(new FileReader(path));
            // 跳过不需要的行
            for(int i=0;i<5;i++){
                reader.readLine();
            }
            // 得到crashTime
            crashTime=reader.readLine();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return crashTime;
    }

    public String getJoinTime() {
        String joinTime="";
        String path=LOG_Path+"/join.log";
        // 创建 BufferedReader 对象
        BufferedReader reader = null;
        // 定义字符串变量来存储当前行的内容
        try {
            reader = new BufferedReader(new FileReader(path));
            // 跳过不需要的行
            for(int i=0;i<5;i++){
                reader.readLine();
            }
            // 得到joinTime
            joinTime=reader.readLine();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return joinTime;
    }

    public void getChangedTime(String path,List<Long> changedTimestamps) {
        // 创建 BufferedReader 对象
        try {
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.equals("true")){
                    long changedTimestamp=0;
                    // 跳过两个无用行
                    scanner.nextLine();
                    scanner.nextLine();
                    // 得到MemberList发生改变时的Timestamp
                    changedTimestamp=Long.parseLong(scanner.nextLine());
                    changedTimestamps.add(changedTimestamp);
                    System.out.println("[Query]: 得到changedTimestamp "+changedTimestamp);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void getChangedTimestamps(List<Long> changedTimestamps){
        String gossipPath=LOG_Path+"/gossip.log";
        String offlinePath=LOG_Path+"/offline.log";
        // 得到gossip中改变memberList的TimeStamp
        if(logFileNames.contains("gossip.log")){
            getChangedTime(gossipPath,changedTimestamps);
        }
        // 得到offline中改变memberList的TimeStamp
        if(logFileNames.contains("offline.log")){
            getChangedTime(offlinePath,changedTimestamps);
        }
    }
}
