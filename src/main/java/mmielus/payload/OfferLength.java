package mmielus.payload;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OfferLength
{
   private @NotNull @Max(7) Integer days;

   private @NotNull @Max(23) Integer hours;

}
