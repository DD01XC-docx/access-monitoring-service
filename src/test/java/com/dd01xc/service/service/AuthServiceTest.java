package com.dd01xc.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.model.LoginRequest;
import com.dd01xc.service.model.LoginResponse;
import com.dd01xc.service.model.RegisterRequest;
import com.dd01xc.service.model.RegisterResponse;
import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.service.exception.DuplicateAccountException;
import com.dd01xc.service.service.exception.InvalidCredentialsException;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    //const
    private static final String IP = "10.0.0.1";
    private static final String EMAIL = "john@example.com";
    private static final String USERNAME = "john";
    private static final String RAW_PASSWORD = "password123";
    private static final String HASH = "hashed";

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessRepository accessRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUsername(USERNAME);
        activeUser.setEmail(EMAIL);
        activeUser.setPassword(HASH);
        activeUser.setRole("USER");
        activeUser.setEnabled(true);
        activeUser.setStatus(User.STATUS_ACTIVE);
    }

    private LoginRequest loginRequest(String identifier, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(identifier);
        request.setPassword(password);
        return request;
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(USERNAME);
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);
        return request;
    }

    private AccessEvent capturedEvent() {
        ArgumentCaptor<AccessEvent> captor = ArgumentCaptor.forClass(AccessEvent.class);
        verify(accessRepository).save(captor.capture());
        return captor.getValue();
    }

    //login

    @Test
    void login_validEmailAndPassword_returnsTokenAndUpdatesUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
        when(jwtService.generateToken(USERNAME, "USER")).thenReturn("jwt-token");

        LoginResponse response = authService.login(loginRequest(EMAIL, RAW_PASSWORD), IP);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.role()).isEqualTo("USER");
        verify(userRepository).save(activeUser);
        assertThat(activeUser.getLastIp()).isEqualTo(IP);
        assertThat(activeUser.getLastLogin()).isNotNull();
    }

    @Test
    void login_validCredentials_savesSuccessAccessEvent() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
        when(jwtService.generateToken(USERNAME, "USER")).thenReturn("jwt-token");

        authService.login(loginRequest(EMAIL, RAW_PASSWORD), IP);

        AccessEvent event = capturedEvent();
        assertThat(event.getStatus()).isEqualTo("SUCCESS");
        assertThat(event.getUsernameOrEmail()).isEqualTo(EMAIL);
        assertThat(event.getIpAddress()).isEqualTo(IP);
        assertThat(event.getDurationMs()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    @Test
    void login_accountLocked_throwsLockedSavesLockedEventSkipsPasswordCheck() {
        when(loginAttemptService.remainingLock(EMAIL)).thenReturn(Optional.of(java.time.Duration.ofMinutes(3)));

        assertThatThrownBy(() -> authService.login(loginRequest(EMAIL, RAW_PASSWORD), IP))
                .isInstanceOf(com.dd01xc.service.service.exception.AccountLockedException.class);

        assertThat(capturedEvent().getStatus()).isEqualTo("LOCKED");
        verifyNoInteractions(passwordEncoder, userRepository, jwtService);
    }

    @Test
    void login_usernameAsIdentifier_throwsInvalidCredentialsWithoutUsernameLookup() {
        when(userRepository.findByEmail(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest(USERNAME, RAW_PASSWORD), IP))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void login_unknownUser_throwsInvalidCredentialsAndSavesFailedEvent() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest("ghost@example.com", RAW_PASSWORD), IP))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        //bcrypt still runs against dummy hash
        verify(passwordEncoder).matches(eq(RAW_PASSWORD), startsWith("$2a$10$"));
        assertThat(capturedEvent().getStatus()).isEqualTo("FAILED");
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsAndSavesFailedEvent() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpass1", HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest(EMAIL, "wrongpass1"), IP))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        AccessEvent event = capturedEvent();
        assertThat(event.getStatus()).isEqualTo("FAILED");
        assertThat(event.getUsernameOrEmail()).isEqualTo(EMAIL);
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_userNotEnabled_throwsInvalidCredentialsAndSavesFailedEvent() {
        activeUser.setEnabled(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest(EMAIL, RAW_PASSWORD), IP))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        assertThat(capturedEvent().getStatus()).isEqualTo("FAILED");
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_statusNotActive_throwsInvalidCredentials() {
        activeUser.setStatus("SUSPENDED");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest(EMAIL, RAW_PASSWORD), IP))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(capturedEvent().getStatus()).isEqualTo("FAILED");
        verifyNoInteractions(jwtService);
    }

    //register

    @Test
    void register_newAccount_savesEncodedUserAndReturnsResponse() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASH);

        RegisterResponse response = authService.register(registerRequest(), IP);

        assertThat(response.username()).isEqualTo(USERNAME);
        assertThat(response.role()).isEqualTo("USER");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(USERNAME);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getPassword()).isEqualTo(HASH).isNotEqualTo(RAW_PASSWORD);
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(User.STATUS_ACTIVE);
        assertThat(saved.getLastIp()).isEqualTo(IP);
        assertThat(saved.getLastLogin()).isNotNull();
    }

    @Test
    void register_newAccount_savesSuccessAccessEvent() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASH);

        authService.register(registerRequest(), IP);

        AccessEvent event = capturedEvent();
        assertThat(event.getStatus()).isEqualTo("SUCCESS");
        assertThat(event.getUsernameOrEmail()).isEqualTo(EMAIL);
        assertThat(event.getIpAddress()).isEqualTo(IP);
        assertThat(event.getDurationMs()).isNotNull();
    }

    @Test
    void register_emailAlreadyExists_throwsDuplicateAccountAndSavesFailedEvent() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest(), IP))
                .isInstanceOf(DuplicateAccountException.class)
                .hasMessage("Account already exists");

        assertThat(capturedEvent().getStatus()).isEqualTo("FAILED");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_usernameAlreadyExists_throwsDuplicateAccountAndSavesFailedEvent() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest(), IP))
                .isInstanceOf(DuplicateAccountException.class)
                .hasMessage("Account already exists");

        assertThat(capturedEvent().getStatus()).isEqualTo("FAILED");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    //checkAuth

    @Test
    void checkAuth_always_returnsSuccessMessage() {
        assertThat(authService.checkAuth()).isEqualTo("Authenticated successfully");
    }
}
