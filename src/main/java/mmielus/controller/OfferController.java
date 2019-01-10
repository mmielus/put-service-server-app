package mmielus.controller;

import java.net.URI;

import javax.validation.Valid;

import mmielus.model.Offer;
import mmielus.payload.ApiResponse;
import mmielus.payload.OfferRequest;
import mmielus.payload.OfferResponse;
import mmielus.payload.PagedResponse;
import mmielus.payload.VoteRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * @author Michal
 */

@RestController
@RequestMapping("/api/offers")
public class OfferController
{

   @Autowired
   private OfferRepository offerRepository;

   @Autowired
   private VoteRepository voteRepository;

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private OfferService offerService;

   private static final Logger logger = LoggerFactory.getLogger(OfferController.class);

   @GetMapping
   public PagedResponse<OfferResponse> getOffers(@CurrentUser UserPrincipal currentUser,
         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size)
   {
      return offerService.getAllOffers(currentUser, page, size);
   }

   @GetMapping("/archived")
   public PagedResponse<OfferResponse> getArchivedOffers(@CurrentUser UserPrincipal currentUser,
         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size)
   {
      return offerService.getArchivedOffers(currentUser, page, size);
   }

   @GetMapping("/observed")
   public PagedResponse<OfferResponse> getObservedOffers(@CurrentUser UserPrincipal currentUser,
         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size)
   {
      return offerService.getObservedOffers(currentUser, page, size);
   }

   @PostMapping
   @PreAuthorize("hasRole('USER')")
   public ResponseEntity<?> createOffer(@Valid @RequestBody OfferRequest offerRequest)
   {
      Offer offer = offerService.createOffer(offerRequest);

      URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{offerId}")
            .buildAndExpand(offer.getId()).toUri();

      return ResponseEntity.created(location)
            .body(new ApiResponse(true, "Offer Created Successfully"));
   }

   @GetMapping("/{offerId}")
   public OfferResponse getOfferById(@CurrentUser UserPrincipal currentUser,
         @PathVariable Long offerId)
   {
      return offerService.getOfferById(offerId, currentUser);
   }

   @PostMapping("/{offerId}/votes")
   @PreAuthorize("hasRole('USER')")
   public OfferResponse castVote(@CurrentUser UserPrincipal currentUser,
         @PathVariable Long offerId,
         @Valid @RequestBody VoteRequest voteRequest)
   {
      return offerService.castVoteAndGetUpdatedOffer(offerId, voteRequest, currentUser);
   }

   @PostMapping("/{offerId}/observe")
   @PreAuthorize("hasRole('USER')")
   public boolean setOfferObserveBy(@CurrentUser UserPrincipal currentUser,
         @PathVariable Long offerId)
   {
      return offerService.setOfferAsObservedBy(offerId, currentUser.getId());
   }

   @PostMapping("/{offerId}/observe/{username}")
   @PreAuthorize("hasRole('USER')")
   public boolean setOfferObserveBy(@PathVariable String username,
         @PathVariable Long offerId)
   {

      return offerService.setOfferAsObservedBy(offerId, userRepository.findByUsername(username).get().getId());
   }

   @PostMapping("/{offerId}/archive")
   @PreAuthorize("hasRole('USER')")
   public boolean setOfferArchive(@CurrentUser UserPrincipal currentUser,
         @PathVariable Long offerId)
   {
      return offerService.setOfferAsArchiveBy(offerId, currentUser);
   }

}
