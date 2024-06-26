package br.com.adeweb.magicview.controllers;

import java.util.Optional;

import br.com.adeweb.magicview.dto.UserDto;
import br.com.adeweb.magicview.models.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import br.com.adeweb.magicview.dto.LoginRequest;
import br.com.adeweb.magicview.dto.LoginResponse;
import br.com.adeweb.magicview.security.JWTUtil;
import br.com.adeweb.magicview.services.UserService;

@RestController
@RequestMapping("/auth")
public class UserController {

  private final UserService service;
  private final AuthenticationManager authManager;
  private final JWTUtil jwtUtil;

  public UserController(UserService service, AuthenticationManager authManager, JWTUtil jwtUtil) {
    this.service = service;
    this.authManager = authManager;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping
  public ResponseEntity<?> createUser(@RequestBody User user) {
    try {
      User saved = service.saveUser(user);
      UserDto usrDto = new UserDto(saved.getId(),saved.getNick(),saved.getEmail());
      return ResponseEntity.ok(usrDto);
    }catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }



  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest loginRequest) {

    try {

      Optional<br.com.adeweb.magicview.models.User> opUser = service.findByEmail(loginRequest.email());
      System.out.println(loginRequest.email());
      if (opUser.isEmpty()) {
        throw new RuntimeException("Usuário não Cadastrado");
      }
      UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(opUser.get().getId(),
          loginRequest.password());

      authManager.authenticate(authInputToken);

      String token = jwtUtil.generateToken(opUser.get());
      UserDto userDto = new UserDto(opUser.get().getId(), opUser.get().getNick(), opUser.get().getEmail());
      LoginResponse loginResponse = new LoginResponse(userDto, token);

      return loginResponse;

    } catch (AuthenticationException authExc) {

      throw new RuntimeException("Credenciais Inválidas");
    }
  }

  @GetMapping
  public ResponseEntity<?> getAllUsers (
          @RequestParam(defaultValue = "0") final Integer pageNumber,
          @RequestParam(defaultValue = "10") final Integer size
  ) {
    return ResponseEntity.ok(service.findAll(PageRequest.of(pageNumber,size)));
  }

}
