package com.study.crawlbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.study.crawlbook.CrawlBook.ExitListener;


public class NovelRunnable implements Runnable {
	
	private NovelItem item;
	private String dirname;
	private Map<String, String> header = new HashMap<String, String>();
	private int index = 0;
	private File file;
	private ExitListener exitListener;
	private AtomicInteger count;
	
	public NovelRunnable(NovelItem item,String dirname,Map<String, String> header, int index,ExitListener exitListener, AtomicInteger count) {
		super();
		this.item = item;
		this.dirname = dirname;
		this.header = header;
		this.index = index;
		this.exitListener = exitListener;
		this.count = count;
	}
	
	private boolean createTxtFile(String name) throws IOException {
		boolean flag = false;
		file = new File(name);
		if(!file.exists()) {
			file.createNewFile();
			flag = true;
		}
		
		return flag;
	}

	public void run() {
		  
	    FileOutputStream fos = null;  
	    PrintWriter pw = null;
		String fileName =  dirname + index + "_"+ item.getTitle() + ".txt";
		boolean flag;
		try {
			flag = createTxtFile(fileName);
			if(flag) {
				String url = item.getUrl();
				Document doc = Jsoup.connect(url).headers(header).timeout(5000).get();
				StringBuffer buf = new StringBuffer();
				buf.append(doc.select("div.content_read").select("div.box_con").select("div#content").text().replace("<br>", "\n"));
				fos = new FileOutputStream(file);  
	            pw = new PrintWriter(fos);  
	            pw.write(buf.toString().toCharArray());  
	            pw.flush();  
			
			}
			count.decrementAndGet();
			if(count.intValue() == 0) {
				exitListener.exitSystem();
			}
		
		} catch (Exception e) {
			count.decrementAndGet();
			if(count.intValue() == 0) {
				exitListener.exitSystem();
			}
			return;
		} finally {
			if(pw != null) {
				pw.close();
			}
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
