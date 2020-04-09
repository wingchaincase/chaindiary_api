package wingchaincase.chaindiaryapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wingchaincase.chaindiaryapi.domain.NoteAttachment;

public interface NoteAttachmentRepository extends JpaRepository<NoteAttachment, Long> {

}
