package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.ECours;
import com.derteuffel.school.enums.ENiveau;
import com.derteuffel.school.enums.ERole;
import com.derteuffel.school.enums.EVisibilite;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.helpers.EleveEncadreurHelper;
import com.derteuffel.school.helpers.EncadrementRegistrationDto;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Mail;
import com.derteuffel.school.services.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 02/04/2020.
 */

@Controller
@RequestMapping("/encadrements")
public class EncadrementController {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private EncadreurRepository encadreurRepository;

    @Autowired
    private EnfantRepository enfantRepository;
    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ParentRepository parentRepository;


    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private CompteService compteService;
    /*@Value("${file.upload-dir}")
    private  String fileStorage ; *///=System.getProperty("user.dir")+"/src/main/resources/static/downloadFile/";

    @Autowired
    private Multipart multipart;
    @GetMapping("/login")
    public String login(){
        return "encadrements/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/encadrements/login";
    }
    @ModelAttribute("compte")
    public EncadrementRegistrationDto encadrementRegistrationDto(){
        return new EncadrementRegistrationDto();
    }


    @GetMapping("/registration2")
    public String registrationForm(Model model){
        ArrayList<Integer> amounts = new ArrayList<>();
        for (int i = 10;i<500;i+=10){
            amounts.add(i);
        }
        model.addAttribute("amounts",amounts);
        return "encadrements/registration2";
    }

    @GetMapping("/registration1")
    public String registration1(Model model){
        return "encadrements/registration1";
    }
    @GetMapping("/registration")
    public String registration(Model model){
        return "encadrements/registration";
    }

    @PostMapping("/registration2")
    public String registrationDirectionSave(@ModelAttribute("compte") @Valid EncadrementRegistrationDto encadrementRegistrationDto,
                                            BindingResult result, RedirectAttributes redirectAttributes,  @RequestParam("file") MultipartFile file, @RequestParam("picture") MultipartFile picture , String cour_enseigners){

        Compte existAccount = compteService.findByUsername(encadrementRegistrationDto.getUsername());
        if (existAccount != null){
            result.rejectValue("username", null, "Il existe déjà un compte avec ce nom d'utilisateur veuillez choisir un autre");
            redirectAttributes.addFlashAttribute("error","Il existe déjà un compte avec ce nom d'utilisateur veuillez choisir un autre");
        }

        if (result.hasErrors()) {
            return "redirect:/encadrements/registration2";
        }

            Encadreur encadreur = new Encadreur();
            encadreur.setTelephone(encadrementRegistrationDto.getTelephone());
            encadreur.setName(encadrementRegistrationDto.getName());
            encadreur.setEmail(encadrementRegistrationDto.getEmail());
            if (!(cour_enseigners.isEmpty())){
            String[]cours= cour_enseigners.split(",");
            System.out.println(cours.length);
                for (String item : cours){
                encadreur.getCour_enseigner().add(item.toUpperCase());
                }
            }
            encadreur.setHeureDebut(encadrementRegistrationDto.getHeureDebut());
            encadreur.setHeureFin(encadrementRegistrationDto.getHeureFin());
            encadreur.setMotivation(encadrementRegistrationDto.getMotivation());
            encadreur.setNbreJourParSemaine(encadrementRegistrationDto.getNbreJourParSemanie());
            encadreur.setNbreMois(encadrementRegistrationDto.getNbreMois());
            encadreur.setSalaire(encadrementRegistrationDto.getSalaire()+" $");
            encadreur.setLocalisation(encadrementRegistrationDto.getLocalisation());
            encadreur.setPays(encadrementRegistrationDto.getPays());
            encadreur.setDescription(encadrementRegistrationDto.getDescription());
            encadreur.setTypes(encadrementRegistrationDto.getTypes());
            encadreur.setState(false);
            multipart.store(file);
            encadreur.setCv("/upload-dir/"+file.getOriginalFilename());
            if (!(picture.isEmpty())) {
                multipart.store(picture);
                encadreur.setAvatar("/upload-dir/" + picture.getOriginalFilename());
            }else {
                encadreur.setAvatar("/images/profile.jpeg");
            }


            encadreurRepository.save(encadreur);
            compteService.saveEncadreur(encadrementRegistrationDto,encadreur.getAvatar(),encadreur);

        Mail sender = new Mail();
        sender.sender(
                encadrementRegistrationDto.getEmail(),
                "Enregistrement Espace encadrement",
                "Vous venez de vous enregistrer dans l'espace d'encadrement en tant que Encadreur/Expert YesB, contacter l'équipe de YesB");

        redirectAttributes.addFlashAttribute("success", "Votre enregistrement a été effectué avec succès");
        return "redirect:/encadrements/login";
    }

