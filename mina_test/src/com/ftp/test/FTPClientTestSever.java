package com.ftp.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP Client工具类，封装了<a href="http://commons.apache.org/net/">Jakarta Commons Net
 * FTPClient</a>对常用的功能如上传、下载 等功能提供简易操作，如果需要Jakarta Commons Net
 * FTPClient的全部功能可以通过{@link #getFtpclient()}
 * 得到org.apache.commons.net.ftp.FTPClient
 * 对象，对于org.apache.commons.net.ftp.FTPClient的全部功能有请查看 <a href =
 * "http://commons.apache.org/proper/commons-net/javadocs/api-3.3/index.html" >
 * FTPClient API </a>
 * 
 * @since V2.0
 * @version V1.0 2015-9-17
 * @author 研发中心
 */
public class FTPClientTestSever {

	private static Log logger = LogFactory.getLog(FTPClient.class);

	private FTPClient ftpclient;
	MD5 md5 = new MD5();

	/**
	 * 实现匿名用户登陆
	 * 
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	public FTPClientTestSever(String host, int port) throws Exception {
		this(host, port, null, null);
	}

	/**
	 * 非匿名用户登陆，并进入被动模式
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public FTPClientTestSever(String host, int port, String username,
			String password) throws Exception {
		ftpclient = new FTPClient();
		// 文件名乱码,默认ISO8859-1，不支持中文
		ftpclient.setControlEncoding("UTF-8");
		try {
			ftpclient.connect(host, port);
			if (username != null) {
				if (!ftpclient.login(username, password)) {
					ftpclient.disconnect();
					logger.fatal("登陆验证失败，请检查账号和密码是否正确");
					throw new Exception("登陆验证失败，请检查账号和密码是否正确");
				}
			}

			ftpclient.setFileType(FTPClient.BINARY_FILE_TYPE);
			// 设置成被动模式
			ftpclient.enterLocalPassiveMode();
			ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
			int reply = ftpclient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpclient.disconnect();
				logger.fatal("FTP服务器拒绝连接");
				throw new Exception("FTP服务器拒绝连接");
			}

		} catch (SocketException e) {
			logger.fatal("无法连接至指定FTP服务器", e);
			throw new Exception(e);
		} catch (IOException e) {
			logger.fatal("无法用指定用户名和密码连接至指定FTP服务器", e);
			throw new Exception(e);
		}
	}

	/**
	 * 实现文件的上传到服务器的目录功能
	 * 
	 * @param distpath
	 *            服务器路径
	 * @param srcpath
	 *            本地路径
	 * @return true/false
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public boolean upload(String distPath, String srcPath) throws Exception {
		File file = new File(srcPath);
		
		mkdir(distPath);
		ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpclient.changeWorkingDirectory(distPath);
		// 如果srcPath是文件
		if (file.isFile()) {
			String fileName = file.getName();
			ftpclient.storeFile(new String(fileName.getBytes(),
					ftpclient.getControlEncoding()), new FileInputStream(file));
			String md5_1 = md5.getFileMD5String(file);
			String md5_2 = md5.getCodecMD5(new FileInputStream(file));
		}
		return false;
	}

	/**
	 * 在服务器端完成重命名功能
	 * @param oldRemotePath
	 * @param newRemotePath
	 * @return
	 * @throws Exception
	 * boolean
	 */
	public boolean rename(String oldRemotePath, String newRemotePath) throws Exception{
		return ftpclient.rename(oldRemotePath, newRemotePath);
	}
	
	/**
	 * 实现文件的上传并重命名功能
	 * 
	 * @param distpath
	 *            服务器目录
	 * @param fileName
	 *            新文件名
	 * @param srcFile
	 *            本地文件
	 * @return true/false
	 * @throws Exception
	 */
	public boolean upload(String distPath, String newFileName, String srcFile)
			throws Exception {
		File file = new File(srcFile);
		ftpclient.changeWorkingDirectory(distPath);
		if (file.isFile()) {
				return ftpclient.storeFile(new String(newFileName.getBytes(),
						ftpclient.getControlEncoding()), new FileInputStream(
						file));
		}
		return false;
	}

	/**
	 * 从FTP服务器上下载pathname指定的文件，命名为localName
	 * 
	 * @param pathname
	 * @param localName
	 * @return
	 * @throws Exception
	 */
	public boolean download(String pathname, String localName) throws Exception {
		String filename = localName != null ? localName : pathname
				.substring(pathname.lastIndexOf("/") + 1);

		if (filename == null || filename.isEmpty()) {
			return false;
		}

		// 设置被动模式
		ftpclient.enterLocalPassiveMode();
		// 设置以二进制方式传输
		ftpclient.setFileType(FTP.BINARY_FILE_TYPE);

		if (ftpclient.listFiles(new String(pathname.getBytes(), ftpclient
				.getControlEncoding())).length == 0) {
			logger.fatal("下载文件不存在");
			throw new Exception("下载文件不存在");
		}

		File tmp = new File(filename + "_tmp"); // 临时文件
		File file = new File(filename);
		FileOutputStream output = null;
		boolean flag;
		try {
			output = new FileOutputStream(tmp);
			flag = ftpclient.retrieveFile(new String(pathname.getBytes(),
					ftpclient.getControlEncoding()), output);
			output.close();
			if (flag) {
				// 下载成功,重命名临时文件。
				tmp.renameTo(file);
				System.out.println(file.getAbsolutePath());
			}
		} catch (FileNotFoundException e) {
			logger.fatal("下载文件失败", e);
			throw new Exception(e);
		} finally {
			output.close();
		}

		return flag;
	}

	/**
	 * 只删除文件,如果删除空目录请用如下方法： <code> 
	 *  getFtpclient().removeDirectory(String pathname)  
	 * </code> 参考 {@link org.apache.commons.net.ftp.FTPClient FTPClient}
	 * 
	 * @param pathname
	 * @return 成功删除返回true,否则返回false(如果文件不存在也返回false)
	 * @throws IOException
	 */
	public boolean delete(String pathname) throws IOException {
		return ftpclient.deleteFile(new String(pathname.getBytes(), ftpclient
				.getControlEncoding()));
	}

	/**
	 * 改变当前目录至pathname,"/"代表根目录
	 * 
	 * @param pathname
	 *            路径名
	 * @return 如果改变成功返true否则返回false
	 * @throws IOException
	 */
	public boolean changeWorkingDirectory(String pathname) throws IOException {
		return ftpclient.changeWorkingDirectory(new String(pathname.getBytes(),
				ftpclient.getControlEncoding()));
	}
	
	/**
	 * 
	 * @param pathname
	 *            要创建的目录路径，可以是相对路径，也可以是绝路径("/"开始)
	 * @return 如果成功创建目录返回true，否则返回false(如果目录已存在也返回false)
	 * @throws IOException
	 */
	public boolean mkdir(String pathname) throws IOException {
		// ftpclient.setControlEncoding("ISO-8859-1");
		// 注意编码，如果不编码文件中文目录无法创建
		String[] path = pathname.split("/");
		String dir = "";
		boolean result = false;
		for (int i = 1; i < path.length; i++) {
			dir += "/" + path[i];
			System.out.println(dir);
			result = ftpclient.makeDirectory(new String(dir.getBytes(),
					ftpclient.getControlEncoding()));
		}
		return result;
	}

	/**
	 * @return {@link org.apache.commons.net.ftp.FTPClient FTPClient}对象
	 */
	public FTPClient getFtpclient() {
		return this.ftpclient;
	}

	/**
	 * @param ftpclient
	 *            {@link org.apache.commons.net.ftp.FTPClient FTPClient}对象
	 */
	public void setFtpclient(FTPClient ftpclient) {
		this.ftpclient = ftpclient;
	}

	public void close() throws Exception {
		ftpclient.disconnect();
	}

	public static void main(String[] args) throws Exception {
		FTPClientTestSever ftputil = new FTPClientTestSever("42.123.90.71",
				2121, "hadp", "123456");
		ftputil.upload("/test/hanyanTest/", "./src/test.txt");
		// ftputil.upload("/test/hanyanTest", "123.java", "./src/test.txt");
		// ftputil.download("/drop.sql", "drop.sql");
		ftputil.close();
	}
}
