package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.ERole;
import com.derteuffel.school.enums.EType;
import com.derteuffel.school.enums.EVisibilite;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Mail;
import com.derteuffel.school.services.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 23/03/2020.
 */
@Controller
@RequestMapping("/prefet")
public class PrefetLoginController {

    @Autowired
    private EcoleRepository ecoleRepository;

    @Autowired
    private CompteService compteService;
    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private  RoleRepository roleRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private EleveRepository eleveRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Multipart multipart;
    @Value("${file.upload-dir}")
    private String location ;
    @GetMapping("/login")
    public String director() {
        return "prefet/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/prefet/login";
    }
    @ModelAttribute("compte")
    public CompteRegistrationDto compteRegistrationDto() {
        return new CompteRegistrationDto();
    }

    @GetMapping("/registration")
    public String registrationForm(Model model, RedirectAttributes redirectAttributes) {

        List<Compte> comptes = compteRepository.findAll();
        Role role = roleRepository.findByName(ERole.ROLE_DIRECTEUR.toString());
        List<Compte> accessAccount = new ArrayList<>();

        for (Compte compte : comptes){
            if (compte.getRoles().contains(role)){
                accessAccount.add(compte);
            }
        }

        if (accessAccount.size() != 0){
            redirectAttributes.addFlashAttribute("message","Desole il existe deja un directeur dans votre etablissement, veuillez le contacter pour vous enregistrer");
            return "redirect:/";
        }
        return "prefet/registration";
    }

    @GetMapping("/registration/root")
    public String registrationRoot(Model model) {
        return "prefet/rootRegistration";
    }


    @PostMapping("/registration")
    public String registrationDirectionSave(@ModelAttribute("compte") @Valid CompteRegistrationDto compteDto,
                                            BindingResult result, RedirectAttributes redirectAttributes, Model model, @RequestParam("file") MultipartFile file) {

        multipart.store(file);
        Compte existAccount = compteService.findByUsername(compteDto.getUsername());
        if (existAccount != null) {
            result.rejectValue("username", null, "Il existe deja un compte avec ce nom d'utilisateur vueillez choisir un autre");
            model.addAttribute("error", "Il existe deja un compte avec ce nom d'utilisateur vueillez choisir un autre");
        }

        if (result.hasErrors()) {
            return "prefet/registration";
        }

            if (compteRepository.findAll().size() > 0) {
                model.addAttribute("error", "Cet Etablissement a deja un dirigeant veuillez choisir celui que vous avez creer");
                return "prefet/registration";
            }
            compteService.save(compteDto.getEmail(),compteDto.getPassword(), compteDto.getUsername(), "/upload-dir/"+file.getOriginalFilename());
            Mail sender = new Mail();
            sender.sender(
                    "confirmation@yesbanana.org",
                    "Enregistrement d'un directeur ou responsable",
                    "Viens de s'enregistrer comme directeur du lycee cirezi de Bukavu");


        redirectAttributes.addFlashAttribute("success", "Votre enregistrement a ete effectuer avec succes");
        return "redirect:/prefet/login";
    }


    @PostMapping("/registration/root")
    public String registrationRoot(@ModelAttribute("compte") @Valid CompteRegistrationDto compteDto,
                                            BindingResult result, RedirectAttributes redirectAttributes, Model model) {

        Compte existAccount = compteService.findByUsername(compteDto.getUsername());
        if (existAccount != null) {
            result.rejectValue("username", null, "Il existe déjà un compte avec ce nom d'utilisateur veuillez choisir un autre");
            redirectAttributes.addFlashAttribute("error", "Il existe deja un compte avec ce nom d'utilisateur vueillez choisir un autre");
        }

        if (result.hasErrors()) {
            return "redirect:/prefet/registration/root";
        }


            compteService.saveRoot(compteDto, "/images/profile.jpeg");

        redirectAttributes.addFlashAttribute("success", "Votre enregistrement a été effectué avec succès");
        return "redirect:/";
    }

    @GetMapping("/home")
    public String home(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        request.getSession().setAttribute("compte", compte);
        return "redirect:/prefet/ecole/detail";
    }


    class User{
        private String name;
        private Long id;
        private String avatar;
        User(Long m_id,String m_name,String m_avatar){
            this.name=m_name;
            this.id=m_id;
            this.avatar= m_avatar;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
    @GetMapping("/ecole/detail")
    public String detail(Model model, HttpServletRequest request) {
        List<User> users = new ArrayList<User>();
        Collection<Compte> comptes = compteRepository.findAll();
        Compte c = (Compte) request.getSession().getAttribute("compte");
        for(Compte compte: comptes)
        {
            if(compte.getId()!=c.getId())
            users.add(new User(compte.getId(), compte.getUsername(),compte.getAvatar()));
        }

        System.out.println(users);
        request.getSession().setAttribute("teacher", new Enseignant());
        model.addAttribute("teacher", new Enseignant());
        model.addAttribute("message",new Message());
        model.addAttribute("users",users);
        return "prefet/home";
    }

    @GetMapping("/administration/lists")
    public String administrationLists(Model model){
        List<Compte> comptes = compteRepository.findAllByType(EType.ADMINISTRATION.toString());
        comptes.addAll(compteRepository.findAllByType(EType.SECRETAIRE.toString()));
        model.addAttribute("lists", comptes);
        return "prefet/administration/lists";
    }


    @GetMapping("/administration/form")
    public String administration(Model model){
        CompteRegistrationDto compte = new CompteRegistrationDto();
        model.addAttribute("compte", compte);
        return "prefet/administration/form";
    }


    @PostMapping("/administration/save")
    public String administrationSave(CompteRegistrationDto compte, RedirectAttributes redirectAttributes, @RequestParam("file") MultipartFile file){

        System.out.println("je suis la!!!");
        Role role = roleRepository.findByName(ERole.ROLE_DIRECTEUR.toString());
        multipart.store(file);
        Compte compte1 = compteRepository.findByUsername(compte.getUsername());
        Compte compte2 = compteRepository.findByEmail(compte.getEmail());

        if (compte1 != null | compte2!=null){
            redirectAttributes.addFlashAttribute("message", "There are existing account with the provided email or username");
            return "redirect:/prefet/administration/form";
        }else {
            Compte compte3 = new Compte();
            compte3.setUsername(compte.getUsername());
            compte3.setEmail(compte.getEmail());
            compte3.setPassword(passwordEncoder.encode("1234567890"));
            compte3.setEncode("1234567890");
            compte3.setType(compte.getType());
            compte3.setAvatar("/upload-dir/"+file.getOriginalFilename());
            if (role!=null){
                compte3.setRoles(Arrays.asList(role));
            }else {
                Role role1 = new Role();
                role1.setName(ERole.ROLE_DIRECTEUR.toString());
                roleRepository.save(role1);
                compte3.setRoles(Arrays.asList(role1));
            }
            compteRepository.save(compte3);
        }
        return "redirect:/prefet/home";
    }



    @GetMapping("/enseignant/lists/salles")
    public String enseignants( Model model){
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Enseignant> enseignants = new ArrayList<>();

        for (Salle salle : salles){
            for (Enseignant enseignant : enseignantRepository.findAllBySalles_Id(salle.getId())){
                if (!(enseignants.contains(enseignant))){
                    enseignants.add(enseignant);
                }else {
                    System.out.println("this enseignant is already present");
                }
            }

        }
        System.out.println(enseignants.size());

        model.addAttribute("lists",enseignants);
        model.addAttribute("classes", salles);

        return "prefet/enseignants";


    }


    //--- Enseignant management start ----///
    @PostMapping("/enseignant/save")
    public String teacherSave(Enseignant enseignant, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request, Long[] classes,String cour_enseigners) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());

        CompteRegistrationDto compte1 = new CompteRegistrationDto();
        Enseignant enseignant1 = enseignantRepository.findByEmail(enseignant.getEmail());
        Collection<Compte> comptes = compteRepository.findAll();
        Collection<Enseignant> enseignants = new ArrayList<>();

        for (Compte compte2 : comptes){
            if (compte2.getEnseignant()!= null){
                enseignants.add(compte2.getEnseignant());
            }
        }

        if (enseignants.contains(enseignant1)){
            System.out.println("je contient cette enseignant");
            model.addAttribute("error", "il existe un enseignant déjà enregistrer avec cet adresse email");
            model.addAttribute("message",new Message());
            return "prefet/home";
        }

        if (!(cour_enseigners.isEmpty())){
            String[]cours= cour_enseigners.split(",");
            System.out.println(cours.length);
            for (String item : cours){
                enseignant.getCour_enseigner().add(item.toUpperCase());
            }
        }
        System.out.println(enseignant.getCour_enseigner());
        compte1.setUsername(enseignant.getName() + "" + compteRepository.findAll().size());
        compte1.setEmail(enseignant.getEmail());
        compte1.setPassword(enseignant.getName() + "" + compteRepository.findAll().size());
        compte1.setConfirmPassword(enseignant.getName() + "" + compteRepository.findAll().size());
        enseignant.setAvatar("/images/profile.jpeg");
        compte1.setType(EType.ENSEIGNANT.toString());
        enseignantRepository.save(enseignant);
        System.out.println(classes);
        if (classes!=null){
            for (Long ids : classes){
                System.out.println(ids);
                Salle salle = salleRepository.getOne(ids);
                salle.getEnseignants().add(enseignant);
                salleRepository.save(salle);
            }
        }else {
            redirectAttributes.addFlashAttribute("error","Il n'y as pas de classe enregistrer vous devez commencer par créer des salles de classe dans votre école");
            return "redirect:/prefet/enseignant/lists";
        }
        compteService.saveEnseignant(compte1, "/images/profile.jpeg", enseignant);
        Mail sender = new Mail();
        sender.sender(
                enseignant.getEmail(),
                "Enregistrement d'un enseignant dans le lycee cirezi : ",
                "vos identifiants : username:" + compte1.getUsername() + " et password : " + compte1.getPassword());

        sender.sender(
                "confirmation@yesbanana.org",
                "Enregistrement d'un enseignant dans le lycee cirezi : ",
                "L'utilisateur " + compte1.getUsername() + " avec l'email :" +
                        compte1.getEmail() + "  Vient d'etre ajouter " +
                        "sur la plateforme de gestion écoles en ligne. Veuillez vous connecter pour manager son statut.");

        redirectAttributes.addFlashAttribute("success", "Vous avez enregistré avec succès ce nouvel enseignant : " + enseignant.getPrenom() + " " + enseignant.getName() + " " + enseignant.getPostnom());
        return "redirect:/prefet/enseignant/lists";
    }


    @GetMapping("/enseignant/lists")
    public String teacherLists(Model model) {

        Collection<Salle> salles = salleRepository.findAll();
        List<Enseignant> enseignants = enseignantRepository.findAll();

        model.addAttribute("classes",salles);
        model.addAttribute("teacher", new Enseignant());
        model.addAttribute("lists", enseignants);

        return "prefet/enseignants/lists";
    }

    @GetMapping("/bibliotheque/lists")
    public String lists(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        List<Livre> alls = new ArrayList<>();
        List<Livre> livres = livreRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        for (int i=0;i<livres.size();i++){
            if (!(i>12)){
                alls.add(livres.get(i));
            }
        }
        model.addAttribute("lists",alls);

        return "prefet/bibliotheques";
    }


    @GetMapping("/parent/lists")
    public String parentLists(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());

