package com.ftp.test;

import java.io.IOException;
import java.util.HashMap;

class JobConf {
	public String jobName;
	public HashMap<String, String> confArgs = new HashMap<String, String>();

	public JobConf(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--job-name=")) {
				jobName = arg.substring("--job-name=".length());
			} else {
				int idx = arg.indexOf("=");
				String key = arg.substring(2, idx);
				String val = arg.substring(idx + 1);
				confArgs.put(key, val);
			}
		}
	}

	public String getProperty(String propertyName) {
		return confArgs.get(propertyName);
	}
}

public class JobPannel {

	public static void run(String[] args) throws IOException {
		JobConf jobConf = new JobConf(args);
		if (jobConf.jobName.equals("FileInfo")) {
			FileInfo.run(jobConf);
		}
	}

	public static void main(String[] args) throws IOException {
		run(args);
	}
}
