package telran.forum.service.filter;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import telran.forum.configuration.AccountConfiguration;
import telran.forum.configuration.AccountUserCredential;
import telran.forum.dao.UserAccountRepository;
import telran.forum.domain.UserAccount;

@Service
@Order(1)
public class PasswordFilter implements Filter {

	@Autowired
	AccountConfiguration configuration;

	@Autowired
	UserAccountRepository repository;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		AccountUserCredential credentials = configuration.tokenDecode(request.getHeader("Authorization"));

		UserAccount user = repository.findById(credentials.getLogin()).orElse(null);

		if (!request.getServletPath().startsWith("/account/register")) {

			if (user == null || user.getExpDate().isBefore(LocalDateTime.now())) {
				response.sendError(401, "Unauthorized");

			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

}
