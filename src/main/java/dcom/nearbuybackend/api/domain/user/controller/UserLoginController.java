package dcom.nearbuybackend.api.domain.user.controller;

import dcom.nearbuybackend.api.domain.user.dto.UserLoginRequestDto;
import dcom.nearbuybackend.api.domain.user.dto.UserLoginResponseDto;
import dcom.nearbuybackend.api.domain.user.repository.UserRepository;
import dcom.nearbuybackend.api.domain.user.service.UserLoginService;
import dcom.nearbuybackend.api.global.security.config.Token;
import dcom.nearbuybackend.api.global.security.config.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = {"User Controller"})
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @ApiOperation("일반 회원가입")
    @PostMapping("/join")
    public ResponseEntity<Void> joinUser(@RequestBody UserLoginRequestDto.UserJoin data) {
        userLoginService.joinUser(data);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation("일반 로그인")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto.UserLogin> loginUser(@RequestBody UserLoginRequestDto.UserLogin data) {

        if (userLoginService.loginUser(data)) {

            Token token = tokenService.generateToken(data.getId(),"USER");

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                    .maxAge(30 * 24 * 60 * 60) // 만료 기한
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .build();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Set-Cookie", refreshTokenCookie.toString());

            userLoginService.storeRefreshToken(userRepository.findById(data.getId()).get(), token.getRefreshToken());

            return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).body(UserLoginResponseDto.UserLogin.of(token.getAccessToken()));
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 아이디 입니다.");
    }

    @ApiOperation("소셜 로그인")
    @GetMapping("/login/social")
    public void socialLoginUser(HttpServletResponse httpServletResponse, @RequestParam String platform) throws IOException {
        httpServletResponse.sendRedirect("http://localhost:8080/oauth2/authorization/" + platform);
    }

    @ApiOperation("비밀번호 찾기")
    @PostMapping("find")
    public ResponseEntity<Void> findUserPassword(@RequestBody UserLoginRequestDto.UserFindPassword data) {
        userLoginService.findPassword(data);
        return ResponseEntity.ok().build();
    }
}