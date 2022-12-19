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

//    public static String queryByType(String type){
//        String xmlName=type+".xml";
//        // 获取日志文件块的名称
//        ArrayList<String> xmlNames=new ArrayList<String>();
//        File dir = new File(LOG_Path);
//
//        File[] xmlFiles = dir.listFiles(new FilenameFilter() {
//            public boolean accept(File dir, String name) {
//                return name.endsWith(".xml");
//            }
//        });
//        // 获取xml文件的名称
//        for (File xmlFile : xmlFiles) {
//            xmlNames.add(xmlFile.getName());
//        }
//        // 不存在当前类型的日志文件
//        if (!xmlNames.contains(xmlName)){
//            return "0";
//        }
//        try {
//            // 创建一个 XMLInputFactory
//            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//            // 创建一个 XMLStreamReader
//            String logFilePath=LOG_Path+"/"+type+".xml";
//            System.out.println("[Query]:Reading file: "+logFilePath);
//            XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileReader(logFilePath));
//            //创建一个字符串集合，包含DBLP数据库中所有可能的文章类型
//            Set<String> typeSet = new HashSet<>(Arrays.asList(
//                    type));
//            // 用于记录匹配的块的计数器
//            int matchedCounter = 0;
//
//            // 开始读取 XML 文档
//            while (reader.hasNext()) {
//                int event = reader.next();
//                switch (event) {
//                    case XMLStreamConstants.START_ELEMENT:
//                        // 如果是某个块的开头，则重置块信息
//                        if (typeSet.contains(reader.getLocalName())) {
//                            matchedCounter++;
//                        }
//                        break;
//                    case XMLStreamConstants.END_ELEMENT:
//                        break;
//                    case XMLStreamConstants.CHARACTERS:
//                        break;
//                }
//            }
//            // 关闭 XMLStreamReader
//            reader.close();
//            // 输出匹配的块的数量
//            //System.out.println(matchedCounter);
//            //次数转为字符串
//            String result = String.valueOf(matchedCounter);
//            //System.out.println("Finished file"+dblpBlockPath);
//            return result;
//        }
//        catch (FileNotFoundException | XMLStreamException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
}
