package com.example.hrmsclient.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponseDTO<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private PageResponseDTO(List<T> content, int pageNumber, int pageSize,
                             long totalElements, int totalPages,
                             boolean last, boolean first) {
        this.content      = content;
        this.pageNumber   = pageNumber;
        this.pageSize     = pageSize;
        this.totalElements = totalElements;
        this.totalPages   = totalPages;
        this.last         = last;
        this.first        = first;
    }
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.isFirst()
        );
    }

    public List<T> getContent()        { return content;       }
    public int getPageNumber()         { return pageNumber;    }
    public int getPageSize()           { return pageSize;      }
    public long getTotalElements()     { return totalElements; }
    public int getTotalPages()         { return totalPages;    }
    public boolean isLast()            { return last;          }
    public boolean isFirst()           { return first;         }
}