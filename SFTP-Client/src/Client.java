import java.io.*;
import java.net.*;
import java.util.*;
class Client {
	private static String serverHostName;
	private static int serverPort;
	private static String fileName;
	private static int windowSize;
	private static int mss;
	
	@SuppressWarnings("resource")
    public static void main(String args[]) throws Exception {
    	/***********************************GET INFO FROM COMMAND LINE*******************************************/
    	serverHostName = args[0];
    	serverPort = Integer.parseInt(args[1]);
    	fileName = args[2];
    	windowSize = Integer.parseInt(args[3]);
    	mss = Integer.parseInt(args[4]);
    	/***********************************GET INFO FROM COMMAND LINE END***************************************/
    	
    	
    	/***********************************READ FROM TEXT FILE*******************************************/
    	BufferedReader br = new BufferedReader(new FileReader(fileName));
		String rfcString = "";
		try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        rfcString = sb.toString();
	    } finally {
	        br.close();
	    }
		/***********************************READ FROM TEXT FILE END*******************************************/
		
		int var1 = mss*10;
		List<String> segStringArray = segmentationFunction(rfcString, var1);
		List<Segment> segmentArray = new ArrayList<Segment>();
    	Iterator<String> it = segStringArray.iterator();
    	int seqNum = 0;
    	while(it.hasNext()) {
			String segment = (String) it.next();
			segmentArray.add(new Segment(segment, var1, seqNum++));
		}

    	
		GoBackN gbn = new GoBackN(serverHostName, serverPort, segmentArray, windowSize);
		gbn.sendAndReceive();
		
		long startTime = System.currentTimeMillis() / 1000;
		System.out.println(startTime);
        
    }
	
	
	
	
    
    public static List<String> segmentationFunction(String rfcString, int mss) {
    	ByteArrayInputStream in = new ByteArrayInputStream (rfcString.getBytes());
    	byte[] buffer = new byte[mss];
    	List<String> segmentArray = new ArrayList<String>();
    	int len;
    	try {
			while ((len = in.read(buffer)) > 0) {
				byte[] buffer1;
				if(len < mss) {
					buffer1 = new byte[len];
					for(int i = 0; i < len; i++) {
						buffer1[i] = buffer[i];
					}
				} else {
					buffer1 = buffer;
				}
				String s = "";
				for (byte b : buffer1) {
			        s+= ((char) b);
			    }
				segmentArray.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return segmentArray;
    }
}
