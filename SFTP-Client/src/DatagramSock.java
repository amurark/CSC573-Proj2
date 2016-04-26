import java.net.*;
import java.net.*;
import java.util.*;

public class DatagramSock {
	private int seqNum;
	DatagramPacket packet;
	Timer t;
	public DatagramSock(int seqNum, DatagramPacket packet, Timer t) {
		this.seqNum =seqNum;
		this.packet = packet;
		this.t = t;
	}
	
	public int getSeqNum() {
		return seqNum;
	}
}
