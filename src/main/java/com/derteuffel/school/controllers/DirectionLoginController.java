package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.ERole;
import com.derteuffel.school.enums.EType;
import com.derteuffel.school.enums.EVisibilite;
import com.derteuffel.school.helpers.*;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Mail;
import com.derteuffel.school.services.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 23/03/2020.
 */
@Controller
@RequestMapping("/direction")
public class DirectionLoginController {

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
    private MatiereRepository matiereRepository;

    @Autowired
    private NoteRepository noteRepository;

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
        return "direction/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/direction/login";
    }
    @ModelAttribute("compte")
    public CompteRegistrationDto compteRegistrationDto() {
        return new CompteRegistrationDto();
    }


    @GetMapping("/home")
    public String home(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        request.getSession().setAttribute("compte", compte);
        return "redirect:/direction/ecole/detail";
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
        return "direction/home";
    }

    /*@GetMapping("/administration/lists")
    public String administrationLists(Model model){
        List<Compte> comptes = compteRepository.findAllByType(EType.ADMINISTRATION.toString());
        model.addAttribute("lists", comptes);
        return "direction/administration/lists";
    }


    @GetMapping("/administration/form")
    public String administration(Model model){
        CompteRegistrationDto compte = new CompteRegistrationDto();
        model.addAttribute("compte", compte);
        return "direction/administration/form";
    }*/


    @PostMapping("/administration/save")
    public String administrationSave(CompteRegistrationDto compte, RedirectAttributes redirectAttributes, @RequestParam("file") MultipartFile file){

        System.out.println("je suis la!!!");
        Role role = roleRepository.findByName(ERole.ROLE_DIRECTEUR.toString());
        multipart.store(file);
        Compte compte1 = compteRepository.findByUsername(compte.getUsername());
        Compte compte2 = compteRepository.findByEmail(compte.getEmail());

        if (compte1 != null | compte2!=null){
            redirectAttributes.addFlashAttribute("message", "There are existing account with the provided email or username");
            return "redirect:/direction/administration/form";
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
        return "redirect:/direction/home";
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

        return "direction/enseignants";


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
            return "direction/home";
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
            return "redirect:/direction/enseignant/lists";
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
        return "redirect:/direction/enseignant/lists";
    }


    @GetMapping("/enseignant/lists")
    public String teacherLists(Model model) {

        Collection<Salle> salles = salleRepository.findAll();
        List<Enseignant> enseignants = enseignantRepository.findAll();

        model.addAttribute("classes",salles);
        model.addAttribute("teacher", new Enseignant());
        model.addAttribute("lists", enseignants);

        return "direction/enseignants/lists";
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

        return "direction/bibliotheques";
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

        return "direction/parent/lists";
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

        return "direction/parent/accounts";
    }

    @GetMapping("/eleve/lists")
    public String elevesLists(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());


        Collection<Eleve> eleves = eleveRepository.findAll();

        model.addAttribute("lists", eleves);

        return "direction/eleve/lists";
    }


    @GetMapping("/enseignant/edit/{id}")
    public String enseignantEdit(@PathVariable Long id, Model model) {
        Enseignant enseignant = enseignantRepository.getOne(id);
        model.addAttribute("teacher", enseignant);
        return "direction/enseignants/edit";
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

        return "redirect:/direction/enseignant/lists";
    }



    @GetMapping("/eleve/edit/{id}")
    public String eleveEdit(@PathVariable Long id, Model model) {
        Eleve eleve = eleveRepository.getOne(id);
        Salle salle = salleRepository.getOne(eleve.getSalle().getId());
        model.addAttribute("student", eleve);
        model.addAttribute("classe",salle);
        return "direction/eleve/edit";
    }


    @PostMapping("/eleve/update")
    public String eleveUpdate(Eleve eleve, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();
        eleve.setPays("Republique Democratique du Congo");
        eleveRepository.save(eleve);
        return "redirect:/direction/eleve/lists";
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
        return "redirect:/direction/enseignant/lists";
    }

    // ------ Enseignant management end -----///
    // ------ Classe management start -----///

    @GetMapping("/classe/lists")
    public String classe(Model model) {
        List<Enseignant> enseignants = enseignantRepository.findAll();

        model.addAttribute("lists", salleRepository.findAll());
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("salle", new Salle());
        return "direction/classes/lists";
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
        return "direction/classes/detail";
    }

    @GetMapping("/classe/add/enseignant/{id}")
    public String addTeacherClasse(@PathVariable Long id, List<Long> ids, RedirectAttributes redirectAttributes) {

        System.out.println(id);
        Salle salle = salleRepository.getOne(id);
        for (Long number : ids){
            Enseignant enseignant = enseignantRepository.getOne(number);
            if (salle.getEnseignants().contains(enseignant)){
                System.out.println("Already contain enseignant with : "+number);
            }else {
                salle.getEnseignants().add(enseignant);
                enseignant.getSallesIds().add(salle.getId());
            }
            enseignantRepository.save(enseignant);
        }


        salleRepository.save(salle);
        redirectAttributes.addFlashAttribute("success", "Vous avez ajouté avec succès un nouvel enseignant a cette classe");
        return "redirect:/direction/enseignant/classe/" + salle.getId();

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

        return "direction/classes/enseignants";
    }


    //---- Classe management end ----//
    //---- Eleve management start ----//

    @GetMapping("/classe/eleves/{id}")
    public String allEleves(@PathVariable Long id, Model model) {

        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(id);
        model.addAttribute("classe", salleRepository.getOne(id));
        model.addAttribute("student", new Eleve());
        model.addAttribute("lists", eleves);
        return "direction/classes/eleves";
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
        return "redirect:/direction/classe/eleves/"+salle.getId();
    }

    @GetMapping("/eleve/delete/{id}/{salleId}")
    public String deleteEleve(@PathVariable Long id, @PathVariable Long salleId){
        eleveRepository.deleteById(id);
        return "redirect:/direction/classe/eleve/"+salleId;
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
        return "direction/classes/parents";
    }


    @GetMapping("/access-denied")
    public String access_denied() {
        return "direction/access-denied";
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
        return "redirect:/direction/salle/detail/" + salle.getId();

    }

    @GetMapping("/message/lists")
    public String messagesDirecteur(HttpServletRequest request, Model model){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Message> messages = messageRepository.findAllByVisibilite(EVisibilite.DIRECTION.toString(), Sort.by(Sort.Direction.DESC, "id"));
        messages.addAll(messageRepository.findAllByVisibilite(EVisibilite.PUBLIC.toString(), Sort.by(Sort.Direction.DESC, "id")));
        messages.addAll(messageRepository.findAllByVisibilite(EVisibilite.ENSEIGNANT.toString(), Sort.by(Sort.Direction.DESC, "id")));
        messages.addAll(messageRepository.findAllByVisibilite(EVisibilite.PARENT.toString()));
        Collection<Message> messages1 = messageRepository.findAllByCompte_Id(compte.getId());
        for (Message message : messages1) {
            if (!(messages.contains(message))) {
                messages.add(message);
            }
        }
        model.addAttribute("lists", messages);
        model.addAttribute("message", new Message());
        return "direction/messages";
    }

    @GetMapping("/message/delete/{id}")
    public String deleteMessage(@PathVariable Long id){
        messageRepository.deleteById(id);
        return "redirect:/direction/home";
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
        return "redirect:/direction/message/lists";
    }


    @Autowired
    private HebdoRepository hebdoRepository;

    @Autowired
    private PlanningRepository planningRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @GetMapping("/classe/hebdos/{id}")
    public String plannings(@PathVariable Long id, Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Salle salle = salleRepository.getOne(id);
        Collection<Hebdo> hebdos = hebdoRepository.findAllBySalles_Id(salle.getId(), Sort.by(Sort.Direction.DESC,"id"));
        Collection<Presence> presences = new ArrayList<>();
        model.addAttribute("lists",hebdos);
        model.addAttribute("salles",salles);
        model.addAttribute("classe",salle);
        model.addAttribute("hebdo",new Hebdo());
        return "direction/classes/hebdos";
    }
    @GetMapping("/programme/hebdos")
    public String plannings( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Collection<Hebdo> hebdos = hebdoRepository.findAll();
        model.addAttribute("lists",hebdos);
        model.addAttribute("salles",salles);
        model.addAttribute("hebdo",new Hebdo());
        return "direction/hebdos";
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

    @GetMapping("/hebdo/detail/{id}/{salleId}")
    public String detailHebdo(Model model, @PathVariable Long id, HttpServletRequest request, @PathVariable Long salleId){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        Collection<Salle> salles = new ArrayList<>();

        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());


        Salle salle = salleRepository.getOne(salleId);


        model.addAttribute("plannings",plannings);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("salles",salles);
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("classe",salle);
        model.addAttribute("planning",new PlanningHelpers());
        return "direction/classes/hebdo";
    }

    @GetMapping("/programme/hebdo/detail/{id}")
    public String detailHebdoPlanning(Model model, @PathVariable Long id, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        Collection<Salle> salles = salleRepository.findAll();

        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());



        model.addAttribute("plannings",plannings);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("enseignants",enseignants);
        model.addAttribute("planning",new PlanningHelpers());
        model.addAttribute("salles",salles);

        return "direction/hebdo";
    }

    @PostMapping("/hebdo/save/{id}")
    public String saveHebdo(Hebdo hebdo, RedirectAttributes redirectAttributes, HttpServletRequest request, @PathVariable Long id){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        hebdo.setCompte(compte);
        Salle salle = salleRepository.getOne(id);
        Collection<Salle> salles = salleRepository.findAll();
        hebdo.setSalles(salles);
        hebdoRepository.save(hebdo);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté une nouvelle semaine avec succès");
        return "redirect:/direction/hebdos/lists/"+ salle.getId();
    }

    @PostMapping("/programme/hebdo/save")
    public String saveHebdo(Hebdo hebdo, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        hebdo.setCompte(compte);
        Collection<Salle> salles = salleRepository.findAll();
        hebdo.setSalles(salles);
        hebdoRepository.save(hebdo);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté une nouvelle semaine avec succès");
        return "redirect:/direction/programme/hebdos";
    }

    @PostMapping("/planning/save/{id}/{salleId}")
    public String saveplanning(PlanningHelpers helpers, RedirectAttributes redirectAttributes, HttpServletRequest request,
                               @PathVariable Long id, String jour_semaine, Long enseignantId, @PathVariable Long salleId){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Hebdo hebdo = hebdoRepository.getOne(id);
        Salle salle = salleRepository.getOne(salleId);

        Planning planning = new Planning();
        Enseignant enseignant = enseignantRepository.getOne(enseignantId);
        Planning planning1 = planningRepository.findByCoursAndDate(helpers.getCours(),jour_semaine+" le "+sdf.format(helpers.getDate()));
        if (planning1!=null){
            redirectAttributes.addFlashAttribute("error","Vous ne pouvez pas ajouter cette leçon deux (02) fois pour le meme jour");
            return "redirect:/direction/hebdo/detail/"+ hebdo.getId();
        }
        planning.setHebdo(hebdo);
        planning.setDate(jour_semaine+" le "+sdf.format(helpers.getDate()));
        planning.setCours(helpers.getCours());
        planning.setEnseignant(enseignant);
        planning.setHeureDebut(helpers.getHeureDebut());
        planning.setHeureFin(helpers.getHeureFin());
        planningRepository.save(planning);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté une nouvelle journée avec succès");
        return "redirect:/direction/classes/planning/lists/"+ hebdo.getId()+"/"+salle.getId();
    }

    @PostMapping("/programme/planning/save/{id}")
    public String savePlanning(PlanningHelpers helpers, RedirectAttributes redirectAttributes, HttpServletRequest request,
                               @PathVariable Long id, String jour_semaine, Long enseignantId){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Hebdo hebdo = hebdoRepository.getOne(id);
        Planning planning = new Planning();
        Enseignant enseignant = enseignantRepository.getOne(enseignantId);
        Planning planning1 = planningRepository.findByCoursAndDate(helpers.getCours(),jour_semaine+" le "+sdf.format(helpers.getDate()));
        if (planning1!=null){
            redirectAttributes.addFlashAttribute("error","Vous ne pouvez pas ajouter cette leçon deux (02) fois pour le meme jour");
            return "redirect:/direction/programme/hebdo/detail/"+ hebdo.getId();
        }
        planning.setHebdo(hebdo);
        planning.setDate(jour_semaine+" le "+sdf.format(helpers.getDate()));
        planning.setCours(helpers.getCours());
        planning.setEnseignant(enseignant);
        planning.setHeureDebut(helpers.getHeureDebut());
        planning.setHeureFin(helpers.getHeureFin());
        planningRepository.save(planning);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté une nouvelle journée avec succès");
        return "redirect:/direction/programme/hebdo/detail/"+ hebdo.getId();
    }

    @GetMapping("/classes/planning/lists/{id}/{salleId}")
    public String planningHebdo(Model model, @PathVariable Long id, HttpServletRequest request, @PathVariable Long salleId){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        Collection<Salle> salles = new ArrayList<>();

        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());


        Salle salle = salleRepository.getOne(salleId);


        model.addAttribute("lists",plannings);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("salles",salles);
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("classe",salle);
        return "direction/classes/plannings";
    }


    @GetMapping("/classes/planning/update/{id}/{salleId}")
    public String planningHebdoUpdate(@PathVariable Long id, Model model, HttpServletRequest request, Long salleId){

        Planning planning = planningRepository.getOne(id);
        Hebdo hebdo = planning.getHebdo();
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        Collection<Salle> salles = new ArrayList<>();

        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }

        Salle salle = salleRepository.getOne(salleId);


        model.addAttribute("hebdo",hebdo);
        model.addAttribute("planning",planning);
        model.addAttribute("salles",salles);
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("classe",salle);
        return "direction/classes/planningsU";
    }


    @GetMapping("/programme/planning/update/{id}")
    public String planningHebdoUpdate(@PathVariable Long id, Model model, HttpServletRequest request){

        Planning planning = planningRepository.getOne(id);
        Hebdo hebdo = planning.getHebdo();
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        Collection<Salle> salles = new ArrayList<>();

        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }


        model.addAttribute("hebdo",hebdo);
        model.addAttribute("planning",planning);
        model.addAttribute("salles",salles);
        model.addAttribute("enseignants", enseignants);
        return "direction/planningsU";
    }

    @PostMapping("/classes/planning/update/{id}")
    public String updatePlanning(Planning planning, RedirectAttributes redirectAttributes, String jour_semaine,  @DateTimeFormat(pattern = "yyyy-MM-dd")Date date, Long enseignantId, @PathVariable Long id){

        Planning planning1 = planningRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null){
            planning1.setDate(jour_semaine+" le "+sdf.format(date));
        }

        if (enseignantId != null){
            Enseignant enseignant = enseignantRepository.getOne(enseignantId);
            planning1.setEnseignant(enseignant);
        }

        planning1.setHeureFin(planning.getHeureFin());
        planning1.setHeureDebut(planning.getHeureDebut());
        planning1.setCours(planning.getCours());
        planningRepository.save(planning1);

        redirectAttributes.addFlashAttribute("success","Planning modifier avec succes");
        return "redirect:/direction/classes/planning/lists/"+planning1.getHebdo().getId();

    }

    @PostMapping("/programme/planning/update/{id}")
    public String updatePlanningHome(Planning planning, RedirectAttributes redirectAttributes, String jour_semaine,  @DateTimeFormat(pattern = "yyyy-MM-dd")Date date, Long enseignantId, @PathVariable Long id, Long classe){

        Planning planning1 = planningRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null){
            planning1.setDate(jour_semaine+" le "+sdf.format(date));
        }

        if (enseignantId != null){
            Enseignant enseignant = enseignantRepository.getOne(enseignantId);
            planning1.setEnseignant(enseignant);
        }


        planning1.setHeureFin(planning.getHeureFin());
        planning1.setHeureDebut(planning.getHeureDebut());
        planning1.setCours(planning.getCours());
        planningRepository.save(planning1);

        redirectAttributes.addFlashAttribute("success","Planning modifier avec succes");
        return "redirect:/direction/programme/lists/"+planning1.getHebdo().getId();

    }


    @GetMapping("/programme/lists")
    public String getAllPlannings(Model model, HttpServletRequest request){
        Collection<Planning> plannings = planningRepository.findAll();
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        PlanningHelpers planningHelpers = new PlanningHelpers();
        Collection<Hebdo> hebdos = hebdoRepository.findAll();

        model.addAttribute("lists", plannings);
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("classes", salles);
        model.addAttribute("hebdos", hebdos);
        model.addAttribute("hebdo", new Hebdo());
        model.addAttribute("planningHelpers", planningHelpers);

        return "direction/plannings";

    }





    /*@GetMapping("/presence/add/{id}")
    public String presenceNew(Model model, @PathVariable Long id){

        Collection<Presence> presences = new ArrayList<Presence>();

        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(hebdo.getSalle().getId());
        Collection<Presence> existPresence = presenceRepository.findAllByDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));


        if (existPresence.size() > 0) {
            presences.addAll(existPresence);
        } else {
            for (Eleve eleve : eleves) {
                Presence presence = new Presence();

                presence.setHebdo(hebdo);
                presence.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                presence.setEleve(eleve);
                presenceRepository.save(presence);
                presences.add(presence);
            }
        }
        System.out.println(presences);

        PresenceForm presenceForm = new PresenceForm();
        presenceForm.setPresences(presences);
        model.addAttribute("lists",eleves);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("presenceForm", presenceForm);
        model.addAttribute("classe",hebdo.getSalle());
        return "direction/classes/presenceNew";

    }

    @GetMapping("/presence/activate/{id}")
    public String presenceSave( @PathVariable Long id){
        Presence presence = presenceRepository.getOne(id);
        if (presence.getStatus() != true){
            System.out.println("je suis ici");
            presence.setStatus(true);
        }else {
            presence.setStatus(false);
        }
        presenceRepository.save(presence);
        return "redirect:/direction/presence/add/"+presence.getHebdo().getId();
    }


    @GetMapping("/presence/detail/{id}")
    public String getPresence(@PathVariable Long id, Model model, HttpServletRequest request){
        Presence presence = presenceRepository.getOne(id);
        model.addAttribute("presence",presence);
        model.addAttribute("classe",presence.getHebdo().getSalle());
        model.addAttribute("hebdo",presence.getHebdo());
        return "direction/classes/presenceDetail";
    }*/

    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        model.addAttribute("compteDto", new CompteRegistrationDto());
        return "direction/account";
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
                return "redirect:/direction/account/detail/"+compte.getId();
            }
        }

        compte.setEncode(compteRegistrationDto.getPassword());

        compteRepository.save(compte);

        return "redirect:/direction/account/detail/"+compte.getId();

    }


    @GetMapping("/classe/matieres/lists/{id}")
    public String showMatieres(@PathVariable Long id, Model model, HttpServletRequest request){

        request.getSession().setAttribute("url",request.getHeader("referer"));
        Salle salle = salleRepository.getOne(id);
        List<Matiere> matieres = matiereRepository.findAllBySalle_Id(salle.getId());
        Collection<Enseignant> enseignants = salle.getEnseignants();
        System.out.println(enseignants.size());
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("matiere", new Matiere());
        model.addAttribute("classe",salle);
        model.addAttribute("lists",matieres);
        return "direction/classes/matieres";
    }


    @PostMapping("/classe/matiere/save/{id}")
    public String saveMatiere(Matiere matiere, RedirectAttributes redirectAttributes, @PathVariable Long id, Long enseignantId){
        Salle salle = salleRepository.getOne(id);
        Matiere matiere1 = matiereRepository.findByNameAndSalle_Id(matiere.getName(),salle.getId());
        Enseignant enseignant = enseignantRepository.getOne(enseignantId);
        if (matiere1 !=null){
            redirectAttributes.addFlashAttribute("message", "Cette matiere existe deja pour cette classe");
        }else {
            matiere.setSalle(salle);
            matiere.setEnseignant(enseignant);
            matiereRepository.save(matiere);
            redirectAttributes.addFlashAttribute("message","Vous avez ajouter une nouvelle matiere avec succes");
        }

        return "redirect:/direction/classe/matieres/lists/"+salle.getId();


    }

    @GetMapping("/classes/matieres/update/{id}")
    public String updateMatieres(@PathVariable Long id, Model model){
        Matiere matiere = matiereRepository.getOne(id);
        Salle salle = matiere.getSalle();
        Collection<Enseignant> enseignants = salle.getEnseignants();

        model.addAttribute("enseignants", enseignants);
        model.addAttribute("classe",salle);
        model.addAttribute("matiere",matiere);
        return "direction/classes/matiereU";
    }

    @PostMapping("/classe/matiere/update/{id}")
    public String saveUpdateMatiere(Matiere matiere, RedirectAttributes redirectAttributes, Long enseignantId, @PathVariable Long id){
        if (enseignantId != null){
            Enseignant enseignant = enseignantRepository.getOne(enseignantId);
            matiere.setEnseignant(enseignant);
        }

        matiereRepository.save(matiere);
        redirectAttributes.addFlashAttribute("message", "Votre matiere a ete modifier avec succes");
        return "redirect:/direction/classe/matieres/lists/"+id;
    }

    @GetMapping("/matiere/delete/{id}")
    public String deleteMatiere(@PathVariable Long id, RedirectAttributes redirectAttributes){
        Matiere matiere = matiereRepository.getOne(id);
        matiereRepository.delete(matiere);
        redirectAttributes.addFlashAttribute("message","Vous avez supprimer une matiere");
        return "redirect:/backside";
    }


    @GetMapping("/classes/matieres/detail/{id}/{salleId}")
    public String getNotes(@PathVariable Long id, @PathVariable Long salleId, Model model){
        Matiere matiere = matiereRepository.getOne(id);
        Salle salle = salleRepository.getOne(salleId);
        List<Note> notes = noteRepository.findAllByMatiere_Id(matiere.getId());
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(salleId);
        System.out.println(eleves.size());


        model.addAttribute("matiere", matiere);
        model.addAttribute("classe",salle);
        model.addAttribute("lists",eleves);
        return "direction/classes/notes";
    }

    @GetMapping("/classes/eleve/note/detail/{id}/{matiereId}")
    public String noteDetail(@PathVariable Long id, @PathVariable Long matiereId, Model model){
        Eleve eleve = eleveRepository.getOne(id);
        Matiere matiere = matiereRepository.getOne(matiereId);
        List<Note> notes = noteRepository.findAllByMatiere_IdAndEleve_Id(matiere.getId(),eleve.getId());
        System.out.println(notes.size());
        model.addAttribute("matiere",matiere);
        model.addAttribute("eleve", eleve);
        model.addAttribute("classe",matiere.getSalle());
        model.addAttribute("lists", notes);
        return "direction/classes/note";
    }

    @GetMapping("/classes/eleve/note/update/{id}/{matiereId}/{noteId}")
    public String noteUpdate(@PathVariable Long id, @PathVariable Long matiereId, Model model, @PathVariable Long noteId){
        Eleve eleve = eleveRepository.getOne(id);
        Matiere matiere = matiereRepository.getOne(matiereId);
        Note note = noteRepository.getOne(noteId);
        model.addAttribute("matiere",matiere);
        model.addAttribute("classe",matiere.getSalle());
        model.addAttribute("note",note);

        model.addAttribute("eleve", eleve);
        return "direction/classes/noteU";
    }


    @PostMapping("/classes/note/update/{id}/{eleveId}")
    public String noteUpdateSave(Note note, @PathVariable Long id, @PathVariable Long eleveId, RedirectAttributes redirectAttributes){
        Matiere matiere = matiereRepository.getOne(id);
        Eleve eleve = eleveRepository.getOne(eleveId);
        Note note1 = noteRepository.getOne(note.getId());
        note1.setPeriode(note.getPeriode());
        note1.setNote(note.getNote());
        noteRepository.save(note1);
        redirectAttributes.addFlashAttribute("message","Vous avez modifier avec succes votre note");
        return "redirect:/direction/classes/eleve/note/detail/"+eleve.getId()+"/"+matiere.getId();

    }

    @GetMapping("/classes/note/form/{id}/{salleId}")
    public String notesForm(Model model, @PathVariable Long id, @PathVariable Long salleId){
        Matiere matiere = matiereRepository.getOne(id);
        Salle salle = salleRepository.getOne(salleId);
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(salle.getId());
        List<NoteHelper> noteHelpers = new ArrayList<>(eleves.size());
        for (Eleve eleve : eleves){
            NoteHelper noteHelper = new NoteHelper();
            noteHelper.setEleveId(eleve.getId());
            noteHelper.setNomEleve(eleve.getName()+" "+eleve.getPrenom()+" "+eleve.getPostnom());
            noteHelpers.add(noteHelper);
        }
        SaveNoteHelper saveNoteHelper = new SaveNoteHelper();
        saveNoteHelper.setNoteHelpers(noteHelpers);
        System.out.println(eleves.size());
        model.addAttribute("saveNoteHelper", saveNoteHelper);
        model.addAttribute("matiere",matiere);
        model.addAttribute("classe",salle);

        return "direction/classes/noteForm";
    }


    @PostMapping("/classes/note/save/{id}/{salleId}")
    public String saveNote(@ModelAttribute("saveNoteHelper") SaveNoteHelper saveNoteHelper, @PathVariable Long id, @PathVariable Long salleId){
        Matiere matiere = matiereRepository.getOne(id);
        Salle salle = salleRepository.getOne(salleId);
        if (saveNoteHelper.getNoteHelpers()!= null && saveNoteHelper.getNoteHelpers().size() != 0){
            for (NoteHelper noteHelper : saveNoteHelper.getNoteHelpers()){
                System.out.println(noteHelper.getEleveId()+"  "+noteHelper.getNomEleve()+"  "+noteHelper.getNote());
                System.out.println("-----------------------------------------------");
                Note note = new Note();
                note.setNote(noteHelper.getNote());
                note.setEleve(eleveRepository.getOne(noteHelper.getEleveId()));
                note.setMatiere(matiere);
                note.setPeriode(saveNoteHelper.getPeriode());
                noteRepository.save(note);
            }
        }
        return "redirect:/direction/classes/matieres/detail/"+matiere.getId()+"/"+salle.getId();
    }


}