    @GetMapping("/encadreurs")
    public String encadreurs(Model model){
        Collection<Encadreur> encadreurs = encadreurRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("lists",encadreurs);
        return "encadrements/enseignants";
    }
    @GetMapping("/encadreurs/lists")
    public String encadreur(Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enfant enfant = compte.getEnfant();
        Collection<Encadreur> encadreurs = encadreurRepository.findAllByEnfants_Id(enfant.getId(),Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("lists",encadreurs);
        return "encadrements/enseignants";
    }


    @GetMapping("/encadreur/delete/{id}")
    public String deleteEncadreur(@PathVariable Long id){
        Compte compte = compteRepository.findByEnseignant_Id(id);
        System.out.println(compte.getUsername());
        compteRepository.deleteById(compte.getId());
        System.out.println("deleted");
        encadreurRepository.deleteById(id);
        return "redirect:/encadrements/encadreurs";
    }

    @GetMapping("/eleves")
    public String eleves(Model model){
        Collection<Enfant> enfants = enfantRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("lists",enfants);
        return "encadrements/enfants";
    }

    @GetMapping("/eleves/lists")
    public String elevesEncadrer(Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());

        Encadreur encadreur = encadreurRepository.getOne(compte.getEnseignant().getId());
        Collection<Enfant> enfants = encadreur.getEnfants();
        model.addAttribute("lists",enfants);
        return "encadrements/enfants";
    }

    @GetMapping("/enseignant/eleves/{id}")
    public String encadreurEleves(Model model, @PathVariable Long id){
        Encadreur encadreur = encadreurRepository.getOne(id);
        Collection<Enfant> enfantCollection = enfantRepository.findAll();
        Collection<Enfant> eleves = new ArrayList<>();
        for (Enfant enfant : enfantCollection){
            /*if (enfant.getMatieres().contains(encadreur.getCour_enseigners())){
                eleves.add(enfant);
            }*/
        }
        Collection<Enfant> enfants = encadreur.getEnfants();
        System.out.println(enfants.size());
        model.addAttribute("eleves",eleves);
        model.addAttribute("helper",new EleveEncadreurHelper());
        model.addAttribute("encadreur",encadreur);
        model.addAttribute("lists",enfants);
        return "encadrements/enseignant/enfants";
    }

    @GetMapping("/eleves/enseignant/{id}")
    public String elevesEncadreur(Model model, @PathVariable Long id){
        Enfant enfant =enfantRepository.getOne(id);
        Collection<Encadreur> encadreurs = encadreurRepository.findAllByEnfants_Id(enfant.getId(),Sort.by(Sort.Direction.DESC,"id"));
        System.out.println(encadreurs.size());
        model.addAttribute("enfant",enfant);
        model.addAttribute("lists",encadreurs);
        return "encadrements/enfant/enseignants";
    }


    @PostMapping("/enseignant/eleves/save/{id}")
    public String EncadreurAddEleve(EleveEncadreurHelper eleveEncadreurHelper, @PathVariable Long id){

        System.out.println(eleveEncadreurHelper.getStudents());
        Encadreur encadreur = encadreurRepository.getOne(id);
        for (Long student : eleveEncadreurHelper.getStudents()){
            Enfant enfant = enfantRepository.getOne(student);
            if (!(encadreur.getEnfants().contains(enfant))) {
                encadreur.getEnfants().add(enfant);
            }
            System.out.println(encadreur.getEnfants().size());
            encadreurRepository.save(encadreur);
        }

        return "redirect:/encadrements/enseignant/eleves/"+encadreur.getId();
    }

    @GetMapping("/eleve/delete/{id}")
    public String deleteEnfants(@PathVariable Long id){
        Compte compte =compteRepository.findByEnfant_Id(id);
        compteRepository.deleteById(compte.getId());
        enfantRepository.deleteById(id);
        return "redirect:/encadrements/eleves";
    }

    @PostMapping("/registration1")
    public String registrationSave(@ModelAttribute("compte") @Valid EncadrementRegistrationDto encadrementRegistrationDto,
                                            BindingResult result, RedirectAttributes redirectAttributes ){

        Compte existAccount = compteService.findByUsername(encadrementRegistrationDto.getUsername());
        if (existAccount != null){
            result.rejectValue("username", null, "Il existe déjà un compte avec ce nom d'utilisateur veuillez choisir un autre");
            redirectAttributes.addFlashAttribute("error","Il existe déjà un compte avec ce nom d'utilisateur veuillez choisir un autre");
        }

        if (result.hasErrors()) {
            return "redirect:/encadrements/registration1";
        }



            Enfant enfant = new Enfant();
            enfant.setMatieres(new ArrayList<>(Arrays.asList(encadrementRegistrationDto.getCours_reference())));
            enfant.setName(encadrementRegistrationDto.getName());
            enfant.setEmail(encadrementRegistrationDto.getEmail());
            enfant.setModePaiement(encadrementRegistrationDto.getModePaiement());
            enfant.setMotivation(encadrementRegistrationDto.getMotivation());
            enfant.setNiveau(encadrementRegistrationDto.getNiveau());
            enfant.setLocalisation(encadrementRegistrationDto.getLocalisation());
            enfant.setAge(encadrementRegistrationDto.getAge());
            enfant.setHeureDebut(encadrementRegistrationDto.getHeureDebut());
            enfant.setHeureFin(encadrementRegistrationDto.getHeureFin());
            enfant.setNbreJourParSemaine(encadrementRegistrationDto.getNbreJourParSemanie());
            enfant.setNbreMois(encadrementRegistrationDto.getNbreMois());
            enfant.setPays(encadrementRegistrationDto.getPays());
            enfant.setState(false);
            enfantRepository.save(enfant);
            compteService.saveEnfant(encadrementRegistrationDto,"/images/profile.jpeg",enfant);
        Mail sender = new Mail();
        sender.sender(
                encadrementRegistrationDto.getEmail(),
                "Enregistrement Espace encadrement",
                "Vous venez de vous enregistrer dans l'espace d'encadrement en tant que Etudiant/Elève, contacter l'équipe de YesB");

        redirectAttributes.addFlashAttribute("success", "Votre enregistrement a été effectué avec succès, bien vouloir contacter l'équipe Yesb via l'adresse info@yesbanana.org pour finaliser votre inscription et entrer en possession de votre code d'activation de votre compte");
        return "redirect:/encadrements/login";
    }

    @GetMapping("/encadreurs/update/{id}")
    public String updateForm(@PathVariable Long id, Model model){
        Encadreur encadreur = encadreurRepository.getOne(id);
        ArrayList<Integer> amounts = new ArrayList<>();
        for (int i = 10;i<500;i+=10){
            amounts.add(i);
        }
        model.addAttribute("amounts",amounts);
        model.addAttribute("encadreur",encadreur);
        return "encadrements/updateEncadreur";
    }


    @GetMapping("/changes/categories/{id}")
    public String changesCategory(@PathVariable Long id, String category){
        Encadreur encadreur = encadreurRepository.getOne(id);
        encadreur.setCategory(category);
        encadreurRepository.save(encadreur);
        return "redirect:/encadrements/encadreurs";
    }

    @PostMapping("/encadreurs/update")
    public String updateEnseignantSave(@Valid Encadreur encadreur, @RequestParam("file") MultipartFile file,@RequestParam("image") MultipartFile image, RedirectAttributes redirectAttributes, String cour_enseigners){
        if (!(file.isEmpty())){
        multipart.store(file);
        encadreur.setCv("/upload-dir/"+file.getOriginalFilename());
        }else {
            encadreur.setCv(encadreur.getCv());
        }

        if (!(image.isEmpty())){
            multipart.store(image);
            encadreur.setAvatar("/upload-dir/"+image.getOriginalFilename());
        }else {
            encadreur.setAvatar(encadreur.getAvatar());
        }

        if (!(cour_enseigners.isEmpty())){
            String[]cours= cour_enseigners.split(",");
            System.out.println(cours.length);
            encadreur.getCour_enseigner().clear();
            for (String item : cours){
                encadreur.getCour_enseigner().add(item.toUpperCase());
            }
        }else {
            encadreur.setCour_enseigner(encadreur.getCour_enseigner());
        }

        encadreur.setCategory(encadreur.getCategory().toString());

        encadreurRepository.save(encadreur);

        redirectAttributes.addFlashAttribute("success","Votre modification a été faite avec succès");
        return "redirect:/encadrements/encadreurs";

    }


    @GetMapping("/enfants/update/{id}")
    public String updateFormEnfant(@PathVariable Long id, Model model){
        Enfant enfant = enfantRepository.getOne(id);

        model.addAttribute("enfant",enfant);
        return "encadrements/updateEnfant";
    }

    @PostMapping("/enfants/update")
    public String updateEnfatnSave(@Valid Enfant enfant, RedirectAttributes redirectAttributes){
        enfantRepository.save(enfant);
        redirectAttributes.addFlashAttribute("success","Votre modification a été faite avec succès");
        return "redirect:/encadrements/eleves";

    }

    @GetMapping("/activation/form")
    public  String activationPage(HttpServletRequest request, Model model){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());

        Role role = roleRepository.findByName(ERole.ROLE_ENCADREUR.name());
        Role role1 = roleRepository.findByName(ERole.ROLE_ENFANT.name());
        if (compte.getStatus() == true){
            if (compte.getRoles().contains(role)) {
                Encadreur encadreur = encadreurRepository.getOne(compte.getEnseignant().getId());
                if (encadreur.getCode() == null){
                    encadreur.setCode(compte.getCode());
                    encadreurRepository.save(encadreur);
                }
                System.out.println("je suis encadreur");
            } else if (compte.getRoles().contains(role1)) {
                Enfant enfant = enfantRepository.getOne(compte.getEnfant().getId());
                if (enfant.getCode() == null){
                    enfant.setCode(compte.getCode());
                    enfantRepository.save(enfant);
                }
            }
            return "redirect:/encadrements/cours/lists";
        }else {
            if (compte.getRoles().contains(role)) {
                System.out.println("je suis encadreur");
                if (compte.getCode() == null) {

                    String randomCode = "" + UUID.randomUUID().toString();
                    compte.setCode(randomCode);
                    compte.setStatus(false);

                    compteRepository.save(compte);
                    Encadreur encadreur = encadreurRepository.getOne(compte.getEnseignant().getId());
                    encadreur.setCode(compte.getCode());
                    encadreurRepository.save(encadreur);
                }
                return "redirect:/encadrements/cours/lists";
            } else if (compte.getRoles().contains(role1)) {

            if (compte.getCode() == null) {

                    String randomCode = "" + UUID.randomUUID().toString();
                    compte.setCode(randomCode);
                    compte.setStatus(false);

                    compteRepository.save(compte);
                    Enfant enfant = enfantRepository.getOne(compte.getEnfant().getId());
                    enfant.setCode(randomCode);
                    enfantRepository.save(enfant);
                }
            }else {
                return "redirect:/encadrements/cours/lists";
            }

            model.addAttribute("success", "Bien vouloir contacter l'équipe de Yesb pour avoir votre code d'activation");
            if (compte.getRoles().contains(role)){
                return "encadrements/activation1";
            }else {
                return "encadrements/activation";
            }

        }
    }

    @GetMapping("/activation/code")
     public String activation(String activation, HttpServletRequest request, RedirectAttributes redirectAttributes){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Role role = roleRepository.findByName(ERole.ROLE_ENCADREUR.name());
        if (!(compte.getCode().equals(activation))){
            if (compte.getRoles().contains(role)){
                Encadreur encadreur = encadreurRepository.getOne(compte.getEnseignant().getId());
                encadreur.setState(true);
                encadreurRepository.save(encadreur);
            }else {
                Enfant enfant = compte.getEnfant();
                enfant.setState(true);
                enfantRepository.save(enfant);
            }
            redirectAttributes.addFlashAttribute("success","Code d'activation incorrect");
            return "redirect:/encadrements/logout";
        }else {
            compte.setStatus(true);
            compteRepository.save(compte);
                redirectAttributes.addFlashAttribute("success","Code d'activation correct, profitez de nos services");
            return "redirect:/encadrements/activation/form";
        }
     }
    @GetMapping("/cours/lists")
    public String cours( Model model, HttpServletRequest request){


        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Compte> alls = compteRepository.findAll();
        Collection<Cours> allsCours = new ArrayList<>();
        Role role = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        Role role1 = roleRepository.findByName(ERole.ROLE_ENFANT.toString());
        Collection<Encadreur> allEncadreurs = encadreurRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
            if (compte.getRoles().contains(role)){
                System.out.println("je suis encadreur");
                Encadreur encadreur = encadreurRepository.getOne(compte.getEnseignant().getId());
                model.addAttribute("lists", enfantRepository.findAllByEncadreurs_Id(encadreur.getId(),Sort.by(Sort.Direction.DESC,"id")));
                model.addAttribute("ecoleId", encadreur.getId());
                allsCours.addAll(coursRepository.findAllByCompte_IdAndType(compte.getId(),ECours.COURS.toString()));
            }else if (compte.getRoles().contains(role1)){
                System.out.println("je suis enfant");
                Collection<Encadreur> encadreurs = encadreurRepository.findAllByEnfants_Id(compte.getEnfant().getId(),Sort.by(Sort.Direction.DESC,"id"));
                System.out.println(encadreurs.size());
                for (Encadreur encadreur : encadreurs){
                    allsCours.addAll(coursRepository.findAllByCompte_IdAndType(compteRepository.findByEnseignant_Id(encadreur.getId()).getId(),ECours.COURS.toString()));
                }
                Enfant enfant = enfantRepository.getOne(compte.getEnfant().getId());
                List<Encadreur> encadreurs1 = (List<Encadreur>) encadreurRepository.findAllByEnfants_Id(enfant.getId(),Sort.by(Sort.Direction.DESC,"id"));
                System.out.println(encadreurs1);
                List<Compte> comptes = new ArrayList<>();
                for (Encadreur encadreur : encadreurs1) {
                    comptes.add(compteRepository.findByEnseignant_Id(encadreur.getId()));
                }
                System.out.println(comptes);
                model.addAttribute("directeur",comptes);
                if(encadreurs1.size()>0)
                    model.addAttribute("ecoleId", encadreurs1.get(0).getId());
                else
                    model.addAttribute("ecoleId", "0000");

            }else {
                System.out.println("je suis root");
                for (Encadreur encadreur : allEncadreurs) {

                    allsCours.addAll(coursRepository.findAllByCompte_IdAndType(compteRepository.findByEnseignant_Id(encadreur.getId()).getId(),ECours.COURS.toString()));
                }
            }

        model.addAttribute("lists1",allsCours);

        System.out.println("je suis et contient: "+allsCours);
        request.getSession().setAttribute("compte",compte);
        model.addAttribute("course",new Cours());

        return "encadrements/courses";
    }

    @GetMapping("/cours/delete/{id}")
    public String deleteCours(@PathVariable Long id){
        coursRepository.deleteById(id);
        return "redirect:/encadrements/cours/lists";
    }

    @GetMapping("/bibliotheque/lists")
    public String bibliotheques( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        request.getSession().setAttribute("compte",compte);
        List<Livre> alls = new ArrayList<>();
        List<Livre> livres = livreRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        List<Livre> generals1 = livreRepository.findAllBySalle(ENiveau.generale_secondaire.toString(),Sort.by(Sort.Direction.DESC,"id"));
        livres.addAll(generals1);
        for (int i=0;i<livres.size();i++){
            if (!(i>12)){
                alls.add(livres.get(i));

            }
        }
        model.addAttribute("lists",alls);
        return "encadrements/bibliotheques";
    }

    @GetMapping("/cours/update/{id}")
    public String updateCours(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Cours cours = coursRepository.getOne(id);
        model.addAttribute("cours",cours);
        return "encadrements/updateCourse";
    }

    @PostMapping("/cours/save")
    public String saveCourse(Cours cours, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        cours.setCompte(compte);
        System.out.println("jesuis entrain d'enregistrer");
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        multipart.store(file);
        cours.setFichier("/upload-dir/"+file.getOriginalFilename());

        System.out.println("je fait un enregistrement");
        cours.setDate(dateFormat.format(date));
        cours.setType(ECours.COURS.toString());
        coursRepository.save(cours);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté un nouveau devoir avec succès");
        return "redirect:/encadrements/cours/lists";
    }

    @GetMapping("/devoirs/lists")
    public String devoirs( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Encadreur> allEncadreurs = encadreurRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        Collection<Cours> allsCours = new ArrayList<>();
        Role role = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        Role role1 = roleRepository.findByName(ERole.ROLE_ENFANT.toString());
            if (compte.getRoles().contains(role)){
                allsCours.addAll(coursRepository.findAllByCompte_IdAndType(compte.getId(),ECours.DEVOIRS.toString()));
            }else if (compte.getRoles().contains(role1)){

                Collection<Encadreur> encadreurs = encadreurRepository.findAllByEnfants_Id(compte.getEnfant().getId(),Sort.by(Sort.Direction.DESC,"id"));
                for (Encadreur encadreur : encadreurs){
                    allsCours.addAll(coursRepository.findAllByCompte_IdAndType(compteRepository.findByEnseignant_Id(encadreur.getId()).getId(),ECours.DEVOIRS.toString()));
                }
                System.out.println("je contient : "+allsCours.size());
            }else {
                System.out.println("je suis root");
                for (Encadreur encadreur : allEncadreurs) {
                    allsCours.addAll(coursRepository.findAllByCompte_IdAndType(compteRepository.findByEnseignant_Id(encadreur.getId()).getId(),ECours.DEVOIRS.toString()));
                }
            }


            model.addAttribute("lists",allsCours);

        System.out.println(allsCours);
        model.addAttribute("devoir",new Cours());
        return "encadrements/devoirs";
    }

    @GetMapping("/devoir/update/{id}")
    public String updateDevoir(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Cours devoir = coursRepository.getOne(id);
        model.addAttribute("devoir",devoir);
        return "encadrements/updateDevoir";
    }


    @PostMapping("/devoirs/save")
    public String saveDevoir(Cours devoir, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        devoir.setCompte(compte);

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        multipart.store(file);
        devoir.setFichier("/upload-dir/"+file.getOriginalFilename());


        devoir.setDate(dateFormat.format(date));
        devoir.setType(ECours.DEVOIRS.toString());
        coursRepository.save(devoir);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté un nouveau devoir avec succès");
        return "redirect:/encadrements/devoirs/lists";
    }

    @GetMapping("/reponses/add/{id}")
    public String reponsesForm(@PathVariable Long id, Model model){
        Cours devoir = coursRepository.getOne(id);
        Response reponse = new Response();
        model.addAttribute("devoir",devoir);
        model.addAttribute("reponse",reponse);
        return "encadrements/reponse";
    }

    @PostMapping("/reponses/save/{id}")
    public String reponseSave(Response response, HttpServletRequest request, @PathVariable Long id, @RequestParam("file") MultipartFile file){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        response.setCompte(compte);
        response.setCours(coursRepository.getOne(id));
        response.setSalle(coursRepository.getOne(id).getSalle());
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        response.setDate(dateFormat.format(date));
        multipart.store(file);
        response.setFichier("/upload-dir/"+file.getOriginalFilename());

        responseRepository.save(response);
        return "redirect:/encadrements/reponses/lists";
    }


    @GetMapping("/reponses/lists")
    public String reponses( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());

        Collection<Enfant> allEnfants = enfantRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        Collection<Response> allsCours = new ArrayList<>();
        Collection<Cours> tempCours = new ArrayList<>();
        Role role = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        Role role1 = roleRepository.findByName(ERole.ROLE_ENFANT.toString());
            if (compte.getRoles().contains(role)){
                tempCours.addAll(coursRepository.findAllByCompte_IdAndType(compte.getId(),ECours.DEVOIRS.toString()));
                for (Cours cours : tempCours){
                    allsCours.addAll(responseRepository.findAllByCours_Id(cours.getId()));
                }
            }else if (compte.getRoles().contains(role1)){
                    allsCours.addAll(responseRepository.findAllByCompte_Id(compte.getId()));
                System.out.println(allsCours.size());
            }else {
                System.out.println("je suis root");
                for (Enfant enfant : allEnfants) {
                    allsCours.addAll(responseRepository.findAllByCompte_Id(compteRepository.findByEnfant_Id(enfant.getId()).getId()));
                }

        }

           /* for (Cours cours : allsCours){
                if (cours.getStatus().equals(false)){
                    cours.setStatus(true);
                    coursRepository.save(cours);
                }
            }*/
            model.addAttribute("lists", allsCours);

        return "encadrements/reponses";
    }


    @Autowired
    private ExamenRepository examenRepository;

    @GetMapping("/examens/lists")
    public String examens( Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Compte> alls = compteRepository.findAll();
        Collection<Examen> allsCours = new ArrayList<>();
        Collection<Encadreur> allEncadreurs = encadreurRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        Role role = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        Role role1 = roleRepository.findByName(ERole.ROLE_ENFANT.toString());
            if (compte.getRoles().contains(role)){
                allsCours.addAll(examenRepository.findAllByCompte_Id(compte.getId()));
            }else if (compte.getRoles().contains(role1)){

                Collection<Encadreur> encadreurs = encadreurRepository.findAllByEnfants_Id(compte.getEnfant().getId(),Sort.by(Sort.Direction.DESC));
                for (Encadreur encadreur : encadreurs){
                    allsCours.addAll(examenRepository.findAllByCompte_Id(compteRepository.findByEnseignant_Id(encadreur.getId()).getId()));
                }
            }else {
                System.out.println("je suis root");
                for (Encadreur encadreur : allEncadreurs) {
                    allsCours.addAll(examenRepository.findAllByCompte_Id(compteRepository.findByEnseignant_Id(encadreur.getId()).getId()));
                }
            }


            model.addAttribute("lists",allsCours);



        model.addAttribute("examen",new Examen());
        return "encadrements/examens";
    }

    @GetMapping("/examen/update/{id}")
    public String updateExamen(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Cours examen = coursRepository.getOne(id);
        model.addAttribute("examen",examen);
        return "encadrements/updateExamen";
    }

    @PostMapping("/examens/save")
    public String saveExamen(Examen examen, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        examen.setCompte(compte);

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        multipart.store(file);
        examen.setFichier("/upload-dir/"+file.getOriginalFilename());


        examen.setDate(dateFormat.format(date));
        examenRepository.save(examen);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté un nouveau devoir avec succès");
        return "redirect:/encadrements/examens/lists";
    }

    @GetMapping("/examen/delete/{id}")
    public String deleteExamen(@PathVariable Long id){
        examenRepository.deleteById(id);
        return "redirect:/encadrements/examen/lists";
    }

    @GetMapping("/access-denied")
    public String access_denied(){
        return "encadrements/access-denied";
    }

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/message/save")
    public String saveMessage(Message message, @RequestParam("file") MultipartFile file, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        message.setCompte(compte);
        message.setSender(compte.getUsername());
        message.setDate(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        message.setVisibilite(message.getVisibilite().toString());
        multipart.store(file);
        message.setFichier("/upload-dir/"+file.getOriginalFilename());


        messageRepository.save(message);
        Mail sender = new Mail();
        sender.sender(
                compte.getEmail(),
                "Envoi d'un message",
                "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());
        Role roleEncadreur = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        Role roleParent = roleRepository.findByName(ERole.ROLE_PARENT.toString());

        return "redirect:/encadrements/message";

    }

    @GetMapping("/message")
    public String messages( Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Role roleEncadreur = roleRepository.findByName(ERole.ROLE_ENCADREUR.toString());
        Role roleParent = roleRepository.findByName(ERole.ROLE_PARENT.toString());
        Collection<Message> messages =messageRepository.findAllByVisibilite(EVisibilite.ENCADREUR.toString(), Sort.by(Sort.Direction.DESC,"id"));
        Collection<Message> messages2 =messageRepository.findAllByVisibilite(EVisibilite.PARENT.toString(), Sort.by(Sort.Direction.DESC,"id"));
        Collection<Message> messages1 = messageRepository.findAllByCompte_Id(compte.getId());

        if (compte.getRoles().contains(roleEncadreur)){
            for (Message message : messages1){
                if(!(messages.contains(message))){
                    messages.add(message);
                }
            }
            model.addAttribute("lists",messages);
        }else {
            for (Message message : messages1){
                if(!(messages2.contains(message))){
                    messages2.add(message);
                }
            }
            model.addAttribute("lists",messages2);
        }


        model.addAttribute("message",new Message());
        return "encadrements/messages";
    }

    @Autowired
   private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        model.addAttribute("compteDto", new CompteRegistrationDto());
        return "encadrements/account";
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
                return "redirect:/encadrements/account/detail/"+compte.getId();
            }
        }

        compte.setEncode(compteRegistrationDto.getPassword());
        compteRepository.save(compte);

        return "redirect:/encadrements/account/detail/"+compte.getId();

    }


}
