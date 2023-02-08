package dk.easv.entities.api;

import java.util.List;

public class TMDB {

    private Integer page;
    private Integer total_results;
    private Integer total_pages;
    private List<Result> results = null;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTotal_results() {
        return total_results;
    }

    public void setTotal_results(Integer total_results) {
        this.total_results = total_results;
    }

    public Integer getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(Integer total_pages) {
        this.total_pages = total_pages;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "TMDB{"
                + "page=" + page
                + ", total_results=" + total_results
                + ", total_pages=" + total_pages
                + ", results=" + results + '}';
    }
}
