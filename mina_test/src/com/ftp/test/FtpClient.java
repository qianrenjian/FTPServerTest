package com.ftp.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClient {
	
	private FTPClient client;

	public void initConnect(String host,int port,String user,String pwd) throws IOException
	{
		client = new FTPClient();
        client.connect(host,port);
        client.login(user,pwd);
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();
        client.setFileType(FTP.BINARY_FILE_TYPE);
        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            System.out.println("disconnect");
            return;
        }
	}
	
	public int putFile(String localFile,String remoteDir)
	{
		int res=1;
		String fileName=getFileName(localFile);
		String remoteFile=joinPath(remoteDir,fileName);
		try {
			InputStream inputStream = new FileInputStream(localFile);
			client.storeFile(remoteFile, inputStream);
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=-1;
		}
		return res;
	}
	
	public int putDir(String localDir,String remoteDir)
	{
		int res=1;
		File localPath=new File(localDir);
		String localName=localPath.getName();
		remoteDir=joinPath(remoteDir,localName);
		try {
			client.makeDirectory(remoteDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=-1;
		}
		
		File[] fileArr= localPath.listFiles();
		for(File f:fileArr)
		{
			String localFile=f.getPath();
			if(f.isDirectory())
			{
				putDir(localFile,remoteDir);
			}
			else
			{
				if(putFile(localFile,remoteDir)==-1)
				{
					res=-1;
				}
			}
		}
		return res;
	}
	
	public void printFiles(String remotePath,int deepth) throws IOException
	{
		FTPFile[] fileArr=client.listFiles(remotePath);
		String prefix=deepth==0?"":String.format(String.format("%%-%ds",deepth*2),"");
		for(FTPFile file:fileArr)
		{
			
			if(file.isFile())
			{
				System.out.println(prefix+">"+file.getName());
			}
			else
			{
				System.out.println(prefix+"+"+file.getName());
				String subPath=remotePath+"/"+file.getName();
				printFiles(subPath,deepth+1);
			}
		}
	}
	
	public void printFiles(String remotePath) throws IOException
	{
		printFiles(remotePath,0);
	}
	
	public int getDir(String remoteDir,String localDir)
	{
		int res=1;
		localDir=joinPath(localDir,getFileName(remoteDir));
		File localFile=new File(localDir);
		if(!localFile.isDirectory())
		{
			localFile.mkdirs();
		}
		try {
			FTPFile[] fileArr = client.listFiles(remoteDir);
			for(FTPFile file:fileArr)
			{	
				if(file.isDirectory())
				{
					String nextRemoteDir=joinPath(remoteDir,file.getName());
					getDir(nextRemoteDir,localDir);
				}
				else
				{
					String remoteFileName=joinPath(remoteDir,file.getName());
					if(getFile(remoteFileName, localDir)==-1)
					{
						res=-1;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=-1;
		}
		return res;
	}
		
	public int getFile(String remoteFile,String localDir)
	{
		int res=1;
		File localPath=new File(localDir);
		if(!localPath.isDirectory()) localPath.mkdirs();
		String remoteFileName=getFileName(remoteFile);
		File localFile=new File(joinPath(localDir,remoteFileName));
		try {
			OutputStream outputStream = new FileOutputStream(localFile);
			client.retrieveFile(remoteFile,outputStream);
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=-1;
		}
		return res;
	}
	
	public int delete(String remoteFile)
	{
		int res=1;
		try {
			client.deleteFile(remoteFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=-1;
		}
		return res;
	}
	
	public int rename(String oldRemotePath,String newReomtePath)
	{
		int res=1;
		try {
			client.rename(oldRemotePath, newReomtePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			res=-1;
		}
		return res;
	}
	
	public static String joinPath(String path1,String path2)
	{
		return path1.endsWith("/")?path1+path2:path1+"/"+path2;
		
	}
	
	public static String getFileName(String path)
	{
		return Paths.get(path).getFileName().toString();
	}
	
	public static void main(String[] args) throws IOException
	{
		FtpClient ftpClient=new FtpClient();		
		ftpClient.initConnect("42.123.90.71",2121,"hadp","123456");
		ftpClient.delete("/test/test.txt");
		System.out.println(ftpClient.putFile("./src/test.txt", "/test"));
		
	}
}
