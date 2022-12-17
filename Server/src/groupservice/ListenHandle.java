package groupservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
/**
 * @description: 每有一个新节点请求加入时，就开启一个此线程给新节点发memberList的内容
 * @author MXY
 * @date 12/17/22 5:19 PM
 * @version 1.0
 */
public class ListenHandle extends Thread{
    private Daemon daemon;
    private Socket socket;
    public ListenHandle(Socket socket,Daemon daemonTmp){
        this.daemon=daemonTmp;
        this.socket=socket;
    }
    public void run(){
        // 获取输入流
        try {
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os =new DataOutputStream(socket.getOutputStream());
            is.readUTF();
            int memberCount=0;
            for(String memberId : daemon.memberList){
                memberCount++;
            }
            os.writeInt(memberCount);
            os.flush();
            for(String memberId : daemon.memberList){
                os.writeUTF(memberId);
            }
            os.flush();
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
