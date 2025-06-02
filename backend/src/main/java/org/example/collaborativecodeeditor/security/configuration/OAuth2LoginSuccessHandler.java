package org.example.collaborativecodeeditor.security.configuration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Value("${file.storage.root}")
    private String fileStorageRoot;

    @Value("${file.storage.history.root}")
    private String fileStorageHistoryRoot;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            Path rootPath = Paths.get(fileStorageRoot);
            Path historyRootPath = Paths.get(fileStorageHistoryRoot);
            assert email != null;
            Path userPath = rootPath.resolve(email);
            Path userHistoryPath = historyRootPath.resolve(email);
            try {
                Files.createDirectories(userPath);
                Files.createDirectories(userHistoryPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            user.setRootPath(userPath.toString());
            user.setHistoryRootPath(userHistoryPath.toString());
            return userRepository.save(user);
        });
        response.sendRedirect("/dashboard");
    }
}