package com.felip.controller;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felip.config.SecurityUtil;
import com.felip.dao.UsuarioDAO;
import com.felip.model.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public static final String SESSION_USER = "authenticatedUsername";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9._-]{3,50}");
    private final UsuarioDAO usuarioDAO;

    public AuthController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping("/status")
    public AuthResult status(HttpSession session) {
        Object username = session.getAttribute(SESSION_USER);
        return new AuthResult(username != null, usuarioDAO.contarUsuarios() == 0,
                username == null ? null : username.toString(), null);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResult> register(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        String username = normalizeUsername(request.username());
        String validation = validate(username, request.password());
        if (validation != null) {
            return ResponseEntity.badRequest().body(new AuthResult(false, usuarioDAO.contarUsuarios() == 0, null, validation));
        }
        if (usuarioDAO.contarUsuarios() != 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResult(false, false, null, "La cuenta inicial ya fue creada. Inicia sesión."));
        }
        if (!usuarioDAO.registrar(username, SecurityUtil.hashPassword(request.password()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResult(false, true, null, "Ese nombre de usuario ya está registrado."));
        }
        return authenticated(username, httpRequest, true);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResult> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        String username = normalizeUsername(request.username());
        if (username == null || request.password() == null || request.password().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResult(false, usuarioDAO.contarUsuarios() == 0, null,
                    "Escribe tu usuario y contraseña."));
        }

        Optional<Usuario> found = usuarioDAO.buscarPorUsername(username);
        if (found.isEmpty() || !SecurityUtil.verifyPassword(request.password(), found.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResult(false, false, null, "Usuario o contraseña incorrectos."));
        }

        Usuario usuario = found.get();
        if (SecurityUtil.needsPasswordUpgrade(usuario.getPasswordHash())) {
            usuarioDAO.actualizarHash(usuario, SecurityUtil.hashPassword(request.password()));
        }
        return authenticated(usuario.getUsername(), httpRequest, false);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResult> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new AuthResult(false, usuarioDAO.contarUsuarios() == 0, null, "Sesión cerrada."));
    }

    private ResponseEntity<AuthResult> authenticated(String username, HttpServletRequest request, boolean setupComplete) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SESSION_USER, username);
        return ResponseEntity.ok(new AuthResult(true, false, username,
                setupComplete ? "Cuenta creada. Sesión iniciada." : "Sesión iniciada."));
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private String validate(String username, String password) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return "El usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo.";
        }
        if (password == null || password.length() < 8) return "La contraseña debe tener al menos 8 caracteres.";
        if (password.length() > 128) return "La contraseña no puede superar 128 caracteres.";
        return null;
    }

    public record AuthRequest(String username, String password) {}
    public record AuthResult(boolean authenticated, boolean setupRequired, String username, String message) {}
}
