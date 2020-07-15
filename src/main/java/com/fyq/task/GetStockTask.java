package com.fyq.task;

import com.fyq.entity.Stock;
import com.fyq.mapper.StockMapper;
import com.fyq.utils.MybatisHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.session.SqlSession;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author fyq
 * @date 2020/7/7
 */
public class GetStockTask implements Runnable{
    private final static String URL = "http://q.10jqka.com.cn/gn/";

    private final static int TIMES = 2;

    @Override
    public void run() {
        System.out.println(System.currentTimeMillis());
        SqlSession sqlSession = MybatisHelper.getSqlSessionLocal();
        StockMapper mapper = sqlSession.getMapper(StockMapper.class);
        ChromeOptions options = configChromeOptions();
        options.addArguments("headless");
        options.addArguments("no_sandbox");
        WebDriver driver = new ChromeDriver(options);
        System.out.println("ready!");
        try {
            driver.get(URL);
            String windowHandle = driver.getWindowHandle();
            //获取源页面html
            String pageSource = driver.getPageSource();
            List<Map<String, String>> list = parsePageSource(pageSource);
            /*int count = 0;
            for (int i = 0; i < TIMES; i++) {
                int listLength = list.size();
                int quarterNum = listLength / TIMES;
                final List<Map<String, String>> subList;
                if (i < TIMES - 1) {
                    subList = list.subList(count, count + quarterNum);
                } else {
                    subList = list.subList(count, list.size());
                }
                count += quarterNum;
                //设想是想将分成多个线程去解析html，各自将解析出来的文件各自入库
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        SqlSession sqlSession = MybatisHelper.getSqlSessionLocal();
                        StockMapper mapper = sqlSession.getMapper(StockMapper.class);
                        List<Stock> stockList = parseInnerData(subList);
                        //这边准备批量插入数据
                        mapper.insertList(stockList);
                        sqlSession.commit();
                        sqlSession.close();
                    }
                }).start();
            }*/
            List<Stock> stockList = parseInnerData(list);
            //这边准备批量插入数据
            mapper.insertList(stockList);
            sqlSession.commit();
            sqlSession.close();
            System.out.println(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.close();
            driver.quit();
        }
    }

    public ChromeOptions configChromeOptions() {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("headless");
        //options.addArguments("no-sandbox");
        return options;
    }

    /**
     * 解析最外面路径下的html数据
     * @param pageSourceHtml html
     * @return List<Map<String, String>>
     */
    public List<Map<String, String>> parsePageSource(String pageSourceHtml) {
        if (StringUtil.isBlank(pageSourceHtml)) {
            return null;
        }
        List<Map<String, String>> list = Lists.newArrayList();
        Document document = Jsoup.parse(pageSourceHtml);
        Elements elements = document.getElementsByClass("cate_items");
        for (Element element : elements) {
            Elements elementsByTags = element.getElementsByTag("a");
            for (Element tag : elementsByTags) {
                String href = tag.attr("href");
                String name = tag.ownText();
                Map<String, String> urlAndName = Maps.newHashMap();
                urlAndName.put(name, href);
                list.add(urlAndName);
            }
        }
        return list;
    }

    /**
     * 解析每个概念下的概念股列表数据
     * @param list List<Map<String, String>>
     * @return List<Stock>
     */
    public List<Stock> parseInnerData(List<Map<String, String>> list) {
        ChromeOptions options = configChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        List<Stock> allStocksData = Lists.newArrayList();
        int length = 0;
        for (Map<String, String> stringStringMap : list) {
            length ++;
            System.out.println("这是线程：" + Thread.currentThread().getName() + " 第" + length + "次遍历最外层的概念股");
            //遍历所有的href和名字
            for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
                //通过href来获取html
                driver.get(entry.getValue());
                String innerPageSource = driver.getPageSource();
                List<Stock> stockList = parseAndInsertIntoData(innerPageSource);
                //递归下一页拿数据
                List<Stock> allStocks = clickNextPage(driver, innerPageSource, stockList);
                allStocksData.addAll(allStocks);
            }
        }

        return allStocksData;
    }

