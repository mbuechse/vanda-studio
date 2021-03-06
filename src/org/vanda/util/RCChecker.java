package org.vanda.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class RCChecker {
	private static String basePath = System.getProperty("user.home") + "/.vanda/";
	private static String rcPath = basePath + "vandarc";
	private static String funcPath = basePath + "functions";
	private static String inPath = basePath + "input";
	private static String outPath = basePath + "output";
	
	public static String getOutPath() {
		return outPath;
	}
	
	public static void readRC() {
		if (ensureRC()) {
			//TODO read path vars from file
		}
	}
	
	public static boolean ensureRC() {
		File base = new File(basePath);
		if (!base.exists()){
			if (!base.mkdirs())
				return false;
		}
		
		File rc = new File(rcPath);
		if (!rc.exists()){
			try {
				rc.createNewFile();
				PrintWriter out = new PrintWriter(rc);
				out.println("#!/bin/bash");
				out.println();
				out.println("DATAPATH=" + inPath);
				out.println("OUTPATH=" + outPath);
				out.println("FUNCDIR=" + funcPath);
				out.println();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}
		
		File func = new File(funcPath);
		if (!func.exists()){
			if (!func.mkdirs())
				return false;
		}
		
		File in = new File(inPath);
		if (!in.exists()){
			if (!in.mkdirs())
				return false;
		}
		
		File out = new File(outPath);
		if (!out.exists()){
			if (!out.mkdirs())
				return false;
		}
		
		return true;
	}
}
