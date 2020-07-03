package com.fyq.entity;/**
 * @author com.fyq
 * @date 2020/7/3
 */

import lombok.Data;

import javax.persistence.Table;

/**
 * @description: 热搜列表
 * @author: com.fyq
 * @date: 2020-07-03 16:39
 **/
@Data
@Table(name = "hot_news")
public class HotList {
    private int id;

    private String sourceFrom;

    private String titleContent;

    private String targetUrl;

}
