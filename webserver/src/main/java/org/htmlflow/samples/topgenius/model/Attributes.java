package org.htmlflow.samples.topgenius.model;

public class Attributes {
    final int page;
    final int perPage;
    final int totalPages;
    final int total;

    public Attributes(int page, int perPage, int totalPages, int total) {
        this.page = page;
        this.perPage = perPage;
        this.totalPages = totalPages;
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotal() {
        return total;
    }
}
