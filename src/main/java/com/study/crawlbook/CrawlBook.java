package com.study.crawlbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlBook {
	private static Map<String, String> header = new HashMap<String, String>();
	static{
		header.put("Accept-Language", "zh-CN,zh;q=0.9");
		header.put("Connection", "keep-alive");
		header.put("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3355.4 Safari/537.36");
		header.put("Accept", "*/*");
		header.put("Accept-Encoding", "gzip, deflate, br");
	}
	private static String keyword;
	private static String url_prefix = "https://www.xxbiquge.com";
	private static String url = null;
	private static String path_prefix = "D:/笔趣阁crawl/";
	private static String dirname = null;
	private static String targetTitle = null;
	
	private static AtomicInteger count = new AtomicInteger(0);
	
	
	private static ExecutorService fixThreadPool = Executors.newFixedThreadPool(5);
	
	
	private static ExitListener exitListener = new ExitListener() {
		public void exitSystem() {
			System.out.println("下载完成，退出系统!!!!");
			fixThreadPool.shutdown();
			System.exit(0);
		}
	};
	
	private static RestartListener restartListener = new RestartListener() {
		public void restartAgain() {
			startSearch();
		}
	};
	
	private static ShowDownloadItemsListener showDownloadItemsListener = new ShowDownloadItemsListener() {
		
		public void showDownloadItems(List<DownloadNovelItem> datas) {
			System.out.println("===============================================================");
			System.out.println("===============================================================");
			System.out.println("===============================================================");
			System.out.println("序号    ---    书名");
			for(DownloadNovelItem item : datas) {
				System.out.println(item.getNumber() +"    ---    " + item.getTitle());
			}
			System.out.println("===============================================================");
			System.out.println("===============================================================");
			System.out.println("===============================================================");
			System.out.println("请输入序号：  ");
			Scanner scanner = new Scanner(System.in);
			String numIndexStr = scanner.nextLine();
			try {
				int numIndex = Integer.parseInt(numIndexStr);
				DownloadNovelItem targetItem  = datas.get(numIndex - 1);
				System.out.println("要下载的是:  " + targetItem.getTitle());
				targetTitle = targetItem.getTitle();
				targetUrl = targetItem.getUrl();
				dirname = path_prefix + targetTitle;
				File file = new File(dirname);
				if(!file.exists()) {
					file.mkdirs();
				}
				dirname = dirname + "/";
				searchAllArticleItems();
			} catch(Exception e) {
				
			}
			
			
		}
	};
	
	private static CallBack callBack = new CallBack() {
		
		public void showData(List<NovelItem> datas) {
			int index = 1;
			System.out.println("开始下载各个章节.....");
			for(NovelItem novelItem : datas) {
//				System.out.println(novelItem);
				count.incrementAndGet();
				fixThreadPool.execute(new NovelRunnable(novelItem, dirname, header, index,exitListener,count));
				index++;
			}
		}
	};
	
	
	public static void main(String[] args) {
		System.out.print("请输入书名:  ");
		Scanner scanner = new Scanner(System.in);
		keyword = scanner.nextLine();
		startSearchBook(keyword);
	}
	
	private static String targetUrl = null;
	
	public static void startSearchBook(String keyword) {
		url = url_prefix + "/search.php?keyword=" + keyword;
		new Thread(new Runnable() {
			
			public void run() {
				startSearch();
			}
		}).start();
		
	}
	
	
	
	
	public static void startSearch() {
		System.out.println("开始搜索....");
		try {
			Document doc = Jsoup.connect(url).headers(header).timeout(10000).get();
			System.out.println(url);
			/*Element targetEl = doc.select("div.result-game-item-detail").get(0);
			targetUrl = targetEl.select("a.result-game-item-title-link").attr("href");
			targetTitle = targetEl.select("a.result-game-item-title-link").select("span").text();
			System.out.println(targetUrl);
			System.out.println(targetTitle);
			dirname = path_prefix + targetTitle;
			File file = new File(dirname);
			if(!file.exists()) {
				file.mkdirs();
			}
			dirname = dirname + "/";
			searchAllArticleItems();
			*/
			Elements pages = doc.select("div.search-result-page-main").select("a");
			Elements items = doc.select("div.result-game-item-detail");
			
			int pagesIndex = pages.size();
			int itemsNumber = items.size();
			
			List<DownloadNovelItem> datas = new ArrayList<DownloadNovelItem>();
			
			if(pagesIndex > 0) {//有分页情况
				System.out.println("有分页情况");
				Element lastEl = pages.last();
				String aStr = lastEl.attr("href");
				String pageStr = aStr.substring(aStr.lastIndexOf("=") + 1);
				System.out.println("总共有" + pageStr + "页"); 
				try {
					int totalPage = Integer.parseInt(pageStr);
					for(int index = 1; index <= totalPage ; index++) {
						String pageUrl = url + "&page=" + index;
						System.out.println("开始搜索第 " + index + " 页");
						startPageSearch(pageUrl,datas);
					}
					showDownloadItemsListener.showDownloadItems(datas);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else if (pagesIndex == 0 && itemsNumber > 0){//没分页情况
				System.out.println("没分页情况，总数为:   " + itemsNumber);
				String pageUrl = url + "&page=" + 1;
				startPageSearch(pageUrl,datas);
				showDownloadItemsListener.showDownloadItems(datas);
			} else {
				System.out.println("没有搜到您想要的书籍");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			restartListener.restartAgain();
		}
	}
	
	
	public static void startPageSearch(String pageUrl,List<DownloadNovelItem> datas) {
		try {
			Document doc = Jsoup.connect(pageUrl).headers(header).timeout(10000).get();
			Elements targetEls = doc.select("div.result-game-item-detail");
			for(Element targetEl : targetEls) {
				DownloadNovelItem item  = new DownloadNovelItem();
				targetUrl = targetEl.select("a.result-game-item-title-link").attr("href");
				targetTitle = targetEl.select("a.result-game-item-title-link").select("span").text();
//				System.out.println(targetUrl + "    ============================     " + targetTitle);
				int itemIndex = datas.size() + 1;
				item.setNumber(itemIndex + "");;
				item.setTitle(targetTitle);
				item.setUrl(targetUrl);
				
//				System.out.println(item);
				datas.add(item);
			}
			
			
			
		} catch (Exception e) {
			startPageSearch(pageUrl,datas);
		}
		
	}
	
	public static void searchAllArticleItems() {
		if(targetUrl != null && !targetUrl.equals("")) {
			try {
				Document doc = Jsoup.connect(targetUrl).headers(header).timeout(10000).get();
				Elements els = doc.select("div.box_con").get(1).select("div.box_con").select("dd");
				List<NovelItem> datas = new ArrayList<NovelItem>();
				for(Element el : els) {
					String title = el.select("a[href]").text();
					String url = url_prefix + el.select("a[href]").attr("href");
					NovelItem item = new NovelItem();
					item.setTitle(title);
					item.setUrl(url);
					datas.add(item);
				}
				callBack.showData(datas);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	interface CallBack{
		void showData(List<NovelItem> datas);
	}
	interface RestartListener{
		void restartAgain();
	}
	
	interface RestartPageListener{
		void restartPageListener();
	}
	
	interface ExitListener{
		void exitSystem();
	}
	
	interface ShowDownloadItemsListener{
		void showDownloadItems(List<DownloadNovelItem> datas);
	}
	
}
