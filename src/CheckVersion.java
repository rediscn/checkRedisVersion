
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * 自动检测redis最新版本
 */
public class CheckVersion{
	static HostnameVerifier hv = new HostnameVerifier() {  
        public boolean verify(String urlHostName, SSLSession session) {  
            System.out.println("Warning: URL Host: " + urlHostName + " vs. "  
                               + session.getPeerHost());  
            return true;  
        }  
    };  
      
    private static void trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
        javax.net.ssl.TrustManager tm = new miTM();  
        trustAllCerts[0] = tm;  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
                .getInstance("SSL");  
        sc.init(null, trustAllCerts, null);  
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc  
                .getSocketFactory());  
    }  
  
    static class miTM implements javax.net.ssl.TrustManager,  
            javax.net.ssl.X509TrustManager {  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
  
        public boolean isServerTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public boolean isClientTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public void checkServerTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
  
        public void checkClientTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
    }  
    
	/**
	 * 获取指定Url里面的html内容
	 * @param domain
	 * @return
	 */
	public static String getWebCon(String domain,String encoding) {
		// System.out.println("开始读取内容...("+domain+")");
		StringBuffer sb = new StringBuffer();
		try {
			java.net.URL url = new java.net.URL(domain);
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream(),encoding));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line + "\n\r");
			}
			in.close();
		} catch (Exception e) { // Report any errors that arise
			sb.append(e.toString());
			System.err.println(e);
			System.err.println("Usage:   java   HttpClient   <URL>   [<filename>]");
		}
		return sb.toString();
	}
	
	/**
	 * 获取指定Url里面的html内容
	 * @param domain
	 * @return
	 */
	public static String getWebCon1(String domain,String encoding) {
		HttpURLConnection connection = null;  
        try {  
        	java.net.URL url = new java.net.URL(domain);
        	
            connection = (HttpURLConnection)url.openConnection();
            final BufferedReader in = new BufferedReader(new InputStreamReader(  
                    connection.getInputStream()));  
  
            String line;  
            final StringBuffer stringBuffer = new StringBuffer(255);  
  
            synchronized (stringBuffer) {  
                while ((line = in.readLine()) != null) {  
                    stringBuffer.append(line);  
                    stringBuffer.append("\n");  
                }  
                return stringBuffer.toString();  
            }  
  
        } catch (final IOException e) {  
            e.printStackTrace();
            return null;  
        } catch (final Exception e1){  
            e1.printStackTrace();
            return null;  
        }finally {  
            if (connection != null) {  
                connection.disconnect();  
            }  
        }  
	}
	
	/**
	 * 解析html里面下载地址url
	 * @param html
	 * @return
	 */
	public static String getVersion(String html){
		String ver = "";
		// <a href="http://download.redis.io/releases/redis-3.2.8.tar.gz">Redis 3.2.8 is the latest stable version.</a>
		String startTag = "http://download.redis.io";
		String endTag = ".tar.gz";
		
		if(html!=null && !html.isEmpty()){
			int index = html.indexOf(startTag);
			
			if(index>0){
				ver = html.substring(index);
				
				if(ver.indexOf(endTag)>0){
					ver = ver.substring(0, ver.indexOf(endTag)+endTag.length());
				}
			}
		}
		
		return ver;
	}
	
	public static String getRedisVersion(String domain){
		String html = null; // getWebCon(domain,"UTF-8");
		//html = getWebCon(domain,"UTF-8");
		html = getWebCon1(domain,"UTF-8");
		// html = HttpClientUtil.httpGetRequest(domain);
		// System.out.println(html);
		return getVersion(html);
	}
	

    private MimeMessage mimeMsg; //MIME邮件对象   
    private Session session; //邮件会话对象   
    private Properties props; //系统属性   
    private boolean needAuth = false; //smtp是否需要认证   
    //smtp认证用户名和密码   
    private String username;   
    private String password;   
    private Multipart mp; //Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象   
       
    /** 
     * Constructor 
     * @param smtp 邮件发送服务器 
     */  
    public CheckVersion(String smtp){   
        setSmtpHost(smtp);   
        createMimeMessage();   
    }   
  
    /** 
     * 设置邮件发送服务器 
     * @param hostName String  
     */  
    public void setSmtpHost(String hostName) {   
        System.out.println("设置系统属性：mail.smtp.host = "+hostName);   
        if(props == null)  
            props = System.getProperties(); //获得系统属性对象    
        props.put("mail.smtp.host",hostName); //设置SMTP主机   
    }   
  
  
    /** 
     * 创建MIME邮件对象   
     * @return 
     */  
    public boolean createMimeMessage()   
    {   
        try {   
            System.out.println("准备获取邮件会话对象！");   
            session = Session.getDefaultInstance(props,null); //获得邮件会话对象   
        }   
        catch(Exception e){   
            System.err.println("获取邮件会话对象时发生错误！"+e);   
            return false;   
        }   
      
        System.out.println("准备创建MIME邮件对象！");   
        try {   
            mimeMsg = new MimeMessage(session); //创建MIME邮件对象   
            mp = new MimeMultipart();   
          
            return true;   
        } catch(Exception e){   
            System.err.println("创建MIME邮件对象失败！"+e);   
            return false;   
        }   
    }     
      
    /** 
     * 设置SMTP是否需要验证 
     * @param need 
     */  
    public void setNeedAuth(boolean need) {   
        System.out.println("设置smtp身份认证：mail.smtp.auth = "+need);   
        if(props == null) props = System.getProperties();   
        if(need){   
            props.put("mail.smtp.auth","true");   
        }else{   
            props.put("mail.smtp.auth","false");   
        }   
    }   
  
    /** 
     * 设置用户名和密码 
     * @param name 
     * @param pass 
     */  
    public void setNamePass(String name,String pass) {   
        username = name;   
        password = pass;   
    }   
  
    /** 
     * 设置邮件主题 
     * @param mailSubject 
     * @return 
     */  
    public boolean setSubject(String mailSubject) {   
        System.out.println("设置邮件主题！");   
        try{   
            mimeMsg.setSubject(mailSubject);   
            return true;   
        }   
        catch(Exception e) {   
            System.err.println("设置邮件主题发生错误！");   
            return false;   
        }   
    }  
      
    /**  
     * 设置邮件正文 
     * @param mailBody String  
     */   
    public boolean setBody(String mailBody) {   
        try{   
            BodyPart bp = new MimeBodyPart();   
            bp.setContent(""+mailBody,"text/html;charset=GBK");   
            mp.addBodyPart(bp);   
          
            return true;   
        } catch(Exception e){   
        System.err.println("设置邮件正文时发生错误！"+e);   
        return false;   
        }   
    }   
    /**  
     * 添加附件 
     * @param filename String  
     */   
    public boolean addFileAffix(String filename) {   
      
        System.out.println("增加邮件附件："+filename);   
        try{   
            BodyPart bp = new MimeBodyPart();   
            FileDataSource fileds = new FileDataSource(filename);   
            bp.setDataHandler(new DataHandler(fileds));   
            bp.setFileName(fileds.getName());   
              
            mp.addBodyPart(bp);   
              
            return true;   
        } catch(Exception e){   
            System.err.println("增加邮件附件："+filename+"发生错误！"+e);   
            return false;   
        }   
    }   
      
    /**  
     * 设置发信人 
     * @param from String  
     */   
    public boolean setFrom(String from) {   
        System.out.println("设置发信人！");   
        try{   
            mimeMsg.setFrom(new InternetAddress(from)); //设置发信人   
            return true;   
        } catch(Exception e) {   
            return false;   
        }   
    }   
    /**  
     * 设置收信人 
     * @param to String  
     */   
    public boolean setTo(String to){   
        if(to == null)return false;   
        try{   
            mimeMsg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));   
            return true;   
        } catch(Exception e) {   
            return false;   
        }     
    }   
      
    /**  
     * 设置抄送人 
     * @param copyto String   
     */   
    public boolean setCopyTo(String copyto)   
    {   
        if(copyto == null)return false;   
        try{   
        mimeMsg.setRecipients(Message.RecipientType.CC,(Address[])InternetAddress.parse(copyto));   
        return true;   
        }   
        catch(Exception e)   
        { return false; }   
    }   
      
    /**  
     * 发送邮件 
     */   
    public boolean sendOut()   
    {   
        try{   
            mimeMsg.setContent(mp);   
            mimeMsg.saveChanges();   
            System.out.println("正在发送邮件....");   
              
            Session mailSession = Session.getInstance(props,null);   
            Transport transport = mailSession.getTransport("smtp");   
            transport.connect((String)props.get("mail.smtp.host"),username,password);   
            transport.sendMessage(mimeMsg,mimeMsg.getRecipients(Message.RecipientType.TO));
            if(mimeMsg.getRecipients(Message.RecipientType.CC)!=null){
            	transport.sendMessage(mimeMsg,mimeMsg.getRecipients(Message.RecipientType.CC));  
            }
            //transport.send(mimeMsg);   
              
            System.out.println("发送邮件成功！");   
            transport.close();   
              
            return true;   
        } catch(Exception e) {   
            System.err.println("邮件发送失败！"+e);   
            e.printStackTrace();
            return false;   
        }   
    }   
  
    /** 
     * 调用sendOut方法完成邮件发送 
     * @param smtp 
     * @param from 
     * @param to 
     * @param subject 
     * @param content 
     * @param username 
     * @param password 
     * @return boolean 
     */  
    public static boolean send(String smtp,String from,String to,String subject,String content,String username,String password) {  
    	CheckVersion theMail = new CheckVersion(smtp);  
        theMail.setNeedAuth(true); //需要验证  
          
        if(!theMail.setSubject(subject)) return false;  
        if(!theMail.setBody(content)) return false;  
        if(!theMail.setTo(to)) return false;  
        if(!theMail.setFrom(from)) return false;  
        theMail.setNamePass(username,password);  
          
        if(!theMail.sendOut()) return false;  
        return true;  
    }  
      
    /** 
     * 调用sendOut方法完成邮件发送,带抄送 
     * @param smtp 
     * @param from 
     * @param to 
     * @param copyto 
     * @param subject 
     * @param content 
     * @param username 
     * @param password 
     * @return boolean 
     */  
    public static boolean sendAndCc(String smtp,String from,String to,String copyto,String subject,String content,String username,String password) {  
    	CheckVersion theMail = new CheckVersion(smtp);  
        theMail.setNeedAuth(true); //需要验证  
          
        if(!theMail.setSubject(subject)) return false;  
        if(!theMail.setBody(content)) return false;  
        if(!theMail.setTo(to)) return false;  
        if(!theMail.setCopyTo(copyto)) return false;  
        if(!theMail.setFrom(from)) return false;  
        theMail.setNamePass(username,password);  
          
        if(!theMail.sendOut()) return false;  
        return true;  
    }  
      
    /** 
     * 调用sendOut方法完成邮件发送,带附件 
     * @param smtp 
     * @param from 
     * @param to 
     * @param subject 
     * @param content 
     * @param username 
     * @param password 
     * @param filename 附件路径 
     * @return 
     */  
    public static boolean send(String smtp,String from,String to,String subject,String content,String username,String password,String filename) {  
    	CheckVersion theMail = new CheckVersion(smtp);  
        theMail.setNeedAuth(true); //需要验证  
          
        if(!theMail.setSubject(subject)) return false;  
        if(!theMail.setBody(content)) return false;  
        if(!theMail.addFileAffix(filename)) return false;   
        if(!theMail.setTo(to)) return false;  
        if(!theMail.setFrom(from)) return false;  
        theMail.setNamePass(username,password);  
          
        if(!theMail.sendOut()) return false;  
        return true;  
    }  
      
    /** 
     * 调用sendOut方法完成邮件发送,带附件和抄送 
     * @param smtp 
     * @param from 
     * @param to 
     * @param copyto 
     * @param subject 
     * @param content 
     * @param username 
     * @param password 
     * @param filename 
     * @return 
     */  
    public static boolean sendAndCc(String smtp,String from,String to,String copyto,String subject,String content,String username,String password,String filename) {  
    	CheckVersion theMail = new CheckVersion(smtp);  
        theMail.setNeedAuth(true); //需要验证  
          
        if(!theMail.setSubject(subject)) return false;  
        if(!theMail.setBody(content)) return false;  
        if(!theMail.addFileAffix(filename)) return false;   
        if(!theMail.setTo(to)) return false;  
        if(!theMail.setCopyTo(copyto)) return false;  
        if(!theMail.setFrom(from)) return false;  
        theMail.setNamePass(username,password);  
          
        if(!theMail.sendOut()) return false;  
        return true;  
    }  
	
	public static void main(String[] args) {
		try{
			System.out.println("开始检查redis最新版本");
			trustAllHttpsCertificates();  
	    	HttpsURLConnection.setDefaultHostnameVerifier(hv);  
	    	
			String redisioVer = getRedisVersion("https://redis.io/download");
			System.out.println("redisioVer:"+redisioVer);
			
			String rediscnVer = getRedisVersion("http://redis.cn");
			
			System.out.println("rediscnVer:"+rediscnVer);
			
			if(!redisioVer.equals(rediscnVer)){
				String smtp = "smtp.163.com";  
			    String from = "berqilee@163.com";  
			    String to = "13261990835@qq.com";  
			    String subject = "Redis最新版"+redisioVer.replaceAll("http://download.redis.io/releases/redis-", "").replaceAll(".tar.gz", "");  
			    String content = "Redis最新版本下载地址："+redisioVer;  
			    String username="berqilee@163.com";  
			    String password="yuanyuan";  
			    
			    CheckVersion.send(smtp, from, to, subject, content, username, password);
			}else{
				System.out.println("已经是最新版本");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("程序退出");
	}
}

