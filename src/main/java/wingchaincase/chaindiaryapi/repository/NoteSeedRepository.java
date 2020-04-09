package wingchaincase.chaindiaryapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wingchaincase.chaindiaryapi.domain.NoteSeed;

public interface NoteSeedRepository extends JpaRepository<NoteSeed, Long> {

    NoteSeed findByWeixinUnionId(String weixinUnionId);

}
