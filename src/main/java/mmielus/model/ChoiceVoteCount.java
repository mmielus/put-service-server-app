package mmielus.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceVoteCount
{
   private Long choice;
   private Long voteCount;
   private Long offerId;

   public ChoiceVoteCount(Long choice, Long voteCount, Long offerId)
   {
      this.choice = choice;
      this.voteCount = voteCount;
      this.offerId = offerId;
   }

}

