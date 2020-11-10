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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 23/03/2020.
 */
@Controller
@RequestMapping("/percepteur")
public class PercepteurLoginController {

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
    private PaiementRepository paiementRepository;

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
        return "percepteur/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/percepteur/login";
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
        return "redirect:/percepteur/classe/lists";
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

        return "percepteur/enseignants";


    }


    @GetMapping("/enseignant/lists")
    public String teacherLists(Model model) {

        Collection<Salle> salles = salleRepository.findAll();
        List<Enseignant> enseignants = enseignantRepository.findAll();

        model.addAttribute("classes",salles);
        model.addAttribute("teacher", new Enseignant());
        model.addAttribute("lists", enseignants);

        return "percepteur/enseignants/lists";
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

        return "percepteur/bibliotheques";
    }


    @GetMapping("/eleve/lists")
    public String elevesLists(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());


        Collection<Eleve> eleves = eleveRepository.findAll();

        model.addAttribute("lists", eleves);

        return "percepteur/eleve/lists";
    }



    // ------ Enseignant management end -----///
    // ------ Classe management start -----///

    @GetMapping("/classe/lists")
    public String classe(Model model, HttpServletRequest request) {
        List<Enseignant> enseignants = enseignantRepository.findAll();
        ArrayList<Double> montants = new ArrayList<>();
        for (int i=0; i<= 20; i++){
            montants.add(i*0.5*100.0);
        }
        model.addAttribute("montants",montants);
        request.getSession().setAttribute("montants", montants);
        model.addAttribute("lists", salleRepository.findAll());
        model.addAttribute("enseignants", enseignants);
        return "percepteur/classes/lists";
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
        return "percepteur/classes/detail";
    }

    @GetMapping("/classe/eleves/payment/lists/{id}")
    public String paymentLists(@PathVariable Long id, Model model){
        Salle salle = salleRepository.getOne(id);
        ArrayList<Double> montants = new ArrayList<>();
        for (int i=0; i<= 20; i++){
            montants.add(i*0.5*100.0);
        }

        System.out.println(montants);
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(salle.getId());
        Collection<Paiement> paiements = new ArrayList<>();
        for (Eleve eleve : eleves){
            System.out.println(eleve.getName());
            paiements.add(paiementRepository.findByEleve_Id(eleve.getId()));
        }
        model.addAttribute("classe", salle);
        model.addAttribute("lists", paiements);
        model.addAttribute("montants",montants);
        return "percepteur/classes/paiements";
    }


    @GetMapping("/add/montant")
    public String addMontant(Double montant, String niveau, String categorie){
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Eleve> eleves = new ArrayList<>();
        for (Salle salle : salles){
            System.out.println("je suis la!");
            if (salle.getNiveau().contains(niveau)){
                System.out.println("je suis ici dedans");
                for (Eleve eleve : eleveRepository.findAllByCategorieAndSalle_Id(categorie, salle.getId())){
                    System.out.println(eleveRepository.findAllByCategorieAndSalle_Id(categorie, salle.getId()).size());
                    Paiement paiement = paiementRepository.findByEleve_Id(eleve.getId());
                    paiement.setCoutTotal(montant);
                    paiementRepository.save(paiement);
                    System.out.println("et la cest la fin ");
                }
            }
        }
        return "redirect:/percepteur/home";
    }

    @GetMapping("/classe/eleves/add/account/{id}")
    public String addPaiement(String motif, @PathVariable Long id, Double montant){
        Paiement paiement = paiementRepository.getOne(id);
        if (motif.equals("premier")){
            paiement.setAccountTrimestrePremier(montant);
        }else if (motif.equals("deuxieme")){
            paiement.setAccountTrimestreSecond(montant);
        }else if (motif.equals("troisieme")){
            paiement.setAccountTrimestreTroisieme(montant);
        }else if (motif.equals("sport")){
            paiement.setAccountSport(montant);
        }else {
            paiement.setAccountBibliotheque(montant);
        }
        if (paiement.getTotalPayer() != null) {
            paiement.setTotalPayer(paiement.getTotalPayer() + montant);
            paiement.setSolde(paiement.getCoutTotal() - paiement.getTotalPayer());
        }else {
            paiement.setTotalPayer(montant);
            paiement.setSolde(paiement.getCoutTotal()-montant);
        }
        paiementRepository.save(paiement);
        return "redirect:/percepteur/classe/eleves/payment/lists/"+paiement.getEleve().getSalle().getId();
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

        return "percepteur/classes/enseignants";
    }


    //---- Classe management end ----//
    //---- Eleve management start ----//

    @GetMapping("/classe/eleves/{id}")
    public String allEleves(@PathVariable Long id, Model model) {

        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(id);
        model.addAttribute("classe", salleRepository.getOne(id));
        model.addAttribute("student", new Eleve());
        model.addAttribute("lists", eleves);
        return "percepteur/classes/eleves";
    }





    @GetMapping("/access-denied")
    public String access_denied() {
        return "percepteur/access-denied";
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
        return "redirect:/percepteur/salle/detail/" + salle.getId();

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
        return "percepteur/messages";
    }

    @GetMapping("/message/delete/{id}")
    public String deleteMessage(@PathVariable Long id){
        messageRepository.deleteById(id);
        return "redirect:/percepteur/home";
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
        return "redirect:/percepteur/message/lists";
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
        return "percepteur/classes/hebdos";
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


    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        model.addAttribute("compteDto", new CompteRegistrationDto());
        return "percepteur/account";
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
                return "redirect:/percepteur/account/detail/"+compte.getId();
            }
        }

        compte.setEncode(compteRegistrationDto.getPassword());

        compteRepository.save(compte);

        return "redirect:/percepteur/account/detail/"+compte.getId();

    }


}
