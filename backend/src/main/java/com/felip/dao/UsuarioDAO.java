package com.felip.dao;

import com.felip.model.Usuario;
import com.felip.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioDAO {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean registrar(String username, String passwordHash) {
        if (username == null || username.isBlank()) {
            System.out.println("El username es obligatorio.");
            return false;
        }

        if (usuarioRepository.findByUsername(username).isPresent()) {
            System.out.println("Ese username ya existe.");
            return false;
        }

        usuarioRepository.save(new Usuario(username, passwordHash));
        return true;
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public void actualizarHash(Usuario usuario, String passwordHash) {
        usuario.setPasswordHash(passwordHash);
        usuarioRepository.save(usuario);
    }
}
