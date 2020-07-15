package com.fyq;

import com.fyq.task.GetStockTask;
import com.fyq.task.HotTask;
import com.fyq.utils.ExecutorUtil;
import com.google.common.collect.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @description: jsoup爬虫获取网页信息
 * @author: fyq
 * @date: 2020-07-03 09:16
 **/

public class GetHtml {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        int second = calendar.get(Calendar.SECOND);
        long millisecond = calendar.get(Calendar.MILLISECOND);
        long minuteDelay = 60 * 1000L - second * 1000L - millisecond;
        //一分钟拉取一次百度热搜榜的热搜并入库
        ExecutorUtil.scheduleWithFixedDelay(new HotTask(), minuteDelay, 60 * 1000, TimeUnit.MILLISECONDS);

        //模拟浏览器进行操作
        ExecutorUtil.scheduleWithFixedDelay(new GetStockTask(), minuteDelay, 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 根据url获取网页数据
     * @param url
     * @return
     */
    public Document getHtmlTextByUrl(String url) {
        Document document = null;
        try {
            //做一个随机延时，防止网站屏蔽
            Random random = new Random();
            int i = random.nextInt(1000);
            while (i != 0) {
                i--;
            }
            document = Jsoup.connect(url).get();
                    /*.data("query", "java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(30000).post();*/
            //System.out.println(document);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                document = Jsoup.connect(url).timeout(5000).get();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return document;
    }

    /**
     * 根据元素属性获取某个元素内的elements列表
     *
     * @param document
     * @param className
     * @return
     */
    public Elements getElementByClass(Document document, String className) {
        return document.select(className);
    }

    public List<String[]> getResult(String url, String type) {
        List<String[]> result = Lists.newArrayList();
        String classType = "." + type;
        //从网络上获取网页数据
        Document document = getHtmlTextByUrl(url);
        if (document != null) {
            Elements elements = getElementByClass(document, classType);
            //遍历元素列表
            for (Element element : elements) {
                if (element != null) {
                    for (Element elementChildren : element.children()) {
                        String[] prv = new String[4];
                        prv[0] = url;
                        //子节点下的第一个元素
                        //prv[1] = elementChildren.children().first().ownText();
                        //直接选择第几个子节点并获取到当前元素的文本 , 而不是子孩子的文本
                        prv[1] = elementChildren.child(1).ownText();
                        System.out.println(prv[1]);
                        //String ownUrl = elementChildren.children().first().attr("abs:href");
                        prv[2] = elementChildren.child(1).attr("abs:href");
                        //prv[2] = ownUrl;
                        System.out.println(prv[2]);
                        prv[3] = type;
                        result.add(prv);

                    }
                }
            }
        }
        return result;
    }
}
