package wingchaincase.chaindiaryapi.domain.vo;

import org.springframework.beans.BeanUtils;
import wingchaincase.chaindiaryapi.domain.Note;

import java.util.Date;

public class NoteVO {

    public NoteVO(Note note) {
        BeanUtils.copyProperties(note, this);
    }

    public NoteVO(Note note, String contentEnc) {
        BeanUtils.copyProperties(note, this);
        this.contentEnc = contentEnc;
    }

    private Long noteId;

    private String contentEncHash;

    private String contentEnc;

    private Date ctime;

    private Date utime;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getContentEncHash() {
        return contentEncHash;
    }

    public void setContentEncHash(String contentEncHash) {
        this.contentEncHash = contentEncHash;
    }

    public String getContentEnc() {
        return contentEnc;
    }

    public void setContentEnc(String contentEnc) {
        this.contentEnc = contentEnc;
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

