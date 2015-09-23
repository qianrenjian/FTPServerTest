package com.ftp.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5 {
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6','7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	protected static MessageDigest messageDigest = null;
	static {
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsaex) {
			System.err.println(MD5.class.getName()+"初始化失败，MessageDigest不支持MD5!");
			nsaex.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public static String getFileMD5String(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		FileChannel ch = in.getChannel();
		
		//700000000 bytes are about 670M
		int maxSize=700000000;
		
		long startPosition=0L;
		long step=file.length()/maxSize;
		
		if(step == 0){
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,file.length());
			messageDigest.update(byteBuffer);
			return bufferToHex(messageDigest.digest());
		}
		
		for(int i=0;i<step;i++){
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, startPosition,maxSize);
			messageDigest.update(byteBuffer);
			startPosition+=maxSize;
		}
		
		if(startPosition==file.length()){
			return bufferToHex(messageDigest.digest());
		}

		MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, startPosition,file.length()-startPosition);
		messageDigest.update(byteBuffer);
		
			
		return bufferToHex(messageDigest.digest());
	}

	public static String getMD5String(String s) {
		return getMD5String(s.getBytes());
	}

	public static String getMD5String(byte[] bytes) {
		messageDigest.update(bytes);
		return bufferToHex(messageDigest.digest());
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static boolean checkPassword(String password, String md5PwdStr) {
		String s = getMD5String(password);
		return s.equals(md5PwdStr);
	}
	
	//计算MD5值并写入文件中
	
	public String getCodecMD5(InputStream inputStream) throws Exception{
		String md5 = "";
		md5 = DigestUtils.md5Hex(inputStream);
		return md5;
	}
	
	public String getCodecMD5(String filePath, String tempFile) throws Exception{
		String md5 = "";
		File temp = new File(tempFile);
		if(!temp.exists()){
			temp.createNewFile();
		}
		md5 = DigestUtils.md5Hex(new FileInputStream(filePath));
		FileOutputStream out=new FileOutputStream(temp,false);
		out.write(md5.toString().toLowerCase().getBytes("utf-8"));
		out.close();
		return tempFile;
	}
	
	public static void main(String[] args) throws IOException {
		long begin = System.currentTimeMillis();

		File big = new File("./src/测试报告");
	    String md5 = getFileMD5String(big);
		//String md5 = getMD5String("a");
		long end = System.currentTimeMillis();
		System.out.println("md5:" + md5 + " time:" + ((end - begin) / 1000) + "s");
	}
	
}

