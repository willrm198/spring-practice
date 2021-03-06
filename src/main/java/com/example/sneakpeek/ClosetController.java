package com.example.sneakpeek;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sneakpeek.ItemRepository;

@RestController
public class ClosetController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ItemRepository itemRepository;

	/**
	 * Add item to users closet
	 * @param closetReq - request object containing username and closet item
	 * @return response object with message and new 
	 */
	@PostMapping(path = "/Closet", consumes = "application/json", produces = "application/json")
	public ClosetResponse addClosetItem(@RequestBody ClosetRequest closetReq) {
		String username = closetReq.getUsername();
		ClosetItem item = closetReq.getClosetItem();

		User user = userRepository.findByUsername(username);
		String userID = user.getId();

		ClosetItem newItem = new ClosetItem(userID, item.itemSize, item.itemBrand, item.itemModel, item.itemDescription,
				item.itemIsVisible);
		itemRepository.save(newItem);

		return new ClosetResponse("Closet Item Added!", itemRepository.findByUserId(userID));
	}

	/**
	 * Get user's closet by username
	 * @param username - username of users closet to get
	 * @param sortField - field to sort list on
	 * @param sortDir - direction to sort
	 * @return response object with message and sorted closet
	 */
	@GetMapping(path = "/Closet/{username}")
	public ClosetResponse getCloset(@PathVariable("username") String username,
			@RequestParam(name = "sortField", required = false, defaultValue = "size") String sortField,
			@RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir) {

		User user = userRepository.findByUsername(username);
		List<ClosetItem> closet = itemRepository.findByUserId(user.getId());

		List<ClosetItem> visibleCloset = closet.stream().filter(c -> c.getitemIsVisible() == true)
				.collect(Collectors.toList());

		String message = visibleCloset.isEmpty() ? "No closet found!" : "Closet found!";
	
		List<ClosetItem> sortedCloset = sortCloset(visibleCloset, sortDir, sortField);
		
		return new ClosetResponse(message, sortedCloset);
	}

	/**
	 * Sort closet given field and sort direction
	 * @param closet - closet list of items
	 * @param sortDir - direction to sort list
	 * @param field - field to sort list on
	 * @return sorted closet
	 */
	public List<ClosetItem> sortCloset(List<ClosetItem> closet, String sortDir, String field) {
		if (sortDir.compareToIgnoreCase("asc") == 0) {
			Collections.sort(closet, (c1, c2) -> {
				switch (field.toUpperCase()) {
				case "BRAND":
					return c1.getItemBrand().compareToIgnoreCase(c2.getItemBrand());
				case "MODEL":
					return c1.getItemModel().compareToIgnoreCase(c2.getItemModel());
				case "SIZE":
					return Double.compare(c1.getItemSize(), c2.getItemSize());
				default:
					throw new InvalidParameterException("sort field invalid");
				}
			});
		} else if (sortDir.compareToIgnoreCase("desc") == 0) {
			Collections.sort(closet, (c1, c2) -> {
				switch (field.toUpperCase()) {
				case "BRAND":
					return c2.getItemBrand().compareToIgnoreCase(c1.getItemBrand());
				case "MODEL":
					return c2.getItemModel().compareToIgnoreCase(c1.getItemModel());
				case "SIZE":
					return Double.compare(c2.getItemSize(), c1.getItemSize());
				default:
					throw new InvalidParameterException("sort field invalid");
				}
			});
		} else {
			throw new InvalidParameterException("sort value must be 'asc' or 'desc'.");
		}

		return closet;
	}
}
