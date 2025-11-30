package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.dtos.AuthCreateUserRequest;
import com.venta.repuestos.dtos.AuthLoginRequest;
import com.venta.repuestos.dtos.AuthResponse;
import com.venta.repuestos.entidades.securityentities.RoleEntity;
import com.venta.repuestos.entidades.securityentities.UserEntity;
import com.venta.repuestos.repositorios.RoleRepository;
import com.venta.repuestos.repositorios.UserRepository;
import com.venta.repuestos.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceImp implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    //Busca al usuario en la BD
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = repository.findUserEntityByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("El usuario "+username+" no existe") );

        //Creo una lista de autoridades en formato spring security
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        //Tomamos los roles y los convertimos a spring security
        userEntity.getRoles()
                .forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));

        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermisos().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));

        return new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.isEnable(),
                userEntity.isAccountNoExpired(),
                userEntity.isCredentialNoExpired(),
                userEntity.isAccountNoLocked(),
                authorityList);

    }

    //Metodo que nos permite autenticarnos
    public AuthResponse loginUser(AuthLoginRequest authLoginRequest) {
        String username = authLoginRequest.username();
        String password = authLoginRequest.password();

        //si las credenciales son correctas
        Authentication authentication = this.authenticate(username,password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accesToken = jwtUtils.createToken(authentication);

        AuthResponse authResponse = new AuthResponse(username, "User loged succesfully", accesToken, true);
        return authResponse;
    }

    //Busca en la base de datos si el usuario existe
    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username);

        //Verifica si el usuario existe en la base de datos
        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        //Verifica la contraseña
        if (!passwordEncoder.matches(password,userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(username,userDetails.getPassword(),userDetails.getAuthorities());
    }

    public AuthResponse createUser (AuthCreateUserRequest authCreateUserRequest) {
        String username = authCreateUserRequest.username();
        String password = authCreateUserRequest.password();
        List<String> roleRequest = authCreateUserRequest.roleRequest().roleListName();

        //Permite buscar los roles en la base de datos para que coincidan
        Set<RoleEntity> roleEntitySet =  roleRepository.findRoleEntitiesByRoleEnumIn(roleRequest)
                .stream().collect(Collectors.toSet());

        if (roleEntitySet.isEmpty()) {
            throw new IllegalArgumentException("The roles specified does not exist");
        }

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password(new BCryptPasswordEncoder().encode(password))
                .isEnable(true)
                .accountNoExpired(true)
                .accountNoLocked(true)
                .credentialNoExpired(true)
                .roles(roleEntitySet)
                .build();

        UserEntity userBD = repository.save(userEntity);

        ArrayList<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        userBD.getRoles().forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));
        userBD.getRoles()
                .stream()
                .flatMap(role -> role.getPermisos().stream())
                .forEach(permission -> authorityList.add(new SimpleGrantedAuthority(permission.getName())));

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userBD.getUsername(),userBD.getPassword(),authorityList);
        String token = jwtUtils.createToken(authentication);

        AuthResponse authResponse = new AuthResponse(userBD.getUsername(), "User created successfully", token, true);
        return authResponse;
    }
}
