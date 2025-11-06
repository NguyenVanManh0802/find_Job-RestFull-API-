package vn.manh.findJob.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PageResponse<T> {

    int pageNo;
    int pageSize;
    int totalPage;
    T items;
}
