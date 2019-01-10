package mmielus.payload;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class OfferRequest
{
   private @NotBlank @Size(max = 140) @Valid String description;
   private @NotNull @Size(max = 40) @Valid List<DimensionRequest> dimensions;
   private @NotBlank @Valid String phoneNumber;
   private @NotBlank @Valid String email;
   private @NotBlank @Valid String city;
   private @NotBlank @Valid String street;
   private @NotBlank @Valid String houseNumber;
   private @NotBlank @Valid String payment;
   private @NotNull @Valid OfferLength offerLength;

}

