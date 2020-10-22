package com.derteuffel.school.services;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.ERole;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.helpers.EncadrementRegistrationDto;
import com.derteuffel.school.repositories.CompteRepository;
import com.derteuffel.school.repositories.EcoleRepository;
import com.derteuffel.school.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by user on 22/03/2020.
 */
@Service
public class CompteSeviceImpl implements CompteService{

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EcoleRepository ecoleRepository;


    @Override
    public Compte findByUsername(String username) {
        return compteRepository.findByUsername(username);
    }

    @Override
    public Compte findByEmail(String email) {
        return compteRepository.findByEmail(email);
    }

    @Override
    public Compte save(String email, String password, String username, String s) {
        Compte compte = new Compte();

        compte.setEmail(email);
        compte.setPassword(passwordEncoder.encode(password));
        compte.setUsername(username);
        compte.setAvatar(s);
        compte.setEncode(password);
        compte.setStatus(false);

        Role role = new Role();

        if (compteRepository.findAll().size() <=1){
            role.setName(ERole.ROLE_ROOT.toString());
            compte.setType("root");
        }else {
            role.setName(ERole.ROLE_DIRECTEUR.toString());
            compte.setType("directeur");
        }

        Role existRole = roleRepository.findByName(role.getName());
        if (existRole != null){
            compte.setRoles(Arrays.asList(existRole));
        }else {
            roleRepository.save(role);
            compte.setRoles(Arrays.asList(role));
        }
        compteRepository.save(compte);
        return compte;
    }

    @Override
    public Compte saveRoot(CompteRegistrationDto compteRegistrationDto, String s) {
        Compte compte = new Compte();
        compte.setEmail(compteRegistrationDto.getEmail());
        compte.setPassword(passwordEncoder.encode(compteRegistrationDto.getPassword()));
        compte.setUsername(compteRegistrationDto.getUsername());
        compte.setAvatar(s);
        compte.setEncode(compteRegistrationDto.getPassword());
        compte.setCode(UUID.randomUUID().toString());
        compte.setStatus(false);

        Role role = new Role();

        Role existRole = roleRepository.findByName(ERole.ROLE_ROOT.toString());
        if (existRole != null){
            compte.setRoles(Arrays.asList(existRole));
        }else {
            role.setName(ERole.ROLE_ROOT.toString());
            roleRepository.save(role);
            compte.setRoles(Arrays.asList(role));
        }
        compteRepository.save(compte);
        return compte;
    }

    @Override
    public Compte saveEnseignant(CompteRegistrationDto compteRegistrationDto, String s,  Enseignant enseignant) {
        Compte compte = new Compte();

        compte.setEmail(compteRegistrationDto.getEmail());
        compte.setPassword(passwordEncoder.encode(compteRegistrationDto.getPassword()));
        compte.setUsername(compteRegistrationDto.getUsername());
        compte.setAvatar(s);
        compte.setEncode(compteRegistrationDto.getPassword());
        compte.setEnseignant(enseignant);
        compte.setStatus(false);

        Role role = new Role();

        Role existRole = roleRepository.findByName(ERole.ROLE_ENSEIGNANT.toString());
        if (existRole != null){
            compte.setRoles(Arrays.asList(existRole));
        }else {
            role.setName(ERole.ROLE_ENSEIGNANT.toString());
            roleRepository.save(role);
            compte.setRoles(Arrays.asList(role));
        }
        compteRepository.save(compte);
        return compte;
    }

    @Override
    public Compte saveParent(CompteRegistrationDto compteRegistrationDto, String s, Parent parent) {
        Compte compte = new Compte();

        compte.setEmail(compteRegistrationDto.getEmail());
        compte.setPassword(passwordEncoder.encode(compteRegistrationDto.getPassword()));
        compte.setUsername(compteRegistrationDto.getUsername());
        compte.setAvatar(s);
        compte.setEncode(compteRegistrationDto.getPassword());
        compte.setParent(parent);
        compte.setStatus(false);

        Role existRole = roleRepository.findByName(ERole.ROLE_PARENT.toString());
        if (existRole != null){
            compte.setRoles(Arrays.asList(existRole));
        }else {
            Role role = new Role();
            role.setName(ERole.ROLE_PARENT.toString());
            roleRepository.save(role);
            compte.setRoles(Arrays.asList(role));
        }
        compteRepository.save(compte);
        return compte;
    }
    @Override
    public Compte saveEnfant(EncadrementRegistrationDto encadrementRegistrationDto, String s, Enfant enfant) {
        Compte compte = new Compte();

        compte.setEmail(encadrementRegistrationDto.getEmail());
        compte.setPassword(passwordEncoder.encode(encadrementRegistrationDto.getPassword()));
        compte.setUsername(encadrementRegistrationDto.getUsername());
        compte.setAvatar(s);
        compte.setEnfant(enfant);
        compte.setStatus(false);
        compte.setEncode(encadrementRegistrationDto.getPassword());
        compte.setCode(UUID.randomUUID().toString());

        Role existRole = roleRepository.findByName(ERole.ROLE_ENFANT.toString());
        if (existRole != null){
            compte.setRoles(Arrays.asList(existRole));
        }else {
            Role role = new Role();
            role.setName(ERole.ROLE_ENFANT.toString());
            roleRepository.save(role);
            compte.setRoles(Arrays.asList(role));
        }
        compteRepository.save(compte);
        return compte;
    }

    @Override
    public Compte saveEncadreur(EncadrementRegistrationDto encadrementRegistrationDto, String s, Encadreur encadreur) {
        Compte compte = new Compte();

        compte.setEmail(encadrementRegistrationDto.getEmail());
        compte.setPassword(passwordEncoder.encode(encadrementRegistrationDto.getPassword()));
        compte.setUsername(encadrementRegistrationDto.getUsername());
        compte.setAvatar(s);
        compte.setEnseignant(encadreur);
        compte.setEncode(encadrementRegistrationDto.getPassword());
        compte.setCode(UUID.randomUUID().toString());
        compte.setStatus(false);

        Role existRole = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        if (existRole != null){
            compte.setRoles(Arrays.asList(existRole));
        }else {
            Role role = new Role();
            role.setName(ERole.ROLE_ENCADREUR.toString());
            roleRepository.save(role);
            compte.setRoles(Arrays.asList(role));
        }
        compteRepository.save(compte);
        return compte;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        Compte compte = compteRepository.findByUsername(username);
        if (compte == null){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(compte.getUsername(),
                compte.getPassword(),
                mapRolesToAuthorities(compte.getRoles()));
    }

    private Collection <? extends GrantedAuthority> mapRolesToAuthorities(Collection< Role > roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
