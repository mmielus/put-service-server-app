package mmielus.controller;

import mmielus.exception.ResourceNotFoundException;
import mmielus.model.User;
import mmielus.payload.OfferResponse;
import mmielus.payload.PagedResponse;
import mmielus.payload.UserIdentityAvailability;
import mmielus.payload.UserProfile;
import mmielus.payload.UserSummary;
import mmielus.repository.OfferRepository;
import mmielus.repository.UserRepository;
import mmielus.repository.VoteRepository;
import mmielus.security.CurrentUser;
import mmielus.security.UserPrincipal;
import mmielus.service.OfferService;
import mmielus.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController
{

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private OfferRepository offerRepository;

   @Autowired
   private VoteRepository voteRepository;

   @Autowired
   private OfferService offerService;

   private static final Logger logger = LoggerFactory.getLogger(UserController.class);

   @GetMapping("/user/me")
   @PreAuthorize("hasRole('USER')")
   public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser)
   {
      UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
      return userSummary;
   }

   @GetMapping("/user/checkUsernameAvailability")
   public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username)
   {
      Boolean isAvailable = !userRepository.existsByUsername(username);
      return new UserIdentityAvailability(isAvailable);
   }

   @GetMapping("/user/checkEmailAvailability")
   public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email)
   {
      Boolean isAvailable = !userRepository.existsByEmail(email);
      return new UserIdentityAvailability(isAvailable);
   }

   @GetMapping("/users/{username}")
   public UserProfile getUserProfile(@PathVariable(value = "username") String username)
   {
      User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

      long pollCount = offerRepository.countByCreatedBy(user.getId());
      long voteCount = voteRepository.countByUserId(user.getId());

      UserProfile userProfile =
            new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), pollCount,
                  voteCount);

      return userProfile;
   }

   @GetMapping("/users/{username}/offers")
   public PagedResponse<OfferResponse> getPollsCreatedBy(@PathVariable(value = "username") String username,
         @CurrentUser UserPrincipal currentUser,
         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size)
   {
      return offerService.getOffersCreatedBy(username, currentUser, page, size);
   }

   @GetMapping("/users/{username}/votes")
   public PagedResponse<OfferResponse> getPollsVotedBy(@PathVariable(value = "username") String username,
         @CurrentUser UserPrincipal currentUser,
         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size)
   {
      return offerService.getOffersVotedBy(username, currentUser, page, size);
   }

}
