package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.ECours;
import com.derteuffel.school.enums.ENiveau;
import com.derteuffel.school.enums.EVisibilite;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.helpers.PresenceForm;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Mail;
import com.derteuffel.school.services.MailService;
import com.derteuffel.school.services.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 23/03/2020.
 */
@Controller
@RequestMapping("/enseignant")
public class EnseignantLoginController {

    @Autowired
    private EcoleRepository ecoleRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private CompteRepository compteRepository;
    @Autowired
    private EleveRepository eleveRepository;
    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private ExamenRepository examenRepository;
    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private CompteService compteService;
    /*@Value("${file.upload-dir}")
    private  String fileStorage ;*///=System.getProperty("user.dir")+"/src/main/resources/static/downloadFile/";

    @Autowired
    private Multipart multipart;
    @GetMapping("/login")
    public String director(Model model){

        model.addAttribute("message","Bien vouloir contacter le responsable de votre école pour obtenir les informations de connexion a votre compte");
        return "enseignant/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/enseignant/login";
    }
    @GetMapping("/home")
    public String home(HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());

        request.getSession().setAttribute("compte",compte);
        return "redirect:/enseignant/ecole/detail";
    }

    @GetMapping("/ecole/detail")
    public String detail(Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant != null) {
            System.out.println(enseignant.getName());
             salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
            System.out.println(salles);
            System.out.println(salles.size());
        }else {
            salles.addAll(salleRepository.findAll());
        }
        /*if (!(enseignant.getSallesIds().isEmpty())) {
            for (Long ids : enseignant.getSallesIds()) {
                salles.add(salleRepository.getOne(ids));
            }
            System.out.println(salles);
            model.addAttribute("salles",salles);
            request.getSession().setAttribute("salles",salles);
        }*/
        request.getSession().setAttribute("salles",salles);
        model.addAttribute("salles",salles);
        List<Enseignant> enseignants = new ArrayList<>();
        List<Parent> parents = new ArrayList<>();
        List<Compte> directeur = new ArrayList();
          List<Compte> comptes = (List<Compte>) compteRepository.findAll();
          for(int i=0;i<comptes.size();i++){
              if(comptes.get(i).getId()!=compte.getId()&&comptes.get(i).getEnseignant()!=null)
                  enseignants.add(comptes.get(i).getEnseignant());
              List<Role> roles = (List<Role>) comptes.get(i).getRoles();
              for(int j=0;j<roles.size();j++){
                 if(roles.get(j).getName().equals("ROLE_PREFET") || roles.get(j).getName().equals("ROLE_ROOT")){
                     directeur.add(comptes.get(i));
                 }
              }

          }
        for(int i=0;i<comptes.size();i++){
            if(comptes.get(i).getId()!=compte.getId()&&comptes.get(i).getParent()!=null)
                parents.add(comptes.get(i).getParent());

        }
    System.out.println(directeur);
        model.addAttribute("lists",enseignants);
        model.addAttribute("parents",parents);
        model.addAttribute("directeur",directeur);
        return "enseignant/home";
    }

    @GetMapping("/classe/detail/{id}")
    public String classeDetail(@PathVariable Long id,HttpServletRequest request,Model model){
        Salle salle = salleRepository.getOne(id);
        request.getSession().setAttribute("classe",salle);
        model.addAttribute("classe",salle);

        return "redirect:/enseignant/eleves/lists/"+salle.getId();

    }

    @GetMapping("/eleves/lists/{id}")
    public String allEleves(@PathVariable Long id, Model model,HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(id);
        model.addAttribute("classe",salleRepository.getOne(id));
        model.addAttribute("student",new Eleve());
        model.addAttribute("lists",eleves);
        request.getSession().setAttribute("compte",compte);
        System.out.println(eleves.size());
        return "enseignant/eleves";
    }

    @GetMapping("/eleves/lists")
    public String allPrincipales( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Eleve> eleves = new ArrayList<>();
        Salle salle = salleRepository.findByPrincipal(compte.getEnseignant().getName() + "  " + compte.getEnseignant().getPrenom());
        if (salle != null) {
            System.out.println(salle.getNiveau());
            eleves.addAll(eleveRepository.findAllBySalle_Id(salle.getId()));
        }
        model.addAttribute("classe",salle);
        model.addAttribute("lists",eleves);
        System.out.println(eleves.size());
        return "enseignant/elevesP";
    }

    @GetMapping("/bibliotheque/lists/{id}")
    public String allBibliotheques( Model model, HttpServletRequest request, @PathVariable Long id){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);

        model.addAttribute("classe",salle);
        List<Livre> livres = new ArrayList<>();
        List<Livre> niveaux = livreRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        for (Livre livre : niveaux){
            if (salle.getNiveau().contains(livre.getSalle())){
                livres.add(livre);
            }
        }
        List<Livre> generals1 = livreRepository.findAllBySalle(ENiveau.generale_secondaire.toString(),Sort.by(Sort.Direction.DESC,"id"));
        livres.addAll(generals1);
        List<Livre> alls = new ArrayList<>();
        for (int i=0; i<livres.size();i++){
            if (!(i>9)){
                alls.add(livres.get(i));
            }
        }

        if (compte.getStatus() == false || compte.getStatus() == null){
            model.addAttribute("classe",salle);
            model.addAttribute("error","Veuillez contacter l'équipe YesB pour avoir votre code d'activation");
            return "enseignant/activation";
        }else {
            model.addAttribute("lists", alls);

            return "enseignant/bibliotheques";
        }
    }


    @GetMapping("/activation/code/{id}")
    public String activation(String code,HttpServletRequest request, @PathVariable Long id, Model model){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        if (code.equals(compte.getBibliothequeCode())){
            compte.setStatus(true);
            compteRepository.save(compte);
            return "redirect:/enseignant/bibliotheque/lists/"+salle.getId();
        }else {
            model.addAttribute("classe",salle);
            model.addAttribute("error","Votre code n'est pas valide, veuillez contacter l'équipe YesB pour tout besoin d'assistance");
            return "enseignant/activation";
        }
    }



    @GetMapping("/classe/lists")
    public String allClasse( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        model.addAttribute("lists",salles);
        return "enseignant/classes";
    }



    @GetMapping("/parents/lists/{id}")
    public String allParents(@PathVariable Long id, Model model){

        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(id);
        Collection<Parent> parents = new ArrayList<>();
        for (Eleve eleve:eleves){
                if (!(parents.contains(eleve.getParent()))) {
                    parents.add(eleve.getParent());
                }
        }
        model.addAttribute("classe",salleRepository.getOne(id));
        model.addAttribute("parents",parents);
        return "enseignant/parents";
    }

    @GetMapping("/parent/detail/{id}/{classeId}")
    public String parentDetail(@PathVariable Long id, @PathVariable Long classeId, Model model){
        Parent parent = parentRepository.getOne(id);
        Collection<Eleve> eleves = eleveRepository.findAllByParent_Id(parent.getId());
        Salle salle = salleRepository.getOne(classeId);
        model.addAttribute("parent",parent);
        model.addAttribute("lists",eleves);
        model.addAttribute("classe",salle);
        return "enseignant/parent";
    }

    @GetMapping("/parent/delete/{id}/{salleId}")
    public String parentDelete(@PathVariable Long id, @PathVariable Long salleId){
        parentRepository.deleteById(id);
        return "redirect:/enseignant/parents/lists/"+salleId;
    }

    @GetMapping("/eleves/update/{id}")
    public String updateEleve(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Eleve eleve = eleveRepository.getOne(id);
        model.addAttribute("eleve",eleve);
        return "enseignant/updateEleve";
    }


    @GetMapping("/eleve/delete/{id}/{salleId}")
    public String deleteEleve(@PathVariable Long id, @PathVariable Long salleId){
        eleveRepository.deleteById(id);
        return "redirect:/enseignant/eleve/lists/"+salleId;
    }

    @GetMapping("/cours/lists/{id}")
    public String cours(@PathVariable Long id, Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Salle salle = salleRepository.getOne(id);
        Collection<Cours> cours = coursRepository.findAllBySalleAndType(salle.getNiveau()+""+salle.getId(), ECours.COURS.toString());
        model.addAttribute("lists",cours);
        model.addAttribute("salles",salles);
        model.addAttribute("classe",salle);
        model.addAttribute("course",new Cours());
        return "enseignant/courses";
    }


    @GetMapping("/cours/update/{id}")
    public String updateCours(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Cours cours = coursRepository.getOne(id);
        model.addAttribute("cours",cours);
        model.addAttribute("salles",salles);
        return "enseignant/updateCourse";
    }

    @PostMapping("/cours/save")
    public String saveCourse(Cours cours, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request,Long id){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        cours.setCompte(compte);
        Salle salle = salleRepository.getOne(id);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        multipart.store(file);

        cours.setFichier("/upload-dir/"+file.getOriginalFilename());


        cours.setDate(dateFormat.format(date));
        cours.setType(ECours.COURS.toString());
        System.out.println(cours.getSalle());
        System.out.println(salle.getNiveau());
        cours.setSalle(cours.getSalle()+""+salle.getId());
        coursRepository.save(cours);

        redirectAttributes.addFlashAttribute("success", "vous avez ajouté un nouveau cours avec succès");
        return "redirect:/enseignant/cours/lists/"+ salle.getId();
    }

    @GetMapping("/cours/delete/{id}/{salleId}")
    public String coursDelete(@PathVariable Long id, @PathVariable Long salleId){
        coursRepository.deleteById(id);
        return "redirect:/enseignant/cours/lists/"+salleId;
    }

    @GetMapping("/devoirs/lists/{id}")
    public String devoirs(@PathVariable Long id, Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Salle salle = salleRepository.getOne(id);
        Collection<Cours> devoirs = coursRepository.findAllBySalleAndType(salle.getNiveau()+""+salle.getId(), ECours.DEVOIRS.toString());
        model.addAttribute("lists",devoirs);
        model.addAttribute("salles",salles);
        model.addAttribute("classe",salle);
        model.addAttribute("devoir",new Cours());
        return "enseignant/devoirs";
    }

    @GetMapping("/devoir/update/{id}")
    public String updateDevoir(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Cours devoir = coursRepository.getOne(id);
        model.addAttribute("devoir",devoir);
        model.addAttribute("salles",salles);
        return "enseignant/updateDevoir";
    }

    @PostMapping("/devoirs/save")
    public String saveDevoir(Cours devoir, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        devoir.setCompte(compte);
        Salle salle = (Salle)request.getSession().getAttribute("classe");

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        multipart.store(file);
        devoir.setFichier("/upload-dir/"+file.getOriginalFilename());


        devoir.setDate(dateFormat.format(date));
        devoir.setType(ECours.DEVOIRS.toString());
        devoir.setSalle(devoir.getSalle()+""+salle.getId());
        coursRepository.save(devoir);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté un nouveau devoir avec succès");
        return "redirect:/enseignant/devoirs/lists/"+ salle.getId();
    }

    @GetMapping("/devoirs/delete/{id}/{salleId}")
    public String devoirDelete(@PathVariable Long id, @PathVariable Long salleId){
        coursRepository.deleteById(id);

        return "redirect:/enseignant/devoirs/lists/"+salleId;
    }
    @GetMapping("/reponses/lists/{id}")
    public String reponses(@PathVariable Long id, Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Salle salle = salleRepository.getOne(id);
        Collection<Response> reponses = responseRepository.findAllBySalle(salle.getNiveau()+""+salle.getId());
        model.addAttribute("lists",reponses);
        model.addAttribute("classe",salle);
        model.addAttribute("salles",salles);
        return "enseignant/reponses";
    }


    @GetMapping("/examens/lists/{id}")
    public String examens(@PathVariable Long id, Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Salle salle = salleRepository.getOne(id);
        Collection<Examen> examens = examenRepository.findAllBySalle(salle.getNiveau()+""+salle.getId());
        model.addAttribute("lists",examens);
        model.addAttribute("salles",salles);
        model.addAttribute("classe",salle);
        model.addAttribute("examen",new Examen());
        return "enseignant/examens";
    }

    @GetMapping("/examen/update/{id}")
    public String updateExamen(@PathVariable Long id, Model model,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = new ArrayList<>();
        if (enseignant!=null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Cours examen = coursRepository.getOne(id);
        model.addAttribute("examen",examen);
        model.addAttribute("salles",salles);
        return "enseignant/updateExamen";
    }

    @PostMapping("/examens/save")
    public String saveExamen(Examen examen, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        examen.setCompte(compte);
        Salle salle = (Salle)request.getSession().getAttribute("classe");

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
        multipart.store(file);
        examen.setFichier("/upload-dir/"+file.getOriginalFilename());


        examen.setDate(dateFormat.format(date));
        examen.setSalle(examen.getSalle()+""+salle.getId());
        examenRepository.save(examen);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté un nouveau devoir avec succès");
        return "redirect:/enseignant/examens/lists/"+ salle.getId();
    }


    @GetMapping("/examen/delete/{id}/{salleId}")
    public String examenDelete(@PathVariable Long id, @PathVariable Long salleId){
        examenRepository.deleteById(id);
        return "redirect:/enseignant/examens/lists/"+salleId;
    }
    @GetMapping("/access-denied")
    public String access_denied(){
        return "enseignant/access-denied";
    }

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/message/save/{id}")
    public String saveMessage(Message message, @RequestParam("file") MultipartFile file, @PathVariable Long id, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        message.setCompte(compte);
        message.setSender(compte.getUsername());
        message.setSalle(salle.getNiveau()+""+salle.getId());
        message.setDate(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        message.setVisibilite(message.getVisibilite().toString());
        multipart.store(file);
        message.setFichier("/upload-dir/"+file.getOriginalFilename());


        messageRepository.save(message);
        Mail sender = new Mail();
        Collection<Compte> comptes = compteRepository.findAll();

        for (Compte compte1 : comptes){
            if (message.getVisibilite().toString().contains(EVisibilite.DIRECTION.toString())){
                    sender.sender(
                            compte1.getEmail(),
                            "Envoi d'un message",
                            "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

            }else {
                sender.sender(
                        compte.getEmail(),
                        "Envoi d'un message",
                        "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

            }
        }


        sender.sender(
                compte.getEmail(),
                "Envoi d'un message",
                "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

        return "redirect:/enseignant/message/"+salle.getId();

    }

    @PostMapping("/message/save")
    public String saveMessage1(Message message, @RequestParam("file") MultipartFile file, HttpServletRequest request){

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
        Collection<Compte> comptes = compteRepository.findAll();

        for (Compte compte1 : comptes){
            if (message.getVisibilite().toString().contains(EVisibilite.DIRECTION.toString())){
                    sender.sender(
                            compte1.getEmail(),
                            "Envoi d'un message",
                            "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

            }else {
                sender.sender(
                        compte.getEmail(),
                        "Envoi d'un message",
                        "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

            }
        }


        sender.sender(
                compte.getEmail(),
                "Envoi d'un message",
                "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

        return "redirect:/enseignant/messages";

    }

    @GetMapping("/message/{id}")
    public String messages(@PathVariable Long id, Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        Collection<Message> messages = messageRepository.findAllByVisibiliteAndSalle(EVisibilite.ENSEIGNANT.toString(),(salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC,"id"));
        messages.addAll(messageRepository.findAllByVisibilite(EVisibilite.PUBLIC.toString(),Sort.by(Sort.Direction.DESC,"id")));
        Collection<Message> messages1 = messageRepository.findAllByCompte_Id(compte.getId());
        for (Message message : messages1){
            if(!(messages.contains(message))){
                messages.add(message);
            }
        }
        System.out.println(messages.size());
        model.addAttribute("lists",messages);
        model.addAttribute("classe",salle);
        model.addAttribute("message",new Message());
        return "enseignant/messages";
    }

    @GetMapping("/messages")
    public String messageClasse(Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());

        Collection<Message> messages = messageRepository.findAllByVisibilite(EVisibilite.ENSEIGNANT.toString(),Sort.by(Sort.Direction.DESC,"id"));
        messages.addAll(messageRepository.findAllByVisibilite(EVisibilite.PUBLIC.toString(),Sort.by(Sort.Direction.DESC, "id")));
        messages.addAll(messageRepository.findAllByCompte_Id(compte.getId()));
        model.addAttribute("lists",messages);
        model.addAttribute("message", new Message());
        return "enseignant/messages1";


    }

    @Autowired
    private HebdoRepository hebdoRepository;

    @Autowired
    private PlanningRepository planningRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @GetMapping("/hebdos/lists/{id}")
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
        return "enseignant/hebdos";
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
        Collection<Salle> salles = new ArrayList<>();

        if (enseignant != null){
            salles.addAll(salleRepository.findAllByEnseignants_Id(enseignant.getId()));
        }else {
            salles.addAll(salleRepository.findAll());
        }
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());
        Collection<Presence> presences = presenceRepository.findAllByHebdo_Id(hebdo.getId());
        ArrayList<String> dates = new ArrayList<>();
        for (Presence presenceString : presences){
            dates.add(presenceString.getDate());
        }

        Salle salle = salleRepository.getOne(salleId);


        model.addAttribute("plannings",plannings);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("salles",salles);
        model.addAttribute("classe",salle);
        return "enseignant/hebdo";
    }

    @GetMapping("/classes/planning/lists/{id}/{salleId}")
    public String planningHebdo(Model model, @PathVariable Long id, HttpServletRequest request, @PathVariable Long salleId){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = enseignantRepository.findByEmail(compte.getEmail());
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
        model.addAttribute("classe",salle);
        return "enseignant/plannings";
    }

   /* @PostMapping("/hebdo/save/{id}")
    public String saveHebdo(Hebdo hebdo, RedirectAttributes redirectAttributes, HttpServletRequest request, @PathVariable Long id){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        hebdo.setCompte(compte);
        Salle salle = (Salle)request.getSession().getAttribute("classe");
        hebdo.setSalle(salle);
        hebdoRepository.save(hebdo);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté une nouvelle semaine avec succès");
        return "redirect:/enseignant/hebdos/lists/"+ salle.getId();
    }

    @PostMapping("/planning/save/{id}")
    public String saveplanning(Planning planning, RedirectAttributes redirectAttributes, HttpServletRequest request, @PathVariable Long id){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());
        if (plannings.size()<=6){
            redirectAttributes.addFlashAttribute("error","Vous ne pouvez pas ajouter plus de 6 leçons pour une semaine");
            return "redirect:/enseignant/hebdo/detail/"+ hebdo.getId();
        }
        planning.setHebdo(hebdo);
        planningRepository.save(planning);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouté une nouvelle journée avec succès");
        return "redirect:/enseignant/hebdo/detail/"+ hebdo.getId();
    }



    @GetMapping("/presence/add/{id}")
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
        return "enseignant/presenceNew";

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
        return "redirect:/enseignant/presence/add/"+presence.getHebdo().getId();
    }


    @GetMapping("/presence/detail/{id}")
    public String getPresence(@PathVariable Long id, Model model, HttpServletRequest request){
        Presence presence = presenceRepository.getOne(id);
        model.addAttribute("presence",presence);
        model.addAttribute("classe",presence.getHebdo().getSalle());
        model.addAttribute("hebdo",presence.getHebdo());
        return "enseignant/presenceDetail";
    }*/

    @Autowired
   private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        model.addAttribute("compteDto", new CompteRegistrationDto());
        return "enseignant/account";
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
                return "redirect:/enseignant/account/detail/"+compte.getId();
            }
        }

        compte.setEncode(compteRegistrationDto.getPassword());
        compteRepository.save(compte);

        return "redirect:/enseignant/account/detail/"+compte.getId();

    }


}
