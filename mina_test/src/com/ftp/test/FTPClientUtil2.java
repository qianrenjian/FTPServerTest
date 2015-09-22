package com.ftp.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;

public class FTPClientUtil2 {

	private String userName; // FTP 登录用户名
	private String password; // FTP 登录密码
	private String ip; // FTP 服务器地址IP地址
	private int port; // FTP 端口
	private FTPClient ftpClient = null; // FTP 客户端代理

	public FTPClientUtil2(String userName, String password, String ip, int port) {
		this.userName = userName;
		this.password = password;
		this.ip = ip;
		this.port = port;
	}

	public boolean connectServer() {
		boolean flag = true;
		if (ftpClient == null) {
			int reply;
			try {
				ftpClient = new FTPClient();
				ftpClient.setControlEncoding("GBK"); // 文件名乱码,默认ISO8859-1，不支持中文
				// System.out.println("encoding "+ftpClient.getControlEncoding());
				ftpClient.setDefaultPort(port);
				ftpClient.connect(ip);
				ftpClient.login(userName, password);
				System.out.println("Connected to " + ip);
				System.out.print(ftpClient.getReplyString());
				reply = ftpClient.getReplyCode();
				ftpClient.setDataTimeout(120000);
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					System.err.println("FTP server refused connection.");
					flag = false;
				}
			} catch (SocketException e) {
				flag = false;
				e.printStackTrace();
				System.err.println("登录ftp服务器 " + ip + " 失败,连接超时！");
			} catch (IOException e) {
				flag = false;
				e.printStackTrace();
				System.err.println("登录ftp服务器 " + ip + " 失败，FTP服务器无法打开！");
			}
		}
		System.out.println("登陆ftp服务器成功" + ip);
		return flag;
	}

	public void listRemoteAllFiles(String path) {
		try {
			FTPListParseEngine f = ftpClient.initiateListParsing(path);

			while (f.hasNext()) {
				FTPFile[] files = f.getNext(5);
				for (FTPFile file : files) {
					disFile(file, path);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disFile(FTPFile file, String path) {
		if (file.isDirectory() && !file.getName().equals(".")
				&& !file.getName().equals("..")) {
			System.out.println(File.separator + file.getName());
			listRemoteAllFiles(path + File.separator + file.getName());
		} else if (!file.getName().equals(".") && !file.getName().equals("..")) {
			System.out.println(file.getName());
		}
	}

	public void closeConnect() {
		System.out.println("关闭ftp服务器");
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void downFile(String remotePath, String fileName, String localPath) {

		FTPFile[] fs;
		try {
			ftpClient.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
			fs = ftpClient.listFiles();
			for (FTPFile ff : fs) {
				if (ff.getName().equals(fileName)) {
					File localFile = new File(localPath + File.separator
							+ ff.getName());

					FileOutputStream is = new FileOutputStream(localFile);
					ftpClient.retrieveFile(ff.getName(), is);
					is.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void upFile(String path, String filename, String localFilePath) {
		try {
			FileInputStream in = new FileInputStream(new File(localFilePath));
			ftpClient.changeWorkingDirectory(path);
			ftpClient.storeFile(filename, in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		//FTPClientUtil2 ftp = new FTPClientUtil2("admin", "admin","192.168.1.173", 21);
		FTPClientUtil2 ftp = new FTPClientUtil2("hadp", "123456","42.123.90.71", 2121);
		ftp.connectServer();

		//ftp.listRemoteAllFiles("/test");
		// ftp.downFile("/测试报告", "测试报告.mmap",
		// "D:\\development\\workspace\\swing");
		//ftp.upFile("/test", "111.txt", "./src/test.txt");
		ftp.closeConnect(); 
		
	}
}