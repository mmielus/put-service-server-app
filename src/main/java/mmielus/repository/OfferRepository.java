package mmielus.repository;

import java.util.List;
import java.util.Optional;

import mmielus.model.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Michal
 */
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long>
{

   Optional<Offer> findById(Long offerId);

   Page<Offer> findByCreatedBy(Long userId, Pageable pageable);

   long countByCreatedBy(Long userId);

   Page<Offer> findByIdIn(List<Long> offerIds, Pageable pageable);

   List<Offer> findByIdIn(List<Long> offerIds, Sort sort);

   Page<Offer> findByIsArchived(Long isArchived, Pageable pageable);

   @Query("SELECT o FROM Offer o WHERE o.id = :offerId AND o.isArchived= :isArchived")
   Optional<Offer> findOfferByIsArchived(@Param("isArchived") Long isArchived, @Param("offerId") Long offerId);

}
