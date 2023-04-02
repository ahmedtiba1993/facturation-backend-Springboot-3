package com.facturation.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageImpl<T> implements Page<T> {

    private final List<T> content;
    private final Pageable pageable;
    private final long total;

    public PageImpl(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    @Override
    public int getTotalPages() {
        return (int) Math.ceil((double) total / (double) pageable.getPageSize());
    }

    @Override
    public long getTotalElements() {
        return total;
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        List<U> convertedContent = getContent().stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageImpl<>(convertedContent, pageable, total);
    }

    @Override
    public int getNumber() {
        return pageable.getPageNumber();
    }

    @Override
    public int getSize() {
        return pageable.getPageSize();
    }

    @Override
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public boolean isFirst() {
        return pageable.getPageNumber() == 0;
    }

    @Override
    public boolean isLast() {
        return pageable.getPageNumber() == getTotalPages() - 1;
    }

    @Override
    public boolean hasNext() {
        return !isLast();
    }

    @Override
    public boolean hasPrevious() {
        return !isFirst();
    }

    @Override
    public Sort getSort() {
        return pageable.getSort();
    }

    @Override
    public Pageable nextPageable() {
        return hasNext() ? pageable.next() : null;
    }

    @Override
    public Pageable previousPageable() {
        if (hasPrevious()) {
            return pageable.previousOrFirst();
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}

