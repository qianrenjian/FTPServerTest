package com.ftp.test;

import java.io.File;

public class Test {
	public static void main(String[] args) {
		File file = new File("test.txt");
		System.out.println(file.getAbsolutePath());
	}
}
