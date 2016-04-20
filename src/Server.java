import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Server {
	private static final int portNum = 7735;//TODO:change to argument.
	private static final String fileName = "response.txt"; 
	private static int currentSeqNumber = 0;
	private static File file;
	private static final Map<String, Short> packetTypes;
	
	static DatagramSocket serverSocket;
	static
    {
		packetTypes = new HashMap<String, Short>();
		packetTypes.put("Data", (short)21845);
		packetTypes.put("Ack", (short)-21846);
		file = new File(fileName);
    }
	
    public static void main(String args[]) throws Exception {
        serverSocket = new DatagramSocket(portNum);

        System.out.println("Server listening on port: "+serverSocket.getLocalPort());
        while (true) {
            byte[] buffer = new byte[204800]; 
			DatagramPacket receivePacket =new DatagramPacket(buffer, buffer.length);
			//System.out.println(receivePacket.getAddress());
			serverSocket.receive(receivePacket);
			byte[] receivedData = new byte[receivePacket.getLength()];
			//System.out.println(receivePacket.getLength());
			
			System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivedData.length);
			//System.out.println(byteArrayToInt(receivedData));
			
			
			processPacket(receivedData, receivePacket);
        }
        //serverSocket.close();
    }
    
    public static void processPacket(byte[] packet, DatagramPacket receivePacket){
    	byte[] headerBytes = new byte[8];
		byte[] dataBytes = new byte[packet.length-8];
		
		System.arraycopy(packet, 0, headerBytes, 0, 8);
		System.arraycopy(packet, 8, dataBytes, 0, packet.length-8);
		
    	byte[] seqNum = new byte[4];
    	byte[] checkSum = new byte[2];
    	byte[] packetType = new byte[2];
    	System.arraycopy(packet, 0, seqNum, 0, 4);
    	System.arraycopy(packet, 4, checkSum, 0, 2);
    	System.arraycopy(packet, 6, packetType, 0, 2);
    	
    	int seqNumInInt = byteArrayToInt(seqNum);
    	short checkSumInShort = byteArrayToShort(checkSum);
    	short packTypeInShort = byteArrayToShort(packetType);
    	
    	short dataCheckSum =  calculateCheckSum(dataBytes);
    	System.out.println(seqNumInInt+" "+checkSumInShort+" "+packTypeInShort+" "+dataCheckSum);
    	boolean isValid = validateData(seqNumInInt, checkSumInShort, packTypeInShort, dataCheckSum);
    	if(isValid) {
    		currentSeqNumber++;
    		//TODO: Add to file
    		appendToFile(dataBytes);
    		//Send Response
    		sendAcknowledgement(receivePacket);
    	}
    }
    
    public static boolean validateData(int seqNumInInt, short checkSumInShort, short packTypeInShort, short dataCheckSum) {
    	boolean isValid = true;
    	if(checkSumInShort != dataCheckSum) {
    		isValid = false;
    	}
    	if(seqNumInInt != currentSeqNumber) {
    		isValid = false;
    	}
    	if(packTypeInShort != packetTypes.get("Data")) {
    		isValid = false;
    	}
    	return isValid;
    }
    
    public static void sendAcknowledgement(DatagramPacket receivePacket) {
    	byte[] seqNumByteStream = intToByteArray(currentSeqNumber);
    	byte[] zeroByteStream = shortToByteArray((short)0);
    	byte[] packetTypeStream = shortToByteArray(packetTypes.get("Ack"));
    	
    	byte[] acknowledgement = new byte[8];
    	System.arraycopy(seqNumByteStream, 0, acknowledgement, 0, 4);
    	System.arraycopy(zeroByteStream, 0, acknowledgement, 4, 2);
    	System.arraycopy(packetTypeStream, 0, acknowledgement, 6, 2);
    	
    	DatagramPacket sendPacket = new DatagramPacket(acknowledgement, acknowledgement.length, receivePacket.getAddress(),receivePacket.getPort());
    	try {
    		//if(currentSeqNumber == 2) {
    			serverSocket.send(sendPacket);
    		//}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void appendToFile(byte[] dataBytes) {
    	BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(new String(dataBytes));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }
    
    public static byte[] intToByteArray(int val) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(val);
		return b.array();
	}
	
	public static byte[] shortToByteArray(short val) {
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort(val);
		return b.array();
	}
    
    public static int byteArrayToInt(byte[] arr) {
    	ByteBuffer wrapped = ByteBuffer.wrap(arr);
    	int num = wrapped.getInt();
    	return num;
    }
    
	public static short byteArrayToShort(byte[] arr) {
    	ByteBuffer wrapped = ByteBuffer.wrap(arr);
    	short num = wrapped.getShort();
    	return num;
    }
	
	public static short calculateCheckSum(byte[] segmentStream) {
		int length = segmentStream.length;
		int i = 0;
	    long sum = 0;
	    while (length > 0) {
	        sum += (segmentStream[i++]&0xff) << 8;
	        if ((--length)==0) break;
	        sum += (segmentStream[i++]&0xff);
	        --length;
	    }
	    return (short) ((~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF);
	}
}
