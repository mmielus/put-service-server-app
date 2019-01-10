package mmielus.payload;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChoiceResponse
{
   private long plusVoteCount;
   private long minusVoteCount;
}
