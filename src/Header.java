import java.util.*;

public class Header {
	private static final Map<String, Short> packetTypes;
	private int seqNum;
	private short checksum;
	private short packetType;
	private byte[] headerBytes = new byte[8];
	static
    {
		packetTypes = new HashMap<String, Short>();
		packetTypes.put("Data", (short)21845);
		packetTypes.put("Ack", (short)-21846);
    }
	public Header(int seqNum, byte[] segmentStream, String pType ) {
		this.seqNum = seqNum;
		this.checksum = this.calculateCheckSum(segmentStream);
		this.packetType = packetTypes.get(pType);
		this.headerBytes = setHeaderBytes();
	}
	private short calculateCheckSum(byte[] segmentStream) {
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
	
	public byte[] setHeaderBytes() {
		byte[] hb = new byte[8];
		System.arraycopy(Util.intToByteArray(this.seqNum), 0, hb, 0, 4);
		System.arraycopy(Util.shortToByteArray(this.checksum), 0, hb, 4, 2);
		System.arraycopy(Util.shortToByteArray(this.packetType), 0, hb, 6, 2);
		//System.out.println(this.checksum);
		byte[] arr = Arrays.copyOfRange(hb, 6, 8);
		return hb;
	}
	
	public byte[] getHeaderBytes() {
		return headerBytes;
	}
}
