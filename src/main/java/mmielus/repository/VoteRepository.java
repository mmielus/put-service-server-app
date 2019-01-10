package mmielus.repository;

import java.util.List;

import mmielus.model.ChoiceVoteCount;
import mmielus.model.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long>
{
   @Query(
         "SELECT NEW mmielus.model.ChoiceVoteCount(v.choice, count(v.id),v.offer.id) FROM Vote v WHERE v.offer.id in :offerIds GROUP BY v.offer.id,v.choice")
   List<ChoiceVoteCount> countByOfferIdInGroupByChoice(@Param("offerIds") List<Long> offerIds);

   @Query(
         "SELECT NEW mmielus.model.ChoiceVoteCount(v.choice, count(v.id),v.offer.id) FROM Vote v WHERE v.offer.id = :offerId GROUP BY v.offer.id,v.choice")
   List<ChoiceVoteCount> countByOfferIdGroupByChoice(@Param("offerId") Long offerId);

   @Query("SELECT v FROM Vote v where v.user.id = :userId and v.offer.id in :offerIds")
   List<Vote> findByUserIdAndOfferIdIn(@Param("userId") Long userId, @Param("offerIds") List<Long> offerIds);

   @Query("SELECT v FROM Vote v where v.user.id = :userId and v.offer.id = :offerId")
   Vote findByUserIdAndOfferId(@Param("userId") Long userId, @Param("offerId") Long offerId);

   @Query("SELECT COUNT(v.id) from Vote v where v.user.id = :userId")
   long countByUserId(@Param("userId") Long userId);

   @Query("SELECT v.offer.id FROM Vote v WHERE v.user.id = :userId")
   Page<Long> findVotedOfferIdsByUserId(@Param("userId") Long userId, Pageable pageable);
}

