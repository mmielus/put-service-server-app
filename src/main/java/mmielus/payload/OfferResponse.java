package mmielus.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OfferResponse
{
   private Long id;
   private String description;
   private String phoneNumber;
   private String email;
   private String city;
   private String street;
   private String houseNumber;
   private String payment;
   private List<DimensionResponse> dimensions;
   private UserSummary createdBy;
   private Instant creationDateTime;
   private Instant expirationDateTime;
   private Boolean isExpired;

   @JsonInclude(JsonInclude.Include.NON_NULL)
   private Long selectedChoice;
   private Long plusVotes;
   private Long minusVotes;

}
