package telran.forum.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import telran.forum.configuration.AccountConfiguration;
import telran.forum.configuration.AccountUserCredential;
import telran.forum.dao.UserAccountRepository;
import telran.forum.domain.UserAccount;
import telran.forum.dto.UserProfileDto;
import telran.forum.dto.UserRegisterDto;

@Service
public class AccountServiceImpl implements AccountService {
	@Autowired
	UserAccountRepository userRepository;

	@Autowired
	AccountConfiguration accountConfiguration;

	@Override
	public UserProfileDto addUser(UserRegisterDto userRegDto, String auth) {
		AccountUserCredential credentials = accountConfiguration.tokenDecode(auth);
		if (userRepository.existsById(credentials.getLogin())) {
			throw new UserExistException();
		}
		String hashPassword = BCrypt.hashpw(credentials.getPassword(), BCrypt.gensalt());
		UserAccount userAccount = UserAccount.builder().id(credentials.getLogin()).password(hashPassword)
				.firstName(userRegDto.getFirstName()).lastName(userRegDto.getLastName()).role("User")
				.expDate(LocalDateTime.now().plusDays(accountConfiguration.getExpPeriod())).build();
		userRepository.save(userAccount);
		return new UserProfileDto(credentials.getLogin(), userRegDto.getFirstName(), userRegDto.getLastName());
	}

	@Override
	public UserProfileDto editUser(UserRegisterDto userRegDto, String auth) {
		AccountUserCredential credentials = accountConfiguration.tokenDecode(auth);
		UserAccount userAccount = userRepository.findById(credentials.getLogin()).get();
		userAccount.setFirstName(userRegDto.getFirstName());
		userAccount.setLastName(userRegDto.getLastName());
		userRepository.save(userAccount);
		return new UserProfileDto(credentials.getLogin(), userRegDto.getFirstName(), userRegDto.getLastName());
	}

	@Override
	public UserProfileDto removeUser(String id, String auth) {
		AccountUserCredential credentials = accountConfiguration.tokenDecode(auth);
		UserAccount user = userRepository.findById(credentials.getLogin()).get();
		Set<String> roles = user.getRoles();
		boolean hasRight = roles.stream().anyMatch(s -> "Admin".equals(s) || "Moderator".equals(s));
		hasRight = hasRight || credentials.getLogin().equals(id);
		if (!hasRight) {
			throw new ForbiddenException();
		}
		UserAccount userAccount = userRepository.findById(id).get();
		userRepository.delete(userAccount);
		return new UserProfileDto(userAccount.getId(), userAccount.getFirstName(), userAccount.getLastName());
	}

	@Override
	public boolean addRole(String id, String auth) {
		AccountUserCredential credentials = accountConfiguration.tokenDecode(auth);
		UserAccount user = userRepository.findById(credentials.getLogin()).orElse(null);
		if (user != null) {
			if (!user.getRoles().contains("Admin")) {
				throw new ForbiddenException();
			}
		}

		UserAccount user1 = userRepository.findById(id).orElse(null);

		if (user1 != null) {
			boolean res = user1.getRoles().add("Moderator");
			userRepository.save(user1);
			return res;
		}

		return false;
	}

}
