package com.fyq.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
import tk.mybatis.mapper.entity.Config;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.io.IOException;

/**
 * @description:
 * @author: com.fyq
 * @date: 2020-07-03 17:01
 **/
public class MybatisHelper {
    private static SqlSessionFactory sqlSessionFactory;
    static {
        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder()
                    .build(Resources.getResourceAsReader("mybatis-config.xml"), "local");
            SqlSession sessionLocal = null;
            try {
                sessionLocal = sqlSessionFactory.openSession();
                MapperHelper mapperHelper = new MapperHelper();
                Config config = new Config();
                config.setEnableMethodAnnotation(true);
                mapperHelper.setConfig(config);
                mapperHelper.registerMapper(Mapper.class);
                mapperHelper.registerMapper(MySqlMapper.class);
                mapperHelper.processConfiguration(sessionLocal.getConfiguration());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (sessionLocal != null) {
                    sessionLocal.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SqlSession getSqlSessionLocal() {
        return sqlSessionFactory.openSession();
    }
}
