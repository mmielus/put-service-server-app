package mmielus.repository;

import java.util.List;
import java.util.Optional;

import mmielus.model.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Michal
 */
@Repository
public interface ObservedRepository extends JpaRepository<Observed, Long>
{
   @Query("SELECT o.offer.id FROM Observed o WHERE o.user.id = :userId")
   List<Long> findOfferIdsByObservedBy(@Param("userId") Long userId);

   @Query("SELECT o.id FROM Observed o WHERE o.user.id = :userId AND o.offer.id = :offerId")
   Optional<Observed> findObservedOfferIdByUserAndOffer(@Param("userId") Long userId, @Param("offerId") Long offerId);

}
