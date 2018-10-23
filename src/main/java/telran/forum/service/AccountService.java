package telran.forum.service;

import telran.forum.dto.UserProfileDto;
import telran.forum.dto.UserRegisterDto;

public interface AccountService {

	public UserProfileDto addUser(UserRegisterDto userRegDto, String auth);

	public UserProfileDto editUser(UserRegisterDto userRegDto, String auth);

	public UserProfileDto removeUser(String id, String auth);

	public boolean addRole(String id, String auth);

}