        Collection<Eleve> eleves = eleveRepository.findAll();
        Collection<Parent> parents = new ArrayList<>();

        for (Eleve eleve : eleves){
            if (!(parents.contains(eleve.getParent()))){
                parents.add(eleve.getParent());
            }
        }


        System.out.println(parents.size());

        model.addAttribute("lists1", parents);
        model.addAttribute("parents", parents);

        return "prefet/parent/lists";
    }

    @GetMapping("/parent/accounts/lists")
    public String parentListsAccount(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());

        Collection<Compte> accounts = new ArrayList<>();



        for (Compte compte1 : compteRepository.findAll()){
                if (compte1.getParent() != null){
                        accounts.add(compte1);
                }

        }

        System.out.println(accounts.size());


        model.addAttribute("lists1", accounts);

        return "prefet/parent/accounts";
    }

    @GetMapping("/eleve/lists")
    public String elevesLists(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());


        Collection<Eleve> eleves = eleveRepository.findAll();

        model.addAttribute("lists", eleves);

        return "prefet/eleve/lists";
    }


    @GetMapping("/enseignant/edit/{id}")
    public String enseignantEdit(@PathVariable Long id, Model model) {
        Enseignant enseignant = enseignantRepository.getOne(id);
        model.addAttribute("teacher", enseignant);
        return "prefet/enseignants/edit";
    }


    @PostMapping("/enseignant/update")
    public String enseignantUpdate(Enseignant enseignant, @RequestParam("file") MultipartFile file, String cour_enseigners) {

        if (!file.isEmpty()) {
            multipart.store(file);
            enseignant.setAvatar("/upload-dir/" + file.getOriginalFilename());
        }else {
            enseignant.setAvatar(enseignant.getAvatar());
        }
        if (!(cour_enseigners.isEmpty())){
            String[]cours= cour_enseigners.split(",");
            System.out.println(cours.length);
            enseignant.getCour_enseigner().clear();
            for (String item : cours){
                enseignant.getCour_enseigner().add(item.toUpperCase());
            }
        }else {
            enseignant.setCour_enseigner(enseignant.getCour_enseigner());
        }

        enseignantRepository.save(enseignant);

        return "redirect:/prefet/enseignant/lists";
    }



    @GetMapping("/eleve/edit/{id}")
    public String eleveEdit(@PathVariable Long id, Model model) {
        Eleve eleve = eleveRepository.getOne(id);
        Salle salle = salleRepository.getOne(eleve.getSalle().getId());
        model.addAttribute("student", eleve);
        model.addAttribute("classe",salle);
        return "prefet/eleve/edit";
    }


    @PostMapping("/eleve/update")
    public String eleveUpdate(Eleve eleve, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        eleve.setPays("Republique Democratique du Congo");
        eleveRepository.save(eleve);
        return "redirect:/prefet/eleve/lists";
    }

    @GetMapping("/enseignant/delete/{id}")
    public String deleteEnseignant(@PathVariable Long id){
        Salle salle = salleRepository.findByPrincipal(enseignantRepository.getOne(id).getName() + "  " + enseignantRepository.getOne(id).getPrenom());
        if (salle != null) {
            salle.setPrincipal("Non definis");
            salleRepository.save(salle);
        }
        Collection<Compte> comptes = compteRepository.findAllByEmail(enseignantRepository.getOne(id).getEmail());
        for (Compte compte : comptes){
            compteRepository.delete(compte);
        }
        enseignantRepository.deleteById(id);
        return "redirect:/prefet/enseignant/lists";
    }

    // ------ Enseignant management end -----///
    // ------ Classe management start -----///

    @GetMapping("/classe/lists")
    public String classe(Model model) {
        List<Enseignant> enseignants = enseignantRepository.findAll();

        model.addAttribute("lists", salleRepository.findAll());
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("salle", new Salle());
        return "prefet/classes/lists";
    }


    @PostMapping("/classe/save")
    public String classeSave(Salle salle, Long enseignantId, RedirectAttributes redirectAttributes, String suffix) {
        if (enseignantId !=null){
        Enseignant enseignant = enseignantRepository.getOne(enseignantId);
        salle.setEnseignants(Arrays.asList(enseignant));
            salle.setPrincipal(enseignant.getName() + "  " + enseignant.getPrenom());
            enseignant.getSallesIds().add(salle.getId());
            enseignantRepository.save(enseignant);
        }else {

            salle.setPrincipal("Non defini");
        }
        salle.setNiveau(salle.getNiveau().toString()+" "+ suffix.toUpperCase());
        salleRepository.save(salle);
        redirectAttributes.addFlashAttribute("success", "Vous avez ajouté avec succès une nouvelle classe");
        return "redirect:/prefet/classe/lists";
    }

    @GetMapping("/update/classe/form/{id}")
    public String updateClasse(@PathVariable Long id, Model model){
        Salle salle = salleRepository.getOne(id);
        Collection<Compte> comptes = compteRepository.findAll();
        List<Enseignant> teachers = new ArrayList<>();

        for (Compte compte1 : comptes) {
            if (compte1.getEnseignant() != null && !(comptes.contains(compte1.getEnseignant()))) {
                teachers.add(compte1.getEnseignant());
                System.out.println(compte1.getEnseignant().getId());
            }
        }
        model.addAttribute("classe",salle);
        model.addAttribute("enseignants",teachers);
        return "prefet/classes/update";
    }

    @GetMapping("/salle/detail/{id}")
    public String classeDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        Collection<Message> messages = messageRepository.findAllByVisibiliteAndSalle(EVisibilite.DIRECTION.toString(), (salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC, "id"));
        messages.addAll(messageRepository.findAllByVisibiliteAndSalle(EVisibilite.PUBLIC.toString(), (salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC, "id")));
        messages.addAll(messageRepository.findAllByVisibiliteAndSalle(EVisibilite.ENSEIGNANT.toString(), (salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC, "id")));
        messages.addAll(messageRepository.findAllByVisibiliteAndSalle(EVisibilite.PARENT.toString(), (salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC, "id")));
        Collection<Message> messages1 = messageRepository.findAllByCompte_Id(compte.getId());
        for (Message message : messages1) {
            if (!(messages.contains(message))) {
                messages.add(message);
            }
        }
        model.addAttribute("lists", messages);
        model.addAttribute("message", new Message());
        model.addAttribute("classe", salle);
        request.getSession().setAttribute("salle",salle);
        return "prefet/classes/detail";
    }

    @GetMapping("/classe/add/enseignant/{id}")
    public String addTeacherClasse(@PathVariable Long id, List<Long> ids, RedirectAttributes redirectAttributes) {

        System.out.println(id);
        Salle salle = salleRepository.getOne(id);
        for (Long number : ids) {
            Enseignant enseignant = enseignantRepository.getOne(number);
            if (salle.getEnseignants().contains(enseignant)) {
                System.out.println("Already contain enseignant with : " + number);
            } else {
                salle.getEnseignants().add(enseignant);
                enseignant.getSallesIds().add(salle.getId());
            }
            enseignantRepository.save(enseignant);
        }
        salleRepository.save(salle);
        redirectAttributes.addFlashAttribute("success", "Vous avez ajouté avec succès un nouvel enseignant a cette classe");
        return "redirect:/prefet/enseignant/classe/" + salle.getId();

    }


    @GetMapping("/enseignant/classe/{id}")
    public String classeTeachers(Model model, @PathVariable Long id) {

        Collection<Compte> comptes = compteRepository.findAll();
        List<Enseignant> teachers = new ArrayList<>();

        for (Compte compte1 : comptes) {
            if (compte1.getEnseignant() != null && !(comptes.contains(compte1.getEnseignant()))) {
                teachers.add(compte1.getEnseignant());
            }
        }
        Salle salle = salleRepository.getOne(id);

        Collection<Enseignant> enseignants = salle.getEnseignants();

        model.addAttribute("classe", salle);
        model.addAttribute("teachers", teachers);
        model.addAttribute("lists", enseignants);

        return "prefet/classes/enseignants";
    }


    //---- Classe management end ----//
    //---- Eleve management start ----//

    @GetMapping("/classe/eleves/{id}")
    public String allEleves(@PathVariable Long id, Model model) {

        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(id);
        model.addAttribute("classe", salleRepository.getOne(id));
        model.addAttribute("student", new Eleve());
        model.addAttribute("lists", eleves);
        return "prefet/classes/eleves";
    }

    @GetMapping("/create/{id}")
    public String createParent(@PathVariable Long id){
        Eleve eleve = eleveRepository.getOne(id);
        Parent parent = new Parent();
        parent.setEmail(eleve.getPrenom().toLowerCase()+"@yesbanana.org");
        parent.setNomComplet(eleve.getName()+" "+eleve.getPrenom());
        parentRepository.save(parent);
            CompteRegistrationDto compteRegistrationDto = new CompteRegistrationDto();
            compteRegistrationDto.setPassword("1234567890");
            compteRegistrationDto.setUsername(eleve.getName()+"_"+eleve.getPrenom());
            compteRegistrationDto.setEmail(parent.getEmail());

            compteService.saveParent(compteRegistrationDto,"/images/profile.jpeg",parent);
        eleve.setParent(parent);
        eleveRepository.save(eleve);
        return "redirect:/prefet/classe/eleves/"+eleve.getSalle().getId();
    }

    @PostMapping("/eleves/save/{id}")
    public String save(Eleve eleve, @PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request){


        Parent existParent = parentRepository.findByNomComplet(eleve.getNomCompletTuteur().toUpperCase());
        Salle salle = salleRepository.getOne(id);
        eleve.setSalle(salle);
        eleve.setPays("Republique Democratique du Congo");
        Mail sender = new Mail();
        CompteRegistrationDto compteRegistrationDto = new CompteRegistrationDto();

        if (existParent != null){
            eleve.setParent(existParent);
        }else {
            Parent parent = new Parent();
            parent.setNomComplet(eleve.getNomCompletTuteur().toUpperCase());
            parent.setEmail(eleve.getEmailTuteur());
            parent.setTelephone(eleve.getTelephoneTuteur().toUpperCase());
            parent.setWhatsapp(eleve.getWhatsappTuteur().toUpperCase());
            compteRegistrationDto.setEmail(parent.getEmail().toLowerCase());
            compteRegistrationDto.setUsername(parent.getNomComplet().toLowerCase());
            compteRegistrationDto.setPassword(compteRegistrationDto.getUsername());
            compteRegistrationDto.setConfirmPassword(compteRegistrationDto.getPassword());
            parentRepository.save(parent);
            compteService.saveParent(compteRegistrationDto, "/images/profile.jpeg", parent);
            eleve.setParent(parent);
            sender.sender(
                    compteRegistrationDto.getEmail(),
                    "Enregistrement d'un parent dans le lycee : ",
                    "L'utilisateur " + compteRegistrationDto.getUsername() + " avec mot de passe :" +
                            compteRegistrationDto.getPassword() + "  Vient d'être ajouter " +
                            "sur la plateforme de gestion écoles en ligne. Veuillez vous connecter pour manager son statut.");

        }
            eleveRepository.save(eleve);


            sender.sender(
                    "confirmation@yesbanana.org",
                    "Enregistrement d'un parent dans le lycee cirezi : ",
                    "L'utilisateur " + compteRegistrationDto.getUsername() + " avec email :" +
                            compteRegistrationDto.getEmail() + "  Vient d'être ajouter " +
                            "sur la plateforme de gestion écoles en ligne. Veuillez vous connecter pour manager son statut.");

        redirectAttributes.addFlashAttribute("success","Vous avez ajouté avec succès un nouvel élève dans cette classe");
        return "redirect:/prefet/classe/eleves/"+salle.getId();
    }

    @GetMapping("/eleve/delete/{id}/{salleId}")
    public String deleteEleve(@PathVariable Long id, @PathVariable Long salleId){
        eleveRepository.deleteById(id);
        return "redirect:/prefet/classe/eleve/"+salleId;
    }

    //---- Eleve management end -----//
    //---- Parent management start -----//

    @GetMapping("/classe/parents/{id}")
    public String allParents(@PathVariable Long id, Model model) {
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(id);
        Collection<Parent> parents = new ArrayList<>();

        for (Eleve eleve : eleves) {
            parents.add(eleve.getParent());
        }

        model.addAttribute("classe", salleRepository.getOne(id));

        model.addAttribute("lists", parents);
        return "prefet/classes/parents";
    }


    @GetMapping("/access-denied")
    public String access_denied() {
        return "prefet/access-denied";
    }

    @PostMapping("/message/save/{id}")
    public String saveMessage(Message message, @RequestParam("file") MultipartFile file, @PathVariable Long id, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        message.setCompte(compte);
        message.setSender(compte.getUsername());
        message.setSalle(salle.getNiveau() + "" + salle.getId());
        message.setDate(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        message.setVisibilite(message.getVisibilite().toString());
        multipart.store(file);
        message.setFichier("/upload-dir/"+file.getOriginalFilename());
        messageRepository.save(message);
        Collection<Compte> comptes = compteRepository.findAll();

        Mail sender = new Mail();
        sender.sender(
                compte.getEmail(),
                "Envoi d'un message",
                "Message de la direction ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

        for (Compte compte1: comptes){

                sender.sender(
                        compte1.getEmail(),
                        "Message de la direction",
                        message.getContent() + ", envoye le " + message.getDate() + ", fichier associe(s) " + message.getFichier()+"Vous pouvez consulter ce message dans votre espace membre dans l'école en ligne sur----> www.ecoles.yesbanana.org");


        }
        return "redirect:/prefet/salle/detail/" + salle.getId();

    }

    @GetMapping("/message/lists")
    public String messagesDirecteur(HttpServletRequest request, Model model){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Message> messages = messageRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        model.addAttribute("lists", messages);
        model.addAttribute("message", new Message());
        return "prefet/messages";
    }

    @GetMapping("/message/delete/{id}")
    public String deleteMessage(@PathVariable Long id){
        messageRepository.deleteById(id);
        return "redirect:/prefet/home";
    }

    @PostMapping("/message/save")
    public String saveMessageEcole(Message message, @RequestParam("file") MultipartFile file, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        message.setCompte(compte);
        message.setSender(compte.getUsername());
        message.setDate(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        message.setVisibilite(message.getVisibilite().toString());
        multipart.store(file);
        message.setFichier("/upload-dir/"+file.getOriginalFilename());
        Collection<Compte> comptes = compteRepository.findAll();

        Mail sender = new Mail();
        sender.sender(
                compte.getEmail(),
                "Envoi d'un message",
                "Message de la direction ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

        for (Compte compte1: comptes){

            sender.sender(
                    compte1.getEmail(),
                    "Message de la direction",
                    message.getContent() + ", envoye le " + message.getDate() + ", fichier associe(s) " + message.getFichier()+"Vous pouvez consulter ce message dans votre espace membre dans l'école en ligne sur ----> www.ecoles.yesbanana.org");

        }

        messageRepository.save(message);
        return "redirect:/prefet/message/lists";
    }


    @Autowired
    private HebdoRepository hebdoRepository;

    @Autowired
    private PlanningRepository planningRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @GetMapping("/classe/hebdos/{id}")
    public String presences(@PathVariable Long id, Model model){

        Salle salle = salleRepository.getOne(id);

                Collection<Hebdo> hebdos = hebdoRepository.findAllBySalles_Id(salle.getId(),Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("classe",salle);
        model.addAttribute("lists",hebdos);
        return "prefet/classes/hebdos";
    }

    public List<String> removeDuplicates(List<String> list)
    {
        if (list == null){
            return new ArrayList<>();
        }

        // Create a new ArrayList
        List<String> newList = new ArrayList<String>();
        // Traverse through the first list
        for (String element : list) {

            // If this element is not present in newList
            // then add it

            if (element !=null && !newList.contains(element) && !element.isEmpty()) {

                newList.add(element);
            }
        }
        // return the new list
        return newList;
    }

    /*@GetMapping("/hebdo/detail/{id}")
    public String detailHebdo(Model model, @PathVariable Long id){

        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());
        Collection<Presence> presences = presenceRepository.findAllByHebdo_Id(hebdo.getId());
        ArrayList<String> dates = new ArrayList<>();
        for (Presence presenceString : presences){
            dates.add(presenceString.getDate());
        }

        Salle salle = hebdo.getSalle();


        model.addAttribute("plannings",plannings);
        model.addAttribute("dates",removeDuplicates(dates));
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("classe",salle);
        return "prefet/classes/hebdo";
    }

    @GetMapping("/presence/detail/{id}")
    public String presenceNew(Model model, @PathVariable Long id, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(hebdo.getSalle().getId());


        model.addAttribute("lists",eleves);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("classe",hebdo.getSalle());
        return "prefet/classes/presence";

    }

    @GetMapping("/presence/eleve/detail/{eleveId}/{id}")
    public String presenceDetail(Model model, @PathVariable Long id,@PathVariable Long eleveId, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Hebdo hebdo = hebdoRepository.getOne(id);
        Eleve eleve = eleveRepository.getOne(eleveId);
        Collection<Presence> presences = presenceRepository.findAllByEleve_IdAndHebdo_Id(eleve.getId(),hebdo.getId());

        model.addAttribute("lists",presences);
        model.addAttribute("eleve",eleve);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("classe",hebdo.getSalle());
        return "prefet/classes/presenceDetail";

    }*/

    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        model.addAttribute("compteDto", new CompteRegistrationDto());
        return "prefet/account";
    }

    @PostMapping("/accounts/save")
    public String saveAccount(CompteRegistrationDto compteRegistrationDto, @RequestParam("file") MultipartFile file, String holdPassword, RedirectAttributes redirectAttributes, Long id, String avatar){
        Compte compte = compteRepository.getOne(id);
        if (!(file.isEmpty())) {
            multipart.store(file);
            compte.setAvatar("/upload-dir/" + file.getOriginalFilename());
        }else {
            compte.setAvatar(avatar);
        }
        if (compteRegistrationDto.getUsername().isEmpty()) {
            compte.setUsername(compte.getUsername());
        }else {
            compte.setUsername(compteRegistrationDto.getUsername());
        }

        if (compteRegistrationDto.getEmail().isEmpty()) {
            compte.setEmail(compte.getEmail());
        }else {
            compte.setEmail(compteRegistrationDto.getEmail());
        }


        if (!(holdPassword.isEmpty()) && !(compteRegistrationDto.getPassword().isEmpty())) {
            System.out.println(holdPassword);
            if (passwordEncoder.matches(holdPassword,compte.getPassword())) {

                compte.setPassword(passwordEncoder.encode(compteRegistrationDto.getPassword()));
            }else {
                redirectAttributes.addFlashAttribute("error","l'ancien mot de passe n'est pas valide veuillez trouver le bon");
                return "redirect:/prefet/account/detail/"+compte.getId();
            }
        }

        compte.setEncode(compteRegistrationDto.getPassword());

        compteRepository.save(compte);

        return "redirect:/prefet/account/detail/"+compte.getId();

    }


}
