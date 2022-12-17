package groupservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
/**
 * @description: 新节点向introducer发送加入申请，接收introducer发过来的memberList
 * @author MXY
 * @date 12/17/22 5:24 PM
 * @version 1.0
 */
public class JoinGroup extends Thread{
    private Daemon daemon;
    public JoinGroup(Daemon daemon){
        this.daemon=daemon;
    }
    public void run(){
        //创建Socket链接
        Socket socket = null;
        try {
            socket = new Socket("212.129.245.31", 9120);
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeUTF("Join in Group");
            os.flush();
            int memberCount=is.readInt();
            for(int j=0;j<memberCount;j++){
                String memberId=is.readUTF();
                //如果当前的memberList不包含该成员，就将其加入memberList并更新心跳时间
                if(!daemon.memberList.contains(memberId)){
                    daemon.memberList.add(memberId);
                    daemon.lastHeartbeatMap.put(memberId,System.currentTimeMillis());
                }
            }
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }
}
