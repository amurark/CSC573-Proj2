import java.nio.ByteBuffer;

public class Util {
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
}
