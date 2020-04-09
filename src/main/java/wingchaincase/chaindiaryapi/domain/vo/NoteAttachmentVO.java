package wingchaincase.chaindiaryapi.domain.vo;

import org.springframework.beans.BeanUtils;
import wingchaincase.chaindiaryapi.domain.NoteAttachment;

import java.util.Date;

public class NoteAttachmentVO {

    public NoteAttachmentVO(NoteAttachment attachment) {
        BeanUtils.copyProperties(attachment, this);
    }

    public NoteAttachmentVO(NoteAttachment attachment, String attachmentEnc) {
        BeanUtils.copyProperties(attachment, this);
        this.attachmentEnc = attachmentEnc;
    }

    private Long attachmentId;

    private String attachmentEncHash;

    private String attachmentEnc;

    private Date ctime;

    private Date utime;

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentEncHash() {
        return attachmentEncHash;
    }

    public void setAttachmentEncHash(String attachmentEncHash) {
        this.attachmentEncHash = attachmentEncHash;
    }

    public String getAttachmentEnc() {
        return attachmentEnc;
    }

    public void setAttachmentEnc(String attachmentEnc) {
        this.attachmentEnc = attachmentEnc;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getUtime() {
        return utime;
    }

    public void setUtime(Date utime) {
        this.utime = utime;
    }
}

