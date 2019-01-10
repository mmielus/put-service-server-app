package mmielus.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import mmielus.model.audit.DateAudit;
import org.hibernate.annotations.NaturalId;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
      @UniqueConstraint(columnNames = {
            "username"
      }),
      @UniqueConstraint(columnNames = {
            "email"
      })
})
public class User extends DateAudit
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private @NotBlank @Size(max = 40) String name;

   private @NotBlank @Size(max = 15) String username;

   @NaturalId
   private @NotBlank @Size(max = 40) @Email String email;

   private @NotBlank @Size(max = 100) String password;

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(name = "user_roles",
              joinColumns = @JoinColumn(name = "user_id"),
              inverseJoinColumns = @JoinColumn(name = "role_id"))
   private Set<Role> roles = new HashSet<>();

   public User()
   {

   }

   public User(String name, String username, String email, String password)
   {
      this.name = name;
      this.username = username;
      this.email = email;
      this.password = password;
   }

}