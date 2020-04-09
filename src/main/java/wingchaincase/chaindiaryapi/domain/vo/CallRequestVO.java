package wingchaincase.chaindiaryapi.domain.vo;

public class CallRequestVO {

    private String txHex;

    private String blobHex;

    public String getTxHex() {
        return txHex;
    }

    public void setTxHex(String txHex) {
        this.txHex = txHex;
    }

    public String getBlobHex() {
        return blobHex;
    }

    public void setBlobHex(String blobHex) {
        this.blobHex = blobHex;
    }
}
