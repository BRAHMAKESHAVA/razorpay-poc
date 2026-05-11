package org.backend.dto.common;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    private List<T> content;
}