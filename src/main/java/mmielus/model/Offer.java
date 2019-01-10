package mmielus.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mmielus.model.audit.UserDateAudit;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Michal
 */
@Entity
@Table(name = "offer")
@Getter
@Setter
@ToString
public class Offer extends UserDateAudit
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private @NotBlank @Size(max = 240) String description;
   private @NotBlank String phoneNumber;
   private @NotBlank String email;
   private @NotBlank String city;
   private @NotBlank String street;
   private @NotBlank String houseNumber;
   private @NotBlank String payment;
   private Long finalVotesCount;
   private @NotNull Instant expirationDateTime;
   private @NotNull Long isArchived;

   @BatchSize(size = 30)
   @Fetch(FetchMode.SELECT)
   @OneToMany(
         mappedBy = "offer",
         cascade = CascadeType.ALL,
         fetch = FetchType.EAGER,
         orphanRemoval = true
   )
   private @Size(min = 2, max = 6) List<Dimension> dimensions = new ArrayList<>();

   public void addDimension(Dimension dimension)
   {
      dimensions.add(dimension);
      dimension.setOffer(this);
   }

   public void removeDimension(Dimension dimension)
   {
      dimensions.remove(dimension);
      dimension.setOffer(null);
   }
}
