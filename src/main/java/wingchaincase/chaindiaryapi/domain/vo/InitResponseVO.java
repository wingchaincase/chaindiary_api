package wingchaincase.chaindiaryapi.domain.vo;

public class InitResponseVO {

    private String seedEncHex;

    private Boolean isNew;

    public String getSeedEncHex() {
        return seedEncHex;
    }

    public void setSeedEncHex(String seedEncHex) {
        this.seedEncHex = seedEncHex;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }
}
