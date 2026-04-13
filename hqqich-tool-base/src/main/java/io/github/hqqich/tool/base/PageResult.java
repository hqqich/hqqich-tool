package io.github.hqqich.tool.base;

import java.util.Collections;
import java.util.List;
import lombok.Builder;

/**
 * Created by hqqich on 2024/4/5 is 15:13.<p/>
 *
 * @author hqqich
 */
@Builder
public class PageResult<T> {

    private int page; // 当前页
    private int size; // 每页数量
    private int totalPages; // 总页数
    private long totalElements; // 总元素数
    private List<T> content; // 当前页数据

    public PageResult() {
    }

    public PageResult(int page, int size, int totalPages, long totalElements, List<T> content) {
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageResult<T> empty(int page, int size) {
        return new PageResult<>(page, size, 0, 0, Collections.emptyList());
    }

}
