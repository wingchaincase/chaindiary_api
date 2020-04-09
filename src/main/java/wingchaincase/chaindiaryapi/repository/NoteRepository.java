package wingchaincase.chaindiaryapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import wingchaincase.chaindiaryapi.domain.Note;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByAddressAndIsDeleted(String address, Boolean deleted, Pageable pageable);

    Long countByAddressAndIsDeleted(String address, Boolean deleted);
}
