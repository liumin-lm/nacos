/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.service.repository.embedded;

import com.alibaba.nacos.config.server.model.Page;
import com.alibaba.nacos.config.server.service.repository.PaginationHelper;
import com.alibaba.nacos.config.server.service.sql.EmbeddedStorageContextUtils;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;


/**
 * 分页辅助类 For Apache Derby
 *
 * @param <E>
 * @author boyan
 * @date 2010-5-6
 */
class EmbeddedPaginationHelperImpl<E> implements PaginationHelper {
    
    private final DatabaseOperate databaseOperate;
    
    public EmbeddedPaginationHelperImpl(DatabaseOperate databaseOperate) {
        this.databaseOperate = databaseOperate;
    }
    
    /**
     * 取分页
     *
     * @param sqlCountRows 查询总数的SQL
     * @param sqlFetchRows 查询数据的sql
     * @param args         查询参数
     * @param pageNo       页数
     * @param pageSize     每页大小
     * @param rowMapper
     * @return
     */
    public Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
            final int pageNo, final int pageSize, final RowMapper rowMapper) {
        return fetchPage(sqlCountRows, sqlFetchRows, args, pageNo, pageSize, null, rowMapper);
    }
    
    public Page<E> fetchPage(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
            final int pageNo, final int pageSize, final Long lastMaxId, final RowMapper rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        
        // 查询当前记录总数
        Integer rowCountInt = databaseOperate.queryOne(sqlCountRows, args, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }
        
        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }
        
        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);
        
        if (pageNo > pageCount) {
            return page;
        }
        
        final int startRow = (pageNo - 1) * pageSize;
        String selectSQL = sqlFetchRows + " OFFSET " + startRow + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        
        List<E> result = databaseOperate.queryMany(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }
    
    public Page<E> fetchPageLimit(final String sqlCountRows, final String sqlFetchRows, final Object[] args,
            final int pageNo, final int pageSize, final RowMapper rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 查询当前记录总数
        Integer rowCountInt = databaseOperate.queryOne(sqlCountRows, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }
        
        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }
        
        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);
        
        if (pageNo > pageCount) {
            return page;
        }
        
        String selectSQL = sqlFetchRows.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        List<E> result = databaseOperate.queryMany(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }
    
    public Page<E> fetchPageLimit(final String sqlCountRows, final Object[] args1, final String sqlFetchRows,
            final Object[] args2, final int pageNo, final int pageSize, final RowMapper rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 查询当前记录总数
        Integer rowCountInt = databaseOperate.queryOne(sqlCountRows, args1, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }
        
        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }
        
        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);
        
        if (pageNo > pageCount) {
            return page;
        }
        
        String selectSQL = sqlFetchRows.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        List<E> result = databaseOperate.queryMany(selectSQL, args2, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }
    
    public Page<E> fetchPageLimit(final String sqlFetchRows, final Object[] args, final int pageNo, final int pageSize,
            final RowMapper rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 创建Page对象
        final Page<E> page = new Page<E>();
        
        String selectSQL = sqlFetchRows.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        List<E> result = databaseOperate.queryMany(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }
    
    public void updateLimit(final String sql, final Object[] args) {
        String sqlUpdate = sql.replaceAll("limit \\?", "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
        
        EmbeddedStorageContextUtils.addSqlContext(sqlUpdate, args);
        try {
            databaseOperate.update(EmbeddedStorageContextUtils.getCurrentSqlContext());
        } finally {
            EmbeddedStorageContextUtils.cleanAllContext();
        }
    }
    
}
