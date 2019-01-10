package mmielus.util;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import mmielus.model.ChoiceVoteCount;
import mmielus.model.Offer;
import mmielus.model.User;
import mmielus.payload.ChoiceResponse;
import mmielus.payload.DimensionResponse;
import mmielus.payload.OfferResponse;
import mmielus.payload.UserSummary;

public class ModelMapper
{

   public static OfferResponse mapPollToPollResponse(Offer offer, List<ChoiceVoteCount> choiceVotesMap, User creator,
         Long userVote)
   {
      OfferResponse offerResponse = new OfferResponse();
      offerResponse.setId(offer.getId());
      offerResponse.setDescription(offer.getDescription());
      offerResponse.setCreationDateTime(offer.getCreatedAt());
      offerResponse.setExpirationDateTime(offer.getExpirationDateTime());
      offerResponse.setCity(offer.getCity());
      offerResponse.setStreet(offer.getStreet());
      offerResponse.setPhoneNumber(offer.getPhoneNumber());
      offerResponse.setHouseNumber(offer.getHouseNumber());
      offerResponse.setEmail(offer.getEmail());
      offerResponse.setPayment(offer.getPayment());

      Instant now = Instant.now();
      offerResponse.setIsExpired(offer.getExpirationDateTime().isBefore(now));

      List<DimensionResponse> dimensionResponseList = offer.getDimensions().stream().map(dimension -> {
         DimensionResponse dimensionResponse = new DimensionResponse();
         dimensionResponse.setId(dimension.getId());
         dimensionResponse.setText(dimension.getText());

         return dimensionResponse;
      }).collect(Collectors.toList());

      ChoiceResponse choiceResponse = new ChoiceResponse();
      if (choiceVotesMap.stream().anyMatch(x -> x.getOfferId().equals(offer.getId())))
      {
         Optional<ChoiceVoteCount> minusVoteCount =
               choiceVotesMap.stream().filter(x -> x.getOfferId().equals(offer.getId()))
                     .filter(x -> x.getChoice().equals(0L)).findFirst();

         Optional<ChoiceVoteCount> plusVoteCount =
               choiceVotesMap.stream().filter(x -> x.getOfferId().equals(offer.getId()))
                     .filter(x -> x.getChoice().equals(1L)).findFirst();

         minusVoteCount.ifPresent(choiceVoteCount -> choiceResponse.setMinusVoteCount(choiceVoteCount.getVoteCount()));
         plusVoteCount.ifPresent(choiceVoteCount -> choiceResponse.setPlusVoteCount(choiceVoteCount.getVoteCount()));

      }
      else
      {
         choiceResponse.setMinusVoteCount(0);
         choiceResponse.setPlusVoteCount(0);
      }

      offerResponse.setDimensions(dimensionResponseList);
      UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName());
      offerResponse.setCreatedBy(creatorSummary);

      if (userVote != null)
      {
         offerResponse.setSelectedChoice(userVote);
      }
      //TODO bardzo do poprawy

      offerResponse.setMinusVotes(choiceResponse.getMinusVoteCount());
      offerResponse.setPlusVotes(choiceResponse.getPlusVoteCount());

      return offerResponse;
   }

}
