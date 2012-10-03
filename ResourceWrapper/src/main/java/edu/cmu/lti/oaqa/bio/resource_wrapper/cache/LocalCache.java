package edu.cmu.lti.oaqa.bio.resource_wrapper.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

public class LocalCache {
	String filePath;
	HashSet<String> files;
	
	public LocalCache(String path) {
		this.filePath = new File(path).getAbsolutePath();
		this.files = new HashSet<String>();
		
		
		// Scan directory at path and store filenames i.e. cached items
		File dir = new File(this.filePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		for (String s : dir.list()) {
			this.files.add(s);
		}
	}
	
	public void put(String id, InputStream inStream) {
			try {
				// Write file
				File out = new File(this.filePath + File.separator + id);
				BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(out));
				byte buf[] = new byte[10240];
				int len;
				while ( (len = inStream.read(buf)) > -1) {
					outStream.write(buf, 0, len);
				}
				outStream.flush();
				// Add to cache listing
				this.files.add(id);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("File not found:" + this.filePath + File.separator + id);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public InputStream get(String id) {
		if (this.files.contains(id)) {
			try {
				return new BufferedInputStream (new FileInputStream(new File(this.filePath + File.separator + id)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("File not found:" + this.filePath + File.separator + id);
			}
		}
		return null;
	}
	
	public boolean contains(String id) {
		return this.files.contains(id);
	}
	
	public void printContents() {
		Iterator<String> iter = this.files.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
	}
}
