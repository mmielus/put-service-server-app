package mmielus.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.ToString;
import mmielus.exception.BadRequestException;
import mmielus.exception.ResourceNotFoundException;
import mmielus.model.ChoiceVoteCount;
import mmielus.model.Dimension;
import mmielus.model.Observed;
import mmielus.model.Offer;
import mmielus.model.User;
import mmielus.model.Vote;
import mmielus.payload.OfferRequest;
import mmielus.payload.OfferResponse;
import mmielus.payload.PagedResponse;
import mmielus.payload.VoteRequest;
import mmielus.repository.ObservedRepository;
import mmielus.repository.OfferRepository;
import mmielus.repository.UserRepository;
import mmielus.repository.VoteRepository;
import mmielus.security.UserPrincipal;
import mmielus.util.AppConstants;
import mmielus.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author Michal
 */
@Service
@ToString
public class OfferService
{

   @Autowired
   private OfferRepository offerRepository;

   @Autowired
   private VoteRepository voteRepository;

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private ObservedRepository observedRepository;

   private static final Logger logger = LoggerFactory.getLogger(OfferService.class);

   public PagedResponse<OfferResponse> getAllOffers(UserPrincipal currentUser, int page, int size)
   {
      validatePageNumberAndSize(page, size);

      Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "finalVotesCount", "createdAt");
      Page<Offer> offers = offerRepository.findByIsArchived(0L, pageable);

