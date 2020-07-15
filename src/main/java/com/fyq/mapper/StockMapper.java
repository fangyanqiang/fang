package com.fyq.mapper;

import com.fyq.entity.Stock;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @author fyq
 * @date 2020/7/7
 */
public interface StockMapper extends InsertListMapper<Stock>, Mapper<Stock> {
}
