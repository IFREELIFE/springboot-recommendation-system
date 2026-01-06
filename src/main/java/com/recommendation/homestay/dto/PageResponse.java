package com.recommendation.homestay.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import java.util.List;

/**
 * 通用分页响应DTO（替代IPage，避免类型转换异常）
 */
@Setter
public class PageResponse<T> {
    // 分页核心字段
    private List<T> records;    // 数据列表
    private long total;         // 总条数
    private long size;          // 每页条数
    private long current;       // 当前页（从0/1开始，和前端保持一致）
    private long pages;         // 总页数

    // Keep legacy field names (content/totalElements) for frontend compatibility
    /**
     * Legacy alias for records to keep frontend responses backward compatible.
     */
    @JsonProperty("content")
    public List<T> getContent() {
        return records;
    }

    @JsonIgnore
    public List<T> getRecords() {
        return records;
    }

    /**
     * Legacy alias for total to keep frontend responses backward compatible.
     */
    @JsonProperty("totalElements")
    public long getTotalElements() {
        return total;
    }

    @JsonIgnore
    public long getTotal() {
        return total;
    }

    public long getSize() {
        return size;
    }

    public long getCurrent() {
        return current;
    }

    public long getPages() {
        return pages;
    }

    // 从MyBatis-Plus的IPage转换为自定义DTO
    public static <T> PageResponse<T> fromIPage(com.baomidou.mybatisplus.core.metadata.IPage<T> iPage) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setRecords(iPage.getRecords());
        pageResponse.setTotal(iPage.getTotal());
        pageResponse.setSize(iPage.getSize());
        pageResponse.setCurrent(iPage.getCurrent());
        pageResponse.setPages(iPage.getPages());
        return pageResponse;
    }
}
