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

        model.addAttribute("message","Bien vouloir contacter le responsable de votre ecole pour obtenir les informations de connexion a votre compte");
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
        Ecole ecole = compte.getEcole();
        System.out.println(ecole.getName());

        request.getSession().setAttribute("ecole", ecole);
        request.getSession().setAttribute("compte",compte);
        return "redirect:/enseignant/ecole/detail/"+ecole.getId();
    }

    @GetMapping("/ecole/detail/{id}")
    public String detail(Model model, @PathVariable Long id,HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        Enseignant enseignant = compte.getEnseignant();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignant.getId());
        System.out.println(salles);
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
        Ecole ecole = ecoleRepository.getOne(id);
        List<Enseignant> enseignants = new ArrayList<>();
        List<Parent> parents = new ArrayList<>();
        List<Compte> directeur = new ArrayList();
          List<Compte> comptes = (List<Compte>) compteRepository.findAllByEcole_Id(id);
          for(int i=0;i<comptes.size();i++){
              if(comptes.get(i).getId()!=compte.getId()&&comptes.get(i).getEnseignant()!=null)
                  enseignants.add(comptes.get(i).getEnseignant());
              List<Role> roles = (List<Role>) comptes.get(i).getRoles();
              for(int j=0;j<roles.size();j++){
                 if(roles.get(j).getName().equals("ROLE_DIRECTEUR") || roles.get(j).getName().equals("ROLE_ROOT")){
                     directeur.add(comptes.get(i));
                 }
              }

          }
        for(int i=0;i<comptes.size();i++){
            if(comptes.get(i).getId()!=compte.getId()&&comptes.get(i).getParent()!=null)
                parents.add(comptes.get(i).getParent());

        }
    System.out.println(directeur);
        request.getSession().setAttribute("ecole", ecole);
        model.addAttribute("ecole",ecole);
        model.addAttribute("ecoleId",ecole.getId());
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

    @GetMapping("/bibliotheque/lists")
    public String allBibliotheques( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.findByPrincipal(compte.getEnseignant().getName() + "  " + compte.getEnseignant().getPrenom());

        model.addAttribute("classe",salle);
        List<Livre> livres = livreRepository.findAllBySalle(salle.getNiveau(),Sort.by(Sort.Direction.DESC,"id"));
        List<Livre> generals = livreRepository.findAllBySalle(ENiveau.generale_primaire.toString(),Sort.by(Sort.Direction.DESC,"id"));
        List<Livre> generals1 = livreRepository.findAllBySalle(ENiveau.generale_secondaire.toString(),Sort.by(Sort.Direction.DESC,"id"));
        livres.addAll(generals);
        livres.addAll(generals1);
        List<Livre> alls = new ArrayList<>();
        for (int i=0; i<livres.size();i++){
            if (!(i>9)){
                alls.add(livres.get(i));
            }
        }

        model.addAttribute("lists",alls);

        return "enseignant/bibliotheques";
    }



    @GetMapping("/classe/lists")
    public String allClasse( Model model, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        Ecole ecole = ecoleRepository.getOne(id);
        model.addAttribute("ecoleId",ecole.getId());
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


    @PostMapping("/eleves/save/{id}")
    public String save(Eleve eleve, @PathVariable Long id, RedirectAttributes redirectAttributes){

        Parent existParent = parentRepository.findByNomComplet(eleve.getNomCompletTuteur().toUpperCase());
        Salle salle = salleRepository.getOne(id);
        eleve.setSalle(salle);

        if (existParent != null){
            eleve.setParent(existParent);
        }else {
            Parent parent= new Parent();
            CompteRegistrationDto compteRegistrationDto = new CompteRegistrationDto();
            parent.setNomComplet(eleve.getNomCompletTuteur().toUpperCase());
            parent.setEmail(eleve.getEmailTuteur());
            parent.setTelephone(eleve.getTelephoneTuteur().toUpperCase());
            parent.setWhatsapp(eleve.getWhatsappTuteur().toUpperCase());
            compteRegistrationDto.setEmail(parent.getEmail().toLowerCase());
            compteRegistrationDto.setUsername(parent.getNomComplet().toLowerCase());
            compteRegistrationDto.setPassword(compteRegistrationDto.getUsername());
            compteRegistrationDto.setConfirmPassword(compteRegistrationDto.getPassword());
            parentRepository.save(parent);
            compteService.saveParent(compteRegistrationDto,"/images/profile.jpeg",parent);
            eleve.setParent(parent);
            eleveRepository.save(eleve);
            MailService mailService = new MailService();
            mailService.sendSimpleMessage(
                    compteRegistrationDto.getEmail(),
                    "Vous venez d'etre ajouter en tant que enseignant dans l'ecole virtuelle  :",
                    "vos identifiants : username:" +compteRegistrationDto.getUsername()+" et password : "+compteRegistrationDto.getPassword()

            );

            mailService.sendSimpleMessage(
                    "confirmation@yesbanana.org",
                    "YesBanana: Notification Inscription d'un enseignant",
                    "L'utilisateur " + compteRegistrationDto.getUsername() + " dont l'email est " +
                            compteRegistrationDto.getEmail()+ "  Vient de s'inscrire " +
                            "sur la plateforme YesBanana. Veuillez vous connectez pour manager son status.");



        }



        redirectAttributes.addFlashAttribute("success","Vous avez ajouter avec success un nouvel eleve dans cette classe");
        return "redirect:/enseignant/eleves/lists/"+salle.getId();
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(compte.getEnseignant().getId());
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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

        redirectAttributes.addFlashAttribute("success", "vous avez ajouter un vouveau cours avec success");
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        redirectAttributes.addFlashAttribute("success", "vous avez ajouter un vouveau devoir avec success");
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        redirectAttributes.addFlashAttribute("success", "vous avez ajouter un vouveau devoir avec success");
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
        message.setEcole(salle.getEcole().getName());
        message.setSalle(salle.getNiveau()+""+salle.getId());
        message.setDate(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        message.setVisibilite(message.getVisibilite().toString());
        multipart.store(file);
        message.setFichier("/upload-dir/"+file.getOriginalFilename());


        messageRepository.save(message);
        Mail sender = new Mail();
        Collection<Compte> comptes = compteRepository.findAllByEcole_Id(compte.getEcole().getId());

        for (Compte compte1 : comptes){
            if (message.getVisibilite().toString().contains(EVisibilite.DIRECTION.toString())){
                if (compte1.getEcole() == salle.getEcole()){

                    sender.sender(
                            compte1.getEmail(),
                            "Envoi d'un message",
                            "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

                }
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

    @GetMapping("/message/{id}")
    public String messages(@PathVariable Long id, Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        Ecole ecole = salle.getEcole();
        Collection<Message> messages = messageRepository.findAllByVisibiliteAndSalleAndEcole(EVisibilite.ENSEIGNANT.toString(),(salle.getNiveau()+""+salle.getId()),ecole.getName(), Sort.by(Sort.Direction.DESC,"id"));
        messages.addAll(messageRepository.findAllByVisibiliteAndEcole(EVisibilite.PUBLIC.toString(),ecole.getName(),Sort.by(Sort.Direction.DESC,"id")));
        Collection<Message> messages1 = messageRepository.findAllByCompte_Id(compte.getId());
        for (Message message : messages1){
            if(!(messages.contains(message))){
                messages.add(message);
            }
        }
        System.out.println(messages.size());
        model.addAttribute("lists",messages);
        model.addAttribute("ecole",ecole);
        model.addAttribute("classe",salle);
        model.addAttribute("message",new Message());
        return "enseignant/messages";
    }

    @GetMapping("/messages")
    public String messageClasse(Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());

        Ecole ecole = compte.getEcole();
        Collection<Message> messages = messageRepository.findAllByVisibiliteAndEcole(EVisibilite.ENSEIGNANT.toString(),ecole.getName(),Sort.by(Sort.Direction.DESC,"id"));
        messages.addAll(messageRepository.findAllByVisibiliteAndEcole(EVisibilite.PUBLIC.toString(),ecole.getName(),Sort.by(Sort.Direction.DESC, "id")));
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
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
        Salle salle = salleRepository.getOne(id);
        Collection<Hebdo> hebdos = hebdoRepository.findAllByCompte_IdAndSalle_Id(compte.getId(),salle.getId(), Sort.by(Sort.Direction.DESC,"id"));
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

    @GetMapping("/hebdo/detail/{id}")
    public String detailHebdo(Model model, @PathVariable Long id, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Ecole ecole = compte.getEcole();
        Collection<Salle> salles = salleRepository.findAllByEnseignants_Id(enseignantRepository.findByEmail(compte.getEmail()).getId());
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
        model.addAttribute("salles",salles);
        model.addAttribute("classe",salle);
        model.addAttribute("planning",new Planning());
        return "enseignant/hebdo";
    }

    @PostMapping("/hebdo/save/{id}")
    public String saveHebdo(Hebdo hebdo, RedirectAttributes redirectAttributes, HttpServletRequest request, @PathVariable Long id){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        hebdo.setCompte(compte);
        Salle salle = (Salle)request.getSession().getAttribute("classe");
        hebdo.setSalle(salle);
        hebdoRepository.save(hebdo);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouter une nouvelle semaine avec success");
        return "redirect:/enseignant/hebdos/lists/"+ salle.getId();
    }

    @PostMapping("/planning/save/{id}")
    public String saveplanning(Planning planning, RedirectAttributes redirectAttributes, HttpServletRequest request, @PathVariable Long id){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Planning> plannings = planningRepository.findAllByHebdo_Id(hebdo.getId());
        if (plannings.size()<=6){
            redirectAttributes.addFlashAttribute("error","Vous ne pouvez pas ajouter plus de 6 lecons pour une semaine");
            return "redirect:/enseignant/hebdo/detail/"+ hebdo.getId();
        }
        planning.setHebdo(hebdo);
        ArrayList<Boolean> array = new ArrayList<>();
        array.add(false);
        array.add(false);
        planning.setValidations(array);
        planningRepository.save(planning);
        redirectAttributes.addFlashAttribute("success", "vous avez ajouter une nouvelle journee avec success");
        return "redirect:/enseignant/hebdo/detail/"+ hebdo.getId();
    }

    @GetMapping("/activate/planning/{id}")
    public String activatePlan(@PathVariable Long id, HttpServletRequest request){
        Planning planning = planningRepository.getOne(id);
        planning.getValidations().clear();
        planning.getValidations().add(true);
        planningRepository.save(planning);
        Salle salle = (Salle)request.getSession().getAttribute("classe");
        return "redirect:/enseignant/hebdo/detail/"+planning.getHebdo().getId();
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
    }

    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        return "enseignant/account";
    }


}
