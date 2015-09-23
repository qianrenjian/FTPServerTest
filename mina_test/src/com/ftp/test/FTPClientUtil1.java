package com.ftp.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
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
public class FTPClientUtil1 {

	private static Log logger = LogFactory.getLog(FTPClient.class);

	private FTPClient ftpclient;

	/**
	 * 实现匿名用户登陆
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	public FTPClientUtil1(String host, int port) throws Exception {
		this(host, port, null, null);
	}

	public FTPClientUtil1(String host, int port, String username, String password)
			throws Exception {
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
			//设置成被动模式
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
	 * 
	 * @param pathname
	 *            文件在FTP上存储的绝路径
	 * @param input
	 *            输入流
	 * @throws IOException
	 */
	public boolean upload(String pathname, InputStream input)
			throws IOException {
		// 是否是在根目录下
		ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
		if (pathname.indexOf("/") != -1) {
			String path = pathname.substring(0, pathname.lastIndexOf("/"));
			mkdir(path);
		}
		return ftpclient.storeFile(new String(pathname.getBytes(), ftpclient.getControlEncoding()), input);
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
	public boolean upload(String distPath, String srcPath) throws Exception {
		File file = new File(srcPath);

		ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
		mkdir(distPath);
		ftpclient.changeWorkingDirectory(distPath);
		// 如果srcPath是文件
		if (file.isFile()) {
			String fileName = file.getName();
			if (!isSameNameFile(distPath, fileName)) {
				return ftpclient.storeFile(new String(fileName.getBytes(), ftpclient.getControlEncoding()), new FileInputStream(file));
			} else {
				// 如果有重名文件如何处理，为同名的文件添加后缀。
				SimpleDateFormat myFmt = new SimpleDateFormat("yyMMddHHmmss");
				Date date = new Date();
				String suffix = myFmt.format(date);
				String newFileName = "";
				if (fileName.indexOf(".") >= 1)
					newFileName = fileName.substring(0,
							fileName.lastIndexOf("."))
							+ suffix
							+ fileName.substring(fileName.lastIndexOf("."));
				else
					newFileName = fileName + suffix;
				return ftpclient.storeFile(new String(newFileName.getBytes(),
						ftpclient.getControlEncoding()), new FileInputStream(
						file));
			}
		}
		// 如果srcPath是目录
		if (file.isDirectory()) {
			uploadDir(distPath, srcPath);
		}
		return false;
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
		mkdir(distPath);
		ftpclient.changeWorkingDirectory(distPath);
		if (file.isFile()) {
			if (!isSameNameFile(distPath, newFileName)) {
				return ftpclient.storeFile(new String(newFileName.getBytes(),
						ftpclient.getControlEncoding()), new FileInputStream(
						file));
			} else {
				// 如果有重名文件如何处理，为同名的文件添加后缀。
				SimpleDateFormat myFmt = new SimpleDateFormat("yyMMddHHmmss");
				Date date = new Date();
				String suffix = myFmt.format(date);
				String newFileName1 = "";
				if (newFileName.indexOf(".") >= 1)
					newFileName1 = newFileName.substring(0,
							newFileName.lastIndexOf("."))
							+ suffix
							+ newFileName.substring(newFileName
									.lastIndexOf("."));
				else
					newFileName1 = newFileName + suffix;
				return ftpclient.storeFile(new String(newFileName1.getBytes(),
						ftpclient.getControlEncoding()), new FileInputStream(
						file));
			}
		}
		System.out.println("上传的不是文件！");
		return false;
	}

	/**
	 * 实现目录中所有文件的上传，并保持目录的格式。
	 * 
	 * @param distPath
	 *            服务器目录
	 * @param srcPath
	 *            本地目录
	 * @return
	 * @throws Exception
	 *             boolean
	 */
	public boolean uploadDir(String distPath, String srcPath) throws Exception {
		File file = new File(srcPath);
		File[] fileList = file.listFiles();
		String srcFileName = "";
		for (File tFile : fileList) {
			mkdir(distPath);
			ftpclient.changeWorkingDirectory(distPath);
			srcFileName = srcPath + "/" + tFile.getName();
			File file2 = new File(srcFileName);
			// 判断子文件是目录吗？是，递归调用。
			if (file2.isDirectory()) {
				uploadDir(distPath + "/" + tFile.getName(), srcFileName);
			}
			if (file2.isFile()) {
				// 判断文件名是否相同
				if (!isSameNameFile(distPath, file2.getName())) {
					return ftpclient.storeFile(new String(file2.getName().getBytes(),
							ftpclient.getControlEncoding()),
							new FileInputStream(new File(srcFileName)));
				} else {
					// 如果有重名文件如何处理，为同名的文件添加后缀。
					SimpleDateFormat myFmt = new SimpleDateFormat(
							"yyMMddHHmmss");
					Date date = new Date();
					String suffix = myFmt.format(date);
					String newFileName = "";
					if (file2.getName().indexOf(".") >= 1)
						newFileName = file2.getName().substring(0,
								file2.getName().lastIndexOf("."))
								+ suffix
								+ file2.getName().substring(
										file2.getName().lastIndexOf("."));
					else
						newFileName = file2.getName() + suffix;
					return ftpclient.storeFile(new String(newFileName.getBytes(),
							ftpclient.getControlEncoding()),
							new FileInputStream(new File(srcFileName)));
				}
			}
		}
		return true;
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
	 * 遍历ftp服务器上的目录中是否含有同名的文件
	 * 
	 * @param distPath
	 * @param fileName
	 * @return
	 * @throws Exception
	 *             boolean
	 */
	public boolean isSameNameFile(String distPath, String fileName)
			throws Exception {
		FTPFile[] ftpFiles = ftpclient.listFiles(distPath);
		for (FTPFile ftpFile : ftpFiles) {
			if (ftpFile.getName().equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是不是本地下载文件与服务器上的MD5值是否相同
	 * 
	 * @param distPath
	 * @param fileName
	 * @return
	 * @throws Exception
	 *             boolean
	 */
	@SuppressWarnings({ "resource", "static-access" })
	public boolean isSameFile(String distPath, String srcPath) throws Exception {
		File file = new File(srcPath);
		MD5 md5 = new MD5();
		if (file.exists()) {
			String md5Str = md5.getFileMD5String(file);
			if (download(distPath + ".md5", "md5")) {
				File fileMd5 = new File("./md5");
				StringBuffer sb = new StringBuffer();
				FileInputStream fis = new FileInputStream(fileMd5);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));
				String tempstr = "";
				while ((tempstr = br.readLine()) != null)
					sb.append(tempstr);
				if (md5Str.equals(sb.toString())) {
					return true;
				}
			} else {
				System.out.println("服务器上没有相应的MD5文件。");
			}
		} else {
			System.out.println("本地文件不存在");
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		// URI uri = new URI(
		// "ftp://100.180.185.205/oracle_10201_database_win32/database/welcome.html");
		// System.out.println(uri.toURL().getHost());
		// System.out.println(uri.toURL().getPath());
		// System.out.println(uri.toURL().getFile());

		//FTPClientUtil ftputil = new FTPClientUtil("192.168.1.173", 21, "admin","admin");
		FTPClientUtil1 ftputil = new FTPClientUtil1("42.123.90.71", 2121, "hadp",	"123456");
		// System.out.println(ftputil.mkdir("/测试/2"));

		//System.out.println(ftputil.isSameFile("/测试/测试报告",	"./src/log4j.properties"));
		  ftputil.upload("/test/hanyanTest/", "./src/test.txt");
		 //ftputil.upload("/test/hanyanTest", "123.java", "./src/test.txt");
		// ftputil.download("/drop.sql", "drop.sql");
		// FTPClient ftp = ftputil.getFtpclient();
		// FTPListParseEngine engine = ftp.initiateListParsing();
		// System.out.println(ftp.listFiles().length);
		// while (engine.hasNext()) {
		// FTPFile[] files = engine.getNext(25); // "page size" you want
		// }

		// FTPClient ftpclient = ftputil.getFtpclient();
		//
		// FTPFile[] f = ftpclient.listFiles("/kk/aa/drop2.sql");
		// System.out.println(f.length);
		// System.out.println(ftpclient.makeDirectory("/tt/aa/bb"));
		// System.out.println(ftpclient.getReplyString());
		// ftpclient.changeWorkingDirectory("/tt/aa/bb");
		//
		// //System.out.println(ftpclient.deleteFile("/tt/aa/cc/"));
		// System.out.println(ftpclient.getReplyString());
		// ftpclient.changeWorkingDirectory("/");
		// System.out.println(ftpclient.listFiles().length);

		// System.out.println("a".lastIndexOf("/"));
		ftputil.close();
	}
}
