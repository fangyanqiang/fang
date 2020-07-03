package com.fyq;

import com.fyq.entity.HotList;
import com.fyq.mapper.HotListMapper;
import com.fyq.utils.MybatisHelper;
import com.google.common.collect.Lists;
import org.apache.ibatis.session.SqlSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @description: jsoup爬虫获取网页信息
 * @author: com.fyq
 * @date: 2020-07-03 09:16
 **/

public class GetHtml {
    public static void main(String[] args) {
        SqlSession sqlSession = MybatisHelper.getSqlSessionLocal();
        HotListMapper mapper = sqlSession.getMapper(HotListMapper.class);
        //String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2015/index.html";
        //String type = "provincetr";
        String url = "http://top.baidu.com/";
        String type = "list";
        GetHtml html = new GetHtml();
        List<String[]> result = html.getResult(url, type);
        for (String[] strings : result) {
            HotList hotList = new HotList();
            hotList.setSourceFrom(strings[0]);
            hotList.setTitleContent(strings[1]);
            hotList.setTargetUrl(strings[2]);
            mapper.insert(hotList);
            System.out.println("遍历: " + Arrays.toString(strings));
        }
        sqlSession.commit();
        sqlSession.close();
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
