package si.um.feri.riris.backendfm.api.auth;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import si.um.feri.riris.backendfm.engine.auth.RegistrationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegistrationService registration;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessions;
    private final SecurityContextRepository contexts;
    private final UserRepository users;

    public AuthController(RegistrationService registration, AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy sessions, SecurityContextRepository contexts,
                          UserRepository users) {
        this.registration = registration;
        this.authenticationManager = authenticationManager;
        this.sessions = sessions;
        this.contexts = contexts;
        this.users = users;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) { return token; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegistrationRequest request) {
        return registration.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest credentials, HttpServletRequest request,
                              HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.email(), credentials.password()));
        sessions.onAuthentication(authentication, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return currentUser(authentication);
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        return users.findByEmail(authentication.getName()).map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Za dostop se prijavi."));
    }
}
