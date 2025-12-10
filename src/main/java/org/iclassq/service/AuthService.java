package org.iclassq.service;

import org.iclassq.model.dto.request.LoginRequestDTO;
import org.iclassq.model.dto.response.LoginResponseDTO;

import java.io.IOException;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO dto) throws IOException;
    void logout() throws IOException;
    boolean isAuthenticated();

}