    /**
     * 解析数据
     * @param html html
     * @return List<Stock>
     */
    public List<Stock> parseAndInsertIntoData(String html) {
        Document parseDocument = Jsoup.parse(html);
        Element table = parseDocument.getElementsByClass("m-pager-table").get(0);
        Element tBody = table.getElementsByTag("tbody").get(0);
        Elements tr = tBody.getElementsByTag("tr");
        List<Stock> stockList = Lists.newLinkedList();
        for (Element element : tr) {
            Elements tds = element.getElementsByTag("td");
            if (!tds.isEmpty() && !"暂无成份股数据".equals(tds.get(0).ownText())) {
                //代码
                String stockCode = tds.get(1).text();
                //名称
                //TODO 此处有详情的连接，这里暂时就不解析出来存了，有需要了再来解析出来
                String name = tds.get(2).text();
                //现价
                BigDecimal price = parseDecimal(tds.get(3).ownText());
                //涨跌幅(%)
                BigDecimal stockChange = parseDecimal(tds.get(4).ownText());
                //涨跌
                BigDecimal stockChangePrice = parseDecimal(tds.get(5).ownText());
                //涨速(%)
                BigDecimal stockChangeSpeed = parseDecimal(tds.get(6).ownText());
                //换手(%)
                BigDecimal stockHandoverScale = parseDecimal(tds.get(7).ownText());
                //量比
                BigDecimal stockLiangBi = parseDecimal(tds.get(8).ownText());
                //振幅
                BigDecimal stockAmplitude = parseDecimal(tds.get(9).ownText());
                //成交额
                BigDecimal stockDealAmount = parseDecimal(tds.get(10).ownText());
                //流通股
                BigDecimal stockFlowStockNumber = parseDecimal(tds.get(11).ownText());
                //流通市值
                BigDecimal stockFlowMarketValue = parseDecimal(tds.get(12).ownText());
                //市盈率
                BigDecimal stockMarketTtm = parseDecimal(tds.get(13).ownText());

                Stock stock = new Stock();
                stock.setStockCode(stockCode);
                stock.setStockName(name);
                stock.setStockPrice(price);
                stock.setStockChange(stockChange);
                stock.setStockChangePrice(stockChangePrice);
                stock.setStockChangeSpeed(stockChangeSpeed);
                stock.setStockHandoverScale(stockHandoverScale);
                stock.setStockLiangBi(stockLiangBi);
                stock.setStockAmplitude(stockAmplitude);
                stock.setStockDealAmount(stockDealAmount);
                stock.setStockFlowStockNumber(stockFlowStockNumber);
                stock.setStockFlowMarketValue(stockFlowMarketValue);
                stock.setStockMarketTtm(stockMarketTtm);
                stockList.add(stock);
            }
        }
        return stockList;
    }

    /**
     * 点击下一页查询数据
     * @param webDriver webDriver
     * @param html html
     * @param list list
     * @return List<Stock>
     */
    public List<Stock> clickNextPage(WebDriver webDriver, String html, List<Stock> list) {
        final String pageNumber = includeNextPage(html);
        if (StringUtils.isEmpty(pageNumber)) {
            return list;
        }
        WebElement nextPageElement = webDriver.findElement(By.linkText("下一页"));
        //点击下一页进行查找数据
        nextPageElement.click();
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //上面sleep的替代方法
        //在点击之后做一个按照要求条件的等待来使之确保下次点击改元素不会失效
        WebDriverWait wait = new WebDriverWait(webDriver, 60);
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                String pageSource = webDriver.getPageSource();
                String nextPage = includeNextPage(pageSource);
                return !pageNumber.equals(nextPage);
            }
        });
        String nextPageSource = webDriver.getPageSource();
        List<Stock> stocks = parseAndInsertIntoData(nextPageSource);
        list.addAll(stocks);
        return clickNextPage(webDriver, nextPageSource, list);
    }

    /**
     * 是否包含这下一页
     * @param html html
     * @return String
     */
    public String includeNextPage(String html) {
        Document parse = Jsoup.parse(html);
        Elements a = parse.getElementsByClass("changePage");
        for (Element element : a) {
            String text = element.ownText();
            if ("下一页".equals(text)) {
                return element.attr("page");
            }
        }
        return "";
    }

    /**
     * 数字的转化
     * @param numStr numStr
     * @return BigDecimal
     */
    public BigDecimal parseDecimal(String numStr) {
        if (StringUtils.isEmpty(numStr)) {
            return BigDecimal.ZERO;
        } else if ("--".equals(numStr)) {
            return BigDecimal.ZERO;
        } else if (numStr.endsWith("亿")) {
            return new BigDecimal(numStr.substring(0, numStr.length() - 1)).multiply(BigDecimal.ONE);
        }
        return new BigDecimal(numStr);
    }

}
