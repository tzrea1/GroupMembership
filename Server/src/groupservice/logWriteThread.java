package groupservice;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class logWriteThread extends Thread{
    int selfPort;
    XMLStreamWriter writer;
    String type;
    long timeStamp;
    String serverName;
    String serverIp;
    int serverPort;
    boolean isChanged;
    List<Member> memberList;
    public logWriteThread(int selfPort,String type, long timeStamp, String serverName, String serverIp, int serverPort,boolean isChanged, List<Member> memberList){
        this.selfPort=selfPort;
        this.isChanged=isChanged;
        this.serverPort=serverPort;
        this.serverIp=serverIp;
        this.type=type;
        this.serverName=serverName;
        this.memberList=memberList;
        this.timeStamp=timeStamp;
    }
    public void run(){
        try {
           //建立日志写文件流
            try {
                String currentFile="/mnt/log/"+this.selfPort+"/"+this.type+".log";
                XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
                writer = outputFactory.createXMLStreamWriter(new FileWriter(currentFile,true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            writer.writeStartElement(this.type);
            writer.writeCharacters("\n");
            writer.writeStartElement("changed");
            writer.writeCharacters("\n"+Boolean.toString(this.isChanged)+"\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeStartElement("timestamp");
            writer.writeCharacters("\n"+this.timeStamp+"\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");

            if(!type.equals("gossip")) {
                writer.writeStartElement("object");
                writer.writeCharacters("\n");
                writer.writeStartElement("name");
                writer.writeCharacters(this.serverName);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("ip");
                writer.writeCharacters(this.serverIp);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("port");
                writer.writeCharacters(this.serverPort + "");
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }


            writer.writeStartElement("memberList");
            writer.writeCharacters("\n");
            for(int i=0;i<this.memberList.size();i++) {
                writer.writeStartElement("member");
                writer.writeCharacters("\n");

                writer.writeStartElement("memberName");
                writer.writeCharacters(this.memberList.get(i).getName());
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("memberIp");
                writer.writeCharacters(this.memberList.get(i).getAddress());
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("memberPort");
                writer.writeCharacters(Integer.toString(this.memberList.get(i).getPort()));
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeStartElement("memberTimestamp");
                // 使用 formatter 将时间戳格式化为日期字符串
                // 创建一个 SimpleDateFormat 实例
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String memberDateString = formatter.format(this.memberList.get(i).getTimeStamp());
                writer.writeCharacters(memberDateString);
                writer.writeEndElement();
                writer.writeCharacters("\n");

                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");

            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeCharacters("\n");

            writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }


}
