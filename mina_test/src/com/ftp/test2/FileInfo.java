package com.ftp.test2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class FileInfo {

	public long size;
	public String name;
	public Path path;
	public String md5;
	
	public FileInfo(String p)
	{
		this.path=new Path(p);
		name=path.getName();
	}
	
	public boolean compareName(FileInfo fi)
	{
		return name.equals(fi.name);
	}
	
	public String toString()
	{
		return String.format("{name:%s,size:%d,md5:%s,path:%s}",name,size,md5,path.toString());
	}
	
	public static void run(JobConf jobConf) throws IOException
	{
		String localPath=jobConf.getProperty("localPath");
		String hdfsPath=jobConf.getProperty("hdfsPath");
		LocalFileInfo lfi=new LocalFileInfo(localPath);
		HdfsFileInfo hfi=new HdfsFileInfo(hdfsPath);
		System.out.println(lfi);
		System.out.println(hfi);
	}
}

class HdfsFileInfo extends FileInfo{

	public HdfsFileInfo(String p) throws IOException {
		super(p);
		Configuration conf = new Configuration();  
//      conf.addResource(new Path("D:\\myeclipse\\Hadoop\\hadoopEx\\src\\conf\\hadoop.xml"));
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());  
		conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        FileSystem hdfs = FileSystem.get(conf);
		FileStatus fileStatus=hdfs.getFileStatus(path);
		size=fileStatus.getLen();
		FSDataInputStream fsdis = hdfs.open(path);
		md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fsdis));  
		IOUtils.closeQuietly(fsdis); 
	}
}

class LocalFileInfo extends FileInfo{

	public LocalFileInfo(String p) throws IOException {
		super(p);
		File f=new File(p);
		size=f.length();
		FileInputStream fis= new FileInputStream(f);  
		md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));  
		IOUtils.closeQuietly(fis);
	}
}
