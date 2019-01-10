package mmielus.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;
import mmielus.model.audit.DateAudit;

@Entity
@Table(name = "votes", uniqueConstraints = {
      @UniqueConstraint(columnNames = {
            "offer_id",
            "user_id"
      })
})
@Getter
@Setter
public class Vote extends DateAudit
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "offer_id", nullable = false)
   private Offer offer;

   private Long choice;

   @ManyToOne(fetch = FetchType.LAZY, optional = false)
   @JoinColumn(name = "user_id", nullable = false)
   private User user;

}
