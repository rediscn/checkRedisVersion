package checkRedisVersion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
	public static String toString(InputStream instream,String encoding){
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(instream,encoding));
			StringBuffer buffer = new StringBuffer();
			String line = " ";
			while ((line = in.readLine()) != null){
			     buffer.append(line);
			}
			in.close();
			in=null;
			return buffer.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(in!=null){
				try{
					in.close();
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		return null;
	}
}
