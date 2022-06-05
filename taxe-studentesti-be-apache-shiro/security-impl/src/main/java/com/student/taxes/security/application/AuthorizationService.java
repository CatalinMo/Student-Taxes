package com.student.taxes.security.application;

import com.student.taxes.application.AccountServiceApi;
import com.student.taxes.security.domain.dto.TokenWrapperDto;
import com.student.taxes.security.domain.dto.UserAuthentication;
import com.student.taxes.security.domain.dto.UserResponseDto;
import com.student.taxes.security.exception.AuthenticationFailException;
import com.student.taxes.domain.response.UserIdentityResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorizationService {

    private final AccountServiceApi accountService;
    private final JwtTokenProvider tokenProvider;

    public TokenWrapperDto verifyCredentials(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String credentials = extractCredentials(authorization);
        String[] values = credentials.split(":", 2);
        UserIdentityResponseDto user = accountService.getUserIdentity(values[0], values[1]);
        if (user != null) {
            return getAuthorizationToken(user);
        }
        throw new AuthenticationFailException("Credentialele sunt invalide");
    }

    private TokenWrapperDto getAuthorizationToken(UserIdentityResponseDto user) {
        List<String> roles = tokenProvider.addRoles(user.getRole());
        UserAuthentication auth =
                new UserAuthentication(user.getEmail(), user.getPassword(), roles);
        String token = tokenProvider.createToken(auth);
        return new TokenWrapperDto(convertToUserResponse(user), token);
    }

    private UserResponseDto convertToUserResponse(UserIdentityResponseDto user) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setEmail(user.getEmail());
        responseDto.setCnp(user.getCnp());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());
        responseDto.setRole(user.getRole());
        return responseDto;
    }

    private String extractCredentials(String authorization) {
        String base64Credentials = authorization.substring("Basic ".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        return new String(credDecoded, StandardCharsets.UTF_8);
    }
}
