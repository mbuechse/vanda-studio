package org.vanda.studio.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class RCChecker {
	public static boolean ensureRC() {
		String basePath = System.getProperty("user.home") + "/.vanda/";
		String rcPath = basePath + "vandarc";
		String funcPath = basePath + "functions.bash";
		String inPath = basePath + "input";
		String outPath = basePath + "output";

		
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
				out.println("PROGSPATH=/usr/bin");
				out.println("DATAPATH=" + inPath);
				out.println("FUNCFILE=" + funcPath);
				out.println();
				out.println("source \"$FUNCFILE\"");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}
		
		File func = new File(funcPath);
		if (!func.exists()){
			String cp = System.getProperty("java.class.path").split(":")[0];
			if (!localCopy(cp + "/../shared/scripts/functions.bash", funcPath))
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
	
	public static boolean localCopy(String sourcePath, String targetPath) {
		try {
			InputStream in = new FileInputStream(new File(sourcePath));
			OutputStream out = new FileOutputStream(new File(targetPath));
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;		
	}
}