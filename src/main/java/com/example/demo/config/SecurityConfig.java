package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
                .authorizeHttpRequests((auth) ->
                        auth
                                .requestMatchers("/", "/login").permitAll()
                                .requestMatchers("/admin").hasRole("ADMIN")
                                .requestMatchers("/my/**").hasAnyRole("ADMIN", "USER")  //와일드카드는 별표시 두개
                                .anyRequest().authenticated() //위에서 처리하지 못한 나머지 경로들을 처리하는데 .authenticated는 로그인한 사용자만 나타낸다
                );
        // 특정 경로에 대해 허용하거나 거부 하는 메서드 : 신버전 부터는 반드시 람다식으로 작성 해야 한다.

        http
                .formLogin((auth) -> auth.loginPage("/login") //역할 : 로그인 페이지를 지정해주는 역할
                        .loginProcessingUrl("/loginProc")//역할 : 로그인 처리를 하는 URL을 지정해주는 역
                        .permitAll()
                );

        // 사이트 위변조 방지 설정이 자동으로 설정 되어 있어 로그인 할때 반드시 이 csrf 관련 토큰도 전송 해주어야 한다.
        // 일시적으로 disable() 해놓고 추후에 추가 예정
        http
                .csrf((auth) -> auth.disable());


        return http.build();
    }
}


//시큐리티 컨피그를 이용하여 해당 접근경로에 대해 접근을 제한/ 허용 하는법
// 실행 순서는 상단에서 하단 순으로 실행 된다.
// 모든 경로 anyRequest에 대한 설정은 맨 아래에다가 설정 해야한다.
