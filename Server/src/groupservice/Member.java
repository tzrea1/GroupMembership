package groupservice;

/**
 * @Description TODO: Member 类来表示组成员，包含组成员的名称、IP 地址、端口号、时间戳信息
 * @Author root
 * @Date 2022/12/17 18:20
 * @Version 1.0
 **/
public class Member implements Comparable<Member> {
    private String name;
    private String address;
    private int port;
    private int portGossip;
    private long timestamp;

    public Member(String name, String address, int port,long timestamp) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.portGossip = port+100;
        this.timestamp=timestamp;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getPortGossip() {
        return portGossip;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setName(String name) {
        this.name=name;
    }

    public void setAddress(String address) {
        this.address=address;
    }

    public void setPort(int port) {
        this.port=port;
    }

    public void setTimeStamp(long timestamp) {
        this.timestamp=timestamp;
    }

    public int compareTo(Member other) {
        return Integer.parseInt(this.name) - Integer.parseInt(other.name);
    }

    /**
     * @Description TODO: 两个Member的Name、Port、Ip是否相等
     * @return boolean
     * @param member
     * @Author root
     * @Date 2022/12/17 23:55
     * @Version 1.0
     **/
    public boolean exist(Member member) {
        if(this.name!=member.getName()){
            return false;
        }
        if(this.address!=member.getAddress()){
            return false;
        }
        if(this.port!=member.getPort()){
            return false;
        }
        return true;
    }

    /**
     * @Description TODO: 两个Member是否完全相等
     * @return boolean
     * @param member
     * @Author root
     * @Date 2022/12/17 23:55
     * @Version 1.0
     **/
    public boolean equals(Member member) {
        if(this.name!=member.getName()){
            return false;
        }
        if(this.address!=member.getAddress()){
            return false;
        }
        if(this.port!=member.getPort()){
            return false;
        }
        if(this.timestamp!=member.getTimeStamp()){
            return false;
        }
        return true;
    }
}