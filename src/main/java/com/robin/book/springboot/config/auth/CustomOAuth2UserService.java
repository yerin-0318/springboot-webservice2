package com.robin.book.springboot.config.auth;

import com.robin.book.springboot.config.auth.dto.OAuthAttributes;
import com.robin.book.springboot.config.auth.dto.SessionUser;
import com.robin.book.springboot.domain.user.User;
import com.robin.book.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user",new SessionUser(user));

        return new DefaultOAuth2User(Collections.singleton
                (new SimpleGrantedAuthority(user.getRoleKey())),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes){
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity->entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity()); //로그인 안한 경우에는 Role.GUEST를 role로 가지는 유저 반환된다.
        return userRepository.save(user);
    }
}
