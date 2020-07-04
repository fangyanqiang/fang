package com.fyq.task;

import com.fyq.GetHtml;
import com.fyq.entity.HotList;
import com.fyq.mapper.HotListMapper;
import com.fyq.utils.MybatisHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.util.Arrays;
import java.util.List;

/**
 * @author fyq
 * @date 2020/7/4
 */
@Slf4j
public class HotTask implements Runnable{
    @Override
    public void run() {
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
            List<HotList> select = mapper.select(hotList);
            System.out.println("获取的数据遍历结果: " + Arrays.toString(strings));
            if (select.size() > 0) {
                HotList data = select.get(0);
                if (data.getTitleContent().equals(strings[1])) {
                    continue;
                }
            }
            mapper.insert(hotList);
        }
        sqlSession.commit();
        sqlSession.close();
    }
}
