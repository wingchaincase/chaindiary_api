package wingchaincase.chaindiaryapi.domain.vo;

public class InitRequestVO {

    private String appid;
    private String code;
    private String encryptedData;
    private String iv;
    private String seedPublicKeyHex;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getSeedPublicKeyHex() {
        return seedPublicKeyHex;
    }

    public void setSeedPublicKeyHex(String seedPublicKeyHex) {
        this.seedPublicKeyHex = seedPublicKeyHex;
    }

}