      if (offers.getNumberOfElements() == 0)
      {
         return new PagedResponse<>(Collections.emptyList(), offers.getNumber(),
               offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
      }

      List<Long> offerIds = offers.map(Offer::getId).getContent();
      List<ChoiceVoteCount> choiceVoteList = getChoiceVoteCountList(offerIds);
      Map<Long, Long> pollUserVoteMap = getOfferUserVoteMap(currentUser, offerIds);
      Map<Long, User> creatorMap = getPollCreatorMap(offers.getContent());
      //   List<ChoiceVoteCount> votes = voteRepository.countByOfferIdInGroupByChoice(offerIds);
      List<OfferResponse> offerResponses = offers.map(offer -> {
         return ModelMapper.mapPollToPollResponse(offer,
               choiceVoteList,
               creatorMap.get(offer.getCreatedBy()),
               pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(offer.getId(), null));
      }).getContent();

      return new PagedResponse<>(offerResponses, offers.getNumber(),
            offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
   }

   public PagedResponse<OfferResponse> getOffersCreatedBy(String username, UserPrincipal currentUser, int page,
         int size)
   {
      validatePageNumberAndSize(page, size);

      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

      // Retrieve all offers created by the given username
      Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "finalVotesCount", "createdAt");
      Page<Offer> offers = offerRepository.findByCreatedBy(user.getId(), pageable);

      if (offers.getNumberOfElements() == 0)
      {
         return new PagedResponse<>(Collections.emptyList(), offers.getNumber(),
               offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
      }

      // Map Polls to PollResponses containing vote counts and poll creator details
      List<Long> pollIds = offers.map(Offer::getId).getContent();
      List<ChoiceVoteCount> choiceVoteCountMap = getChoiceVoteCountList(pollIds);
      Map<Long, Long> offerUserVoteMap = getOfferUserVoteMap(currentUser, pollIds);

      List<OfferResponse> offerRespons = offers.map(poll -> {
         return ModelMapper.mapPollToPollResponse(poll,
               choiceVoteCountMap,
               user,
               offerUserVoteMap == null ? null : offerUserVoteMap.getOrDefault(poll.getId(), null));
      }).getContent();

      return new PagedResponse<>(offerRespons, offers.getNumber(),
            offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
   }

   public PagedResponse<OfferResponse> getArchivedOffers(UserPrincipal currentUser, int page,
         int size)
   {
      validatePageNumberAndSize(page, size);

      Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "finalVotesCount", "createdAt");
      Page<Offer> offers = offerRepository.findByIsArchived(1L, pageable);

      if (offers.getNumberOfElements() == 0)
      {
         return new PagedResponse<>(Collections.emptyList(), offers.getNumber(),
               offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
      }

      List<Long> offerIds = offers.map(Offer::getId).getContent();
      List<ChoiceVoteCount> choiceVoteList = getChoiceVoteCountList(offerIds);
      Map<Long, Long> pollUserVoteMap = getOfferUserVoteMap(currentUser, offerIds);
      Map<Long, User> creatorMap = getPollCreatorMap(offers.getContent());
      List<ChoiceVoteCount> votes = voteRepository.countByOfferIdInGroupByChoice(offerIds);
      List<OfferResponse> offerResponses = offers.map(offer -> {
         return ModelMapper.mapPollToPollResponse(offer,
               choiceVoteList,
               creatorMap.get(offer.getCreatedBy()),
               pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(offer.getId(), null));
      }).getContent();

      return new PagedResponse<>(offerResponses, offers.getNumber(),
            offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
   }

   public PagedResponse<OfferResponse> getObservedOffers(UserPrincipal currentUser, int page, int size)
   {
      validatePageNumberAndSize(page, size);

      List<Long> offersIds = observedRepository.findOfferIdsByObservedBy(currentUser.getId());
      Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "finalVotesCount", "createdAt");

      Page<Offer> offers = offerRepository.findByIdIn(offersIds, pageable);

      if (offers.getNumberOfElements() == 0)
      {
         return new PagedResponse<>(Collections.emptyList(), offers.getNumber(),
               offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
      }

      List<Long> offerIds = offers.map(Offer::getId).getContent();
      List<ChoiceVoteCount> choiceVoteList = getChoiceVoteCountList(offerIds);
      Map<Long, Long> pollUserVoteMap = getOfferUserVoteMap(currentUser, offerIds);
      Map<Long, User> creatorMap = getPollCreatorMap(offers.getContent());
      //  List<ChoiceVoteCount> votes = voteRepository.countByOfferIdInGroupByChoice(offerIds);
      List<OfferResponse> offerResponses = offers.map(offer -> {
         return ModelMapper.mapPollToPollResponse(offer,
               choiceVoteList,
               creatorMap.get(offer.getCreatedBy()),
               pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(offer.getId(), null));
      }).getContent();

      return new PagedResponse<>(offerResponses, offers.getNumber(),
            offers.getSize(), offers.getTotalElements(), offers.getTotalPages(), offers.isLast());
   }

   public PagedResponse<OfferResponse> getOffersVotedBy(String username, UserPrincipal currentUser, int page, int size)
   {
      validatePageNumberAndSize(page, size);

      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

      // Retrieve all pollIds in which the given username has voted
      Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
      Page<Long> userVotedPollIds = voteRepository.findVotedOfferIdsByUserId(user.getId(), pageable);

      if (userVotedPollIds.getNumberOfElements() == 0)
      {
         return new PagedResponse<>(Collections.emptyList(), userVotedPollIds.getNumber(),
               userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(),
               userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
      }

      // Retrieve all poll details from the voted pollIds.
      List<Long> pollIds = userVotedPollIds.getContent();

      Sort sort = new Sort(Sort.Direction.DESC, "finalVotesCount", "createdAt");
      List<Offer> offers = offerRepository.findByIdIn(pollIds, sort);

      // Map Polls to PollResponses containing vote counts and poll creator details
      List<ChoiceVoteCount> choiceVoteCountMap = getChoiceVoteCountList(pollIds);
      Map<Long, Long> pollUserVoteMap = getOfferUserVoteMap(currentUser, pollIds);
      Map<Long, User> creatorMap = getPollCreatorMap(offers);

      List<OfferResponse> offerRespons = offers.stream().map(poll -> {
         return ModelMapper.mapPollToPollResponse(poll,
               choiceVoteCountMap,
               creatorMap.get(poll.getCreatedBy()),
               pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
      }).collect(Collectors.toList());

      return new PagedResponse<>(offerRespons, userVotedPollIds.getNumber(), userVotedPollIds.getSize(),
            userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
   }

   public Offer createOffer(OfferRequest offerRequest)
   {
      Offer offer = new Offer();
      offer.setDescription(offerRequest.getDescription());
      offer.setEmail(offerRequest.getEmail());
      offer.setCity(offerRequest.getCity());
      offer.setHouseNumber(offerRequest.getHouseNumber());
      offer.setPayment(offerRequest.getPayment());
      offer.setStreet(offerRequest.getStreet());
      offer.setPhoneNumber(offerRequest.getPhoneNumber());
      offer.setFinalVotesCount(0L);
      offer.setIsArchived(0L);

      offerRequest.getDimensions().forEach(dimensionRequest -> {
         offer.addDimension(new Dimension(dimensionRequest.getText()));
      });

      Instant now = Instant.now();
      Instant expirationDateTime = now.plus(Duration.ofDays(offerRequest.getOfferLength().getDays()))
            .plus(Duration.ofHours(offerRequest.getOfferLength().getHours()));

      offer.setExpirationDateTime(expirationDateTime);

      return offerRepository.save(offer);
   }

   public boolean setOfferAsObservedBy(Long offerId, Long userId)
   {
      Optional<Observed> observedOptional = observedRepository.
            findObservedOfferIdByUserAndOffer(userId, offerId);
      if (observedOptional.isPresent())
      {
         return false;
      }

      Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));

      if (offer.getExpirationDateTime().isBefore(Instant.now()))
      {
         throw new BadRequestException("Sorry! This Offer has already expired");
      }
      User user = userRepository.findById(userId).get();

      Observed observed = new Observed();
      observed.setOffer(offer);
      observed.setUser(user);

      observedRepository.save(observed);

      return true;
   }

   public boolean setOfferAsArchiveBy(Long offerId, UserPrincipal currentUser)
   {
      Optional<Offer> optionalOffer = offerRepository.findOfferByIsArchived(1L, offerId);
      if (optionalOffer.isPresent())
      {
         return false;
      }

      Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));

      if (offer.getExpirationDateTime().isBefore(Instant.now()))
      {
         throw new BadRequestException("Sorry! This Offer has already expired");
      }

      offer.setIsArchived(1L);

      offerRepository.save(offer);

      return true;
   }

   /**
    * @param pollId
    * @param currentUser
    * @return
    */
   public OfferResponse getOfferById(Long pollId, UserPrincipal currentUser)
   {
      Offer offer = offerRepository.findById(pollId).orElseThrow(
            () -> new ResourceNotFoundException("Offer", "id", pollId));

      // Retrieve Vote Counts of every choice belonging to the current offer
      List<ChoiceVoteCount> votes = voteRepository.countByOfferIdGroupByChoice(pollId);

      Map<Long, Long> choiceVotesMap = votes.stream()
            .collect(Collectors.toMap(ChoiceVoteCount::getChoice, ChoiceVoteCount::getVoteCount));

      // Retrieve offer creator details
      User creator = userRepository.findById(offer.getCreatedBy())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", offer.getCreatedBy()));

      // Retrieve vote done by logged in user
      Vote userVote = null;
      if (currentUser != null)
      {
         userVote = voteRepository.findByUserIdAndOfferId(currentUser.getId(), pollId);
      }

      return ModelMapper.mapPollToPollResponse(offer, votes,
            creator, userVote != null ? userVote.getChoice() : null);
   }

   /**
    * @param offerId
    * @param voteRequest
    * @param currentUser
    * @return
    */
   public OfferResponse castVoteAndGetUpdatedOffer(Long offerId, VoteRequest voteRequest, UserPrincipal currentUser)
   {
      Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException("Offer", "id", offerId));

      if (offer.getExpirationDateTime().isBefore(Instant.now()))
      {
         throw new BadRequestException("Sorry! This Offer has already expired");
      }

      User user = userRepository.getOne(currentUser.getId());

      Long choice = voteRequest.getChoiceId();

      Vote vote = new Vote();
      vote.setOffer(offer);
      vote.setUser(user);
      vote.setChoice(choice);

      try
      {
         vote = voteRepository.save(vote);
      }
      catch (DataIntegrityViolationException ex)
      {
         logger.info("User {} has already voted in Offer {}", currentUser.getId(), offerId);
         throw new BadRequestException("Sorry! You have already cast your vote in this offer");
      }

      //-- Vote Saved, Return the updated Offer Response now --

      // Retrieve Vote Counts of every choice belonging to the current offer
      List<ChoiceVoteCount> votes = voteRepository.countByOfferIdGroupByChoice(offerId);

      Map<Long, Long> choiceVotesMap = votes.stream()
            .collect(Collectors.toMap(ChoiceVoteCount::getChoice, ChoiceVoteCount::getVoteCount));

      long plusVoteCount = choiceVotesMap.get(1L) != null ? choiceVotesMap.get(1L) : 0;
      long minusVoteCount = choiceVotesMap.get(0L) != null ? choiceVotesMap.get(0L) : 0;

      offer.setFinalVotesCount(plusVoteCount - minusVoteCount);
      offerRepository.save(offer);

      // Retrieve offer creator details
      User creator = userRepository.findById(offer.getCreatedBy())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", offer.getCreatedBy()));

      return ModelMapper.mapPollToPollResponse(offer, votes, creator, vote.getChoice());
   }

   private void validatePageNumberAndSize(int page, int size)
   {
      if (page < 0)
      {
         throw new BadRequestException("Page number cannot be less than zero.");
      }

      if (size > AppConstants.MAX_PAGE_SIZE)
      {
         throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
      }
   }

   private List<ChoiceVoteCount> getChoiceVoteCountList(List<Long> pollIds)
   {
      // Retrieve Vote Counts of every Dimension belonging to the given pollIds
      List<ChoiceVoteCount> votes = voteRepository.countByOfferIdInGroupByChoice(pollIds);

      //TODO moze byc przydatne
      // Map<Long, ChoiceVoteCount> choiceVotesMap = votes.stream()
      //       .collect(Collectors.toMap(ChoiceVoteCount::getOfferId, Function.identity()));

      return votes;
   }

   private Map<Long, Long> getOfferUserVoteMap(UserPrincipal currentUser, List<Long> pollIds)
   {
      // Retrieve Votes done by the logged in user to the given pollIds
      Map<Long, Long> pollUserVoteMap = null;
      if (currentUser != null)
      {
         List<Vote> userVotes = voteRepository.findByUserIdAndOfferIdIn(currentUser.getId(), pollIds);

         pollUserVoteMap = userVotes.stream()
               .collect(Collectors.toMap(vote -> vote.getOffer().getId(), Vote::getChoice));
      }
      return pollUserVoteMap;
   }

   Map<Long, User> getPollCreatorMap(List<Offer> offers)
   {
      // Get Offer Creator details of the given list of offers
      List<Long> creatorIds = offers.stream()
            .map(Offer::getCreatedBy)
            .distinct()
            .collect(Collectors.toList());

      List<User> creators = userRepository.findByIdIn(creatorIds);
      Map<Long, User> creatorMap = creators.stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

      return creatorMap;
   }
}
