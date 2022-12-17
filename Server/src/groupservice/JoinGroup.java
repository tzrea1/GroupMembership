package groupservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
//                if(memberId==null||memberId.equals("null")){
//                    break;
//                }
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
