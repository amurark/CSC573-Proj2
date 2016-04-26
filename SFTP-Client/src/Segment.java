public class Segment {
	private byte[] data;
	private Header header;
	private byte[] segmentInBytes;
	public Segment(String segment, int mss, int seqNum){
		data = new byte[mss];
		data = segment.getBytes();//MSS
		header = new Header(seqNum, data, "Data");
		segmentInBytes = setSegmentInBytes();
		
	}
	
	public byte[] setSegmentInBytes() {
		byte[] hb = new byte[data.length+header.getHeaderBytes().length];
		System.arraycopy(header.getHeaderBytes(), 0, hb, 0, header.getHeaderBytes().length);
		System.arraycopy(this.data, 0, hb, header.getHeaderBytes().length, this.data.length);
		return hb;
	}
	
	public byte[] getSegmentInBytes() {
		return segmentInBytes;
	}
}
