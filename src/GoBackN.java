import java.io.IOException;
import java.net.*;
import java.util.*;

public class GoBackN {
	String serverHostName; 
	int serverPort; 
	List<Segment> segmentArray;
	private int currentAckdPacket = -1;
	int windowSize;
	LinkedList<DatagramSock> windowList;
	
	public GoBackN(String serverHostName, int serverPort, List<Segment> segmentArray, int windowSize) {
		this.serverHostName = serverHostName;
		this.serverPort = serverPort;
		this.segmentArray = segmentArray;
		this.windowSize = windowSize;
		this.windowList = new LinkedList<DatagramSock>();
	}
	
	public void sendAndReceive() {
		DatagramSocket clientSocket;
		try {
			clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(serverHostName);
			
			sendPacket(clientSocket, IPAddress);//Extra
			
	        clientSocket.close();
		} catch (SocketException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	
	public void sendPacket(DatagramSocket clientSocket, InetAddress IPAddress) {
		byte[] sendData;
		Iterator<Segment> it1 = segmentArray.iterator();
		sendData = it1.next().getSegmentInBytes();
		byte[] seqNumArr = new byte[4];
		int seqNum;
		System.arraycopy(sendData, 0, seqNumArr, 0, 4);
		seqNum = Util.byteArrayToInt(seqNumArr);
		while(true) {
			while((windowList.size() < windowSize) && it1.hasNext()) {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
		        try {
		        	Timer t = new Timer();
		        	Retransmit rt = new Retransmit(clientSocket, IPAddress, windowList, windowSize, seqNum , serverPort);
                    t.schedule(rt,3000);
		        	windowList.add(new DatagramSock(seqNum,sendPacket,t));
		        	System.out.println("trans:   "+seqNum);
		        	clientSocket.send(sendPacket);
		        	
                    
					sendData = it1.next().getSegmentInBytes();
					
					ReceiveAck ra = new ReceiveAck(clientSocket, IPAddress, windowList);
					ra.start();
					
					System.arraycopy(sendData, 0, seqNumArr, 0, 4);
					seqNum = Util.byteArrayToInt(seqNumArr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}        
			}
			
			if(!it1.hasNext()) {
				break;
			}
			
		}  
	}

}

class ReceiveAck extends Thread {
	DatagramSocket clientSocket;
	InetAddress IPAddress;
	LinkedList<DatagramSock> windowList;
	ReceiveAck(DatagramSocket clientSocket, InetAddress IPAddress, LinkedList<DatagramSock> windowList) {
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.windowList = windowList;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[204800]; 
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        try {
			clientSocket.receive(receivePacket);
			byte[] receivedData = new byte[receivePacket.getLength()];
			System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivedData.length);
			int seqNum = validateAckPacket(receivedData);
			while(true) {
				if(windowList.size() > 0) {
					if(windowList.getFirst().getSeqNum() <= seqNum) {
						windowList.getFirst().t.cancel();
						windowList.removeFirst();
					}
				} else {
					break;
				}
			}
			
			//System.out.println(receivedData[0]+" "+receivedData[1]+" "+receivedData[2]+" "+receivedData[3]+" "+receivedData[4]+" "+receivedData[5]+" "+receivedData[6]+" "+receivedData[7]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public int validateAckPacket(byte[] receivedData) {
		byte[] seqNumArr = new byte[4];
		byte[] zeroArr = new byte[2];
		byte[] packetTypeArr = new byte[2];
		int seqNum;
		short zero, packetType;
		System.arraycopy(receivedData, 0, seqNumArr, 0, 4);
		System.arraycopy(receivedData, 4, zeroArr, 0, 2);
		System.arraycopy(receivedData, 6, packetTypeArr, 0, 2);
		seqNum = Util.byteArrayToInt(seqNumArr);
		zero = Util.byteArrayToShort(zeroArr);
		packetType = Util.byteArrayToShort(packetTypeArr);
		return seqNum;
	}
}

class Retransmit extends TimerTask {
	DatagramSocket clientSocket;
	InetAddress IPAddress;
	LinkedList<DatagramSock> windowList;
	int windowSize, seqNum, serverPort;
	Retransmit(DatagramSocket clientSocket, InetAddress IPAddress, LinkedList<DatagramSock> windowList, int windowSize, int seqNum, int serverPort) {
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.windowList = windowList;
		this.windowSize = windowSize;
		this.seqNum = seqNum;
		this.serverPort = serverPort;
	}
	
	@Override
	public void run() {
		Iterator<DatagramSock> it2 = windowList.iterator();
		while(it2.hasNext()) {
			DatagramSock ds = it2.next();
			if(ds.getSeqNum() >= seqNum) {
				Timer t = new Timer();
	        	Retransmit rt = new Retransmit(clientSocket, IPAddress, windowList, windowSize, seqNum , serverPort);
                t.schedule(rt,3000);  
                try {
                	System.out.println("Retrans: "+seqNum);
    	        	clientSocket.send(ds.packet);
    				ReceiveAck ra = new ReceiveAck(clientSocket, IPAddress, windowList);
    				ra.start();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}  
			}
		}
	}
}
