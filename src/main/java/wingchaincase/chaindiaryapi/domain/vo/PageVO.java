package wingchaincase.chaindiaryapi.domain.vo;

import java.util.List;
import java.util.Map;

public class PageVO<T> {

    private Long count;

    private List<T> list;

    private Map<String, Object> extra;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
