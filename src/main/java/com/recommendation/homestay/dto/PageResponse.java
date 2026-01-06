package com.recommendation.homestay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 通用分页响应DTO（替代IPage，避免类型转换异常）
 */
@Data
public class PageResponse<T> {
    // 分页核心字段
    private List<T> records;    // 数据列表
    private long total;         // 总条数
    private long size;          // 每页条数
    private long current;       // 当前页（从0/1开始，和前端保持一致）
    private long pages;         // 总页数

    // 兼容前端旧字段（content/totalElements）
    @JsonProperty("content")
    public List<T> getContent() {
        return records;
    }

    @JsonProperty("totalElements")
    public long getTotalElements() {
        return total;
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
