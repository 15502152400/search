package search;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Search {

	public static void main(String[] args){
		
		while (true) {
			/* 初始化键盘扫描 */ 
			Scanner scanner = new Scanner(System.in);
			/* 输入漫画地址 */ 
			System.out.println("输入漫画页面的地址");
			String htmlUrl = scanner.next();
			/* 输入漫画保存的地址"/Users/wangbingxiao/Downloads/"*/ 
			System.out.println("输入要保存的本地路径");	
			String savePath = scanner.next();
			/* 显示分割线 */
			System.out.println("---------------------------------------------------");
			System.out.println();
			/* 执行拉取 */ 
			doSavePic(htmlUrl, savePath);
		}
		
	}
	
	public static void doSavePic(String htmlUrl,String savePath) {
		/* 显示初始化信息 */
		System.out.println("================》正在初始化······");
		/* 获取开始时间 */
		Long start = System.currentTimeMillis();	
		/* 初始化文档容器 */
		Document document;
		/* 创建列表储存图片src */
		ArrayList<String> srcList = new ArrayList<String>();		
		/* 初始化漫画名字 */
		String name = new String();
		/* 初始化输入流 */
		BufferedInputStream in = null;
		/* 初始化输出流 */
		BufferedOutputStream out = null;
		/* 初始化连接 */
		HttpURLConnection httpURLConnection = null;
		/* 显示初始化完成信息 */
		Long end1 = System.currentTimeMillis();
		/* 显示初始化完成信息 */
		System.out.println("================》初始化完成，耗时："+(end1-start)+"毫秒");
		System.out.println("---------------------------------------------------");
		System.out.println();
		
		try {
			/* 显示开始解析 */
			System.out.println("================》开始解析网页");
			Long start2 = System.currentTimeMillis();
			/* 解析url */
			URL url = new URL(htmlUrl);
			/* 根据url获取文档 */
			document = Jsoup.parse(url,1000*30);
			/* 获取图片容器 */
			Elements imgs = document.select(".vimg:not(.lazyload)");
			/* 获取漫画名字 */
			Elements h1 = document.getElementsByTag("h1");
			Iterator<Element> h1iterator = h1.iterator();
			while (h1iterator.hasNext()) {
				name = h1iterator.next().html();				
			}
			/* 获取漫画图片的src的集合 */
			Iterator<Element> iterator = imgs.iterator();
			/* 遍历图片src集合 */
			while (iterator.hasNext()) {
				/* 截取真实src */
				String src = iterator.next().attr("src");
				/* 存入src列表 */
				srcList.add(src);
			}
			/* 创建保存地址 */
			File imgDir = new File(savePath+name+"/");
			if (!imgDir.exists()) {
				imgDir.mkdirs();
			}
			Long end2 = System.currentTimeMillis();
			/* 显示解析完成信息 */
			System.out.println("================》解析网页完成：耗时"+(end2-start2)+"毫秒");
			System.out.println("---------------------------------------------------");
			System.out.println();
			
			/* 显示开始下载 */
			System.out.println("================》开始下载图片");
			Long start3 = System.currentTimeMillis();
			/* 遍历src列表分别把图片写入到本地地址 */
			for (int i = 0;i<srcList.size();i++) {
				/* 获取地址 */
				String src = srcList.get(i);
				int index = src.lastIndexOf("/");
				String imageName = src.substring(index);
				/* 根据地址设置连接 */
				URLConnection con = new URL(src).openConnection();
				httpURLConnection = (HttpURLConnection) con;
				httpURLConnection.setConnectTimeout(1000*30);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty("Charset", "UTF-8");
				/* 打开连接 */
				httpURLConnection.connect();
				/* 获取图片输入流 */
				try {
					in = new BufferedInputStream(httpURLConnection.getInputStream());
				} catch (FileNotFoundException f) {
					System.out.println("================》连接出错，尝试自动修改（网站做的太不规范了）");
					String imageSuffix = imageName.substring(imageName.lastIndexOf(".")).toLowerCase();
					System.out.println("log===="+imageSuffix);
					if (".png".equals(imageSuffix)) {
						src = src.replaceAll(".png", ".jpg");
					}else if (".jpg".equals(imageSuffix)) {
						src = src.replaceAll(".jpg", ".png");
					}
					con = new URL(src).openConnection();
					httpURLConnection = (HttpURLConnection) con;
					/* 重新获取输入流 */
					in = new BufferedInputStream(httpURLConnection.getInputStream());
				}				
				/* 建立输出流 */
				out = new BufferedOutputStream(new FileOutputStream(imgDir+imageName));
				/* 把图片输入流转为byte[] */
				byte[] b = new byte[2048*3];
				b = in.readAllBytes();
				/* 把byte[]写出为图片 */
				out.write(b);
				/* 显示结果 */
				System.out.println("===》保存完成："+src);				
			}
			/* 显示下载完成信息 */
			Long end3 = System.currentTimeMillis();
			System.out.println("================》下载图片完成：耗时"+(end3-start3)+"毫秒");
			System.out.println("---------------------------------------------------");
			System.out.println();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("拉取失败，请尝试其他漫画或者联系作者更新，邮箱：708970258@qq.com");
		}finally {
			/* 关闭输入输出流 */
			try {
				if (in!=null&&out!=null) {
					in.close();
					out.close();
				}			
				httpURLConnection.disconnect();
				/* 获取结束时间 */
				Long end = System.currentTimeMillis();
				/* 显示任务运行时间 */
				System.out.println("================》任务完成，总计耗时："+(end-start)/1000+"秒");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("拉取失败，请尝试其他漫画或者联系作者更新，邮箱：708970258@qq.com");
			}finally {
				/* 显示空行和分割线 */
				System.out.println("==================================！ 本次任务结束 !==================================");
				System.out.println();
				System.out.println();
				System.out.println();
			}
		}
	}
	
}