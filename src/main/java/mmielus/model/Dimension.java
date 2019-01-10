package mmielus.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Michal
 */

@Entity
@Table(name = "dimension")
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Setter
@Getter
public class Dimension
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private @NonNull @NotBlank @Size(max = 40) String text;

   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "offer_id", nullable = false)
   private Offer offer;

}
