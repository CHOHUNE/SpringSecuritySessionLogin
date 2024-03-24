package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 이 클래스는 스프링 부트에게 컨피규레이션 클래스로 등록이 된다.
@EnableWebSecurity // 이 클래스가 스프링 시큐리티에서도 관리가 된다.
public class SecurityConfig {
// 이제 이 클래스 내부에다가 특정한 메서드를 작성하고 빈을 등록 시켜주면 시큐리티 필터 체인에
    //기본 설정을  커스텀 하여 진행할 수 있다.


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    } //암호화를 진행할 메서드



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {




        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login", "/join", "/joinProc").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/my/**").hasAnyRole("ADMIN", "USER")  //와일드카드는 별표시 두개
                        .anyRequest().authenticated() //위에서 처리하지 못한 나머지 경로들을 처리하는데 .authenticated는 로그인한 사용자만 나타낸다
                )
                .formLogin((auth) -> auth.
                        loginPage("/login") //역할 : 로그인 페이지를 지정해주는 역할
                        .loginProcessingUrl("/loginProc")
                        .defaultSuccessUrl("/main")//역할 : 로그인 처리를 하는 URL을 지정해주는 역
                        .permitAll()
                )
//                .csrf((auth) -> auth.disable())
                //crsf는 요청을 위조하여 사용자가 원하지 않아도 서버측으로 특정 요청을 강제로 보내는 방식이다.
                // 회원정보 변경, CURD를 사용자 모르게 요청
                // 개발환경에서는 Security Config 클래스를 통해 disable 하였는데 배포환경에서는
                // crsf 공격 방지를 위해 추가적인 설정을 해보도록 하자
                // disable 없앨 시에 POST PUT DELETE 요청에 대해 토큰 검증을 한다.


                .sessionManagement(
                        (auth) ->
                                auth
                                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                        .sessionFixation((sessionFixation)->sessionFixation.newSession())
                                        //세션 고정 공격 방지
                                        //세션 고정 공격 : 세션 고정 공격은 공격자가 세션 ID를 얻어서 사용자에게 이 ID를 사용하도록 유도하는 공격 방법이다.
                                        //이후 사용자가 로그인을 하면 이전에 공격자가 얻은 세션 ID를 사용하여 세션을 탈취하는 공격이다.
                                        //세션 고정 공격을 방지하기 위해서는 사용자가 로그인을 할 때마다 새로운 세션 ID를 발급해야 한다.
                                        //이것을 세션 고정 공격 방지라고 한다.
                                        .maximumSessions(1)
                                        .maxSessionsPreventsLogin(true)  // 중복 로그인  (true : 로그인 차단) , (false : 초과시 기존 세션 하나 삭제 )

                )
                .logout((auth) -> auth.logoutUrl("/logout")
                        .logoutSuccessUrl("/"));


        return http.build();
    }

//    인 메모리 저장 방식
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user1 = User.builder()
//                .username("user1")
//                .password(bCryptPasswordEncoder().encode("1234"))
//                .roles("ADMIN")
//                .build();
//
//        UserDetails user2 = User.builder()
//                .username("user2")
//                .password(bCryptPasswordEncoder().encode("1234"))
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(user1, user2);
//    }
}


//시큐리티 컨피그를 이용하여 해당 접근경로에 대해 접근을 제한/ 허용 하는법
// 실행 순서는 상단에서 하단 순으로 실행 된다.
// 모든 경로 anyRequest에 대한 설정은 맨 아래에다가 설정 해야한다.
