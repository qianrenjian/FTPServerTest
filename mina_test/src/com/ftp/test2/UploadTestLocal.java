package com.ftp.test2;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
public class UploadTestLocal {

	public HashMap<String,FileInfo> localFileInfo=new HashMap<String,FileInfo>();
	public HashMap<String,FileInfo> remoteFileInfo=new HashMap<String,FileInfo>();
	
	public int checkFileName()
	{
		int res=1;
		return res;
	}
	
	public int checkFileSize()
	{
		int res=1;
		return res;
	}
	
	public int checkFileMd5()
	{
		int res=1;
		return res;
	}
	
	public static void run(JobConf jobConf) throws IOException
	{
		String localDir=jobConf.getProperty("localDir");
		String remoteDir=jobConf.getProperty("remoteDir");
		
		FtpClient ftpClient=new FtpClient();
		ftpClient.initConnect("42.123.90.71",2121,"hadp","123456");
		ftpClient.putDir(localDir, remoteDir);
		
		File local=new File(localDir);
		File[] localFiles = local.listFiles();
		for(File f:localFiles)
		{
			String name=f.getName();
			long size=f.length();
		}
		
	}
	
}
