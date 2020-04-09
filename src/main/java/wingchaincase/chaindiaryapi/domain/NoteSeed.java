package wingchaincase.chaindiaryapi.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import wingchaincase.chaindiaryapi.util.DateConverters;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_note_seed")
public class NoteSeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteSeedId;

    @Column(nullable = false)
    private String weixinUnionId;

    @Column(nullable = false)
    private String seed;

    @Convert(converter = DateConverters.class)
    @Column(nullable = false)
    @CreationTimestamp
    private Date ctime;

    @Convert(converter = DateConverters.class)
    @Column(nullable = false)
    @UpdateTimestamp
    private Date utime;

    public Long getNoteSeedId() {
        return noteSeedId;
    }

    public void setNoteSeedId(Long noteSeedId) {
        this.noteSeedId = noteSeedId;
    }

    public String getWeixinUnionId() {
        return weixinUnionId;
    }

    public void setWeixinUnionId(String weixinUnionId) {
        this.weixinUnionId = weixinUnionId;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
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

/*
drop table if exists `t_note_seed`;
CREATE TABLE `t_note_seed` (
  `note_seed_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `weixin_union_id` varchar(64) NOT NULL,
  `seed` varchar(256) NOT NULL,
  `ctime` int(20) NOT NULL,
  `utime` int(20) NOT NULL,
  PRIMARY KEY (`note_seed_id`),
  unique KEY (`weixin_union_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

 */
