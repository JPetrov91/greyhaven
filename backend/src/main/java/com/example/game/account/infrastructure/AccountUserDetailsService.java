package com.example.game.account.infrastructure;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

	private final AccountRepository accountRepository;

	public AccountUserDetailsService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AccountEntity account = accountRepository.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("Account not found"));
		return new AccountPrincipal(account.getId(), account.getEmail(), account.getPasswordHash());
	}
}
