package wingchaincase.chaindiaryapi.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import wingchaincase.chaindiaryapi.util.DateConverters;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contentEncHash;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Convert(converter = DateConverters.class)
    @Column(nullable = false)
    @CreationTimestamp
    private Date ctime;

    @Convert(converter = DateConverters.class)
    @Column(nullable = false)
    @UpdateTimestamp
    private Date utime;

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContentEncHash() {
        return contentEncHash;
    }

    public void setContentEncHash(String contentEncHash) {
        this.contentEncHash = contentEncHash;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}

/*
drop table if exists `t_note`;
CREATE TABLE `t_note` (
  `note_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(64) NOT NULL,
  `content_enc_hash` varchar(256) NOT NULL,
  `is_deleted` tinyint NOT NULL,
  `ctime` int(20) NOT NULL,
  `utime` int(20) NOT NULL,
  PRIMARY KEY (`note_id`),
  KEY (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

 */