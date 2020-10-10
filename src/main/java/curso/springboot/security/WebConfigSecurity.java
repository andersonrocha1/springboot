package curso.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ImplementaUserDetailsService implementaUserDetailsService;
	
	@Override // Configura as solicitações por acesso por HTTP
	protected void configure(HttpSecurity http) throws Exception {
		
		http.csrf()
		.disable()  //Desativa configurações padrões de memória
		.authorizeRequests() // Permitir restringir acessos
		.antMatchers(HttpMethod.GET, "/").permitAll() // Qualquer usuario acessa a página
		.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN")
		.anyRequest().authenticated()
		.and().formLogin().permitAll() // Permite qualquer usuario
		.loginPage("/login")
		.defaultSuccessUrl("/cadastropessoa")
		.failureUrl("/login?error=true")
		.and().logout()  // Mapeia URL de logout e invalida
		.logoutSuccessUrl("/login")   //Página padrão ao fazer logout
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
		
	}
	
	@Override // Cria autenticação entre usuário e banco de dados ou em memória
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(implementaUserDetailsService)
		.passwordEncoder(new BCryptPasswordEncoder());
		
		

	}
	
	@Override // Ignora URL específica
	public void configure(WebSecurity web) throws Exception {
		
		web.ignoring().antMatchers("/materialize/**");
		
	
	}

}
