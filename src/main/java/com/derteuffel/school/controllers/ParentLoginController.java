package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.ECours;
import com.derteuffel.school.enums.ENiveau;
import com.derteuffel.school.enums.EVisibilite;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Mail;
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
@RequestMapping("/parent")
public class ParentLoginController {

    @Autowired
    private EcoleRepository ecoleRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private PlanningRepository planningRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private EleveRepository eleveRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private ExamenRepository examenRepository;

    @Autowired
    private Multipart multipart;

    @Autowired
    private CompteService compteService;
    /*@Value("${file.upload-dir}")
    private  String fileStorage ; *///=System.getProperty("user.dir")+"/src/main/resources/static/downloadFile/";

    @GetMapping("/login")
    public String director(Model model){

        model.addAttribute("message","Bien vouloir contacter le responsable de votre école pour obtenir les informations de connexion à votre compte");
        return "parent/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/parent/login";
    }

    @GetMapping("/home")
    public String home(HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        Parent parent = compte.getParent();
        Collection<Eleve> eleves =eleveRepository.findAllByParent_Id(parent.getId());

        request.getSession().setAttribute("compte",compte);
        return "redirect:/parent/ecole/detail";
    }

    @GetMapping("/ecole/detail")
    public String ecoleDetail( Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        System.out.println(principal.getName());
        Compte compte = compteService.findByUsername(principal.getName());
        Parent parent = compte.getParent();
        Collection<Eleve> eleves =eleveRepository.findAllByParent_Id(parent.getId());
        Collection<Salle> salles = new ArrayList<>();
        Collection<Salle> lists = new ArrayList<>();
        for (Eleve eleve : eleves){
            salles.add(eleve.getSalle());
        }


        List<Enseignant> enseignants = new ArrayList<>();
        List<Compte> directeur = new ArrayList();
        List<Compte> comptes = (List<Compte>) compteRepository.findAll();
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

        model.addAttribute("lists1", salles);
        model.addAttribute("lists", enseignants);

        model.addAttribute("directeur",directeur);

        return "parent/ecole/home";
    }

    @GetMapping("/classe/detail")
    public String parentClasse(@PathVariable Long id, Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        Collection<Message> messages = messageRepository.findAllByVisibiliteAndSalle(EVisibilite.PARENT.toString(),(salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC,"id"));
        System.out.println(salle.getNiveau());
        messages.addAll(messageRepository.findAllByVisibiliteAndSalle(EVisibilite.PUBLIC.toString(),(salle.getNiveau()+""+salle.getId()), Sort.by(Sort.Direction.DESC,"id")));
        messages.addAll(messageRepository.findAllByVisibilite(EVisibilite.PUBLIC.toString(), Sort.by(Sort.Direction.DESC,"id")));
        Collection<Message> messages1 = messageRepository.findAllByCompte_Id(compte.getId());
        for (Message message : messages1){
            if(!(messages.contains(message))){
                messages.add(message);
            }
        }
        System.out.println(messages.size());
        model.addAttribute("classe",salle);
        model.addAttribute("lists",messages);
        model.addAttribute("message",new Message());
        return "parent/ecole/classe";
    }


    @GetMapping("/message/delete/{id}/{salleId}")
    public String messageDelete(@PathVariable Long id, @PathVariable Long salleId){
        messageRepository.deleteById(id);
        return "redirect:/parent/classe/detail/"+salleId;
    }

    @GetMapping("/cours/lists/{id}")
    public String cours(@PathVariable Long id, Model model){

        Salle salle = salleRepository.getOne(id);
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Cours> cours = new ArrayList<>();
        if (salles.contains(salle)) {
            cours = coursRepository.findAllBySalleAndType(salle.getNiveau()+""+salle.getId(), ECours.COURS.toString());
        }else {
            model.addAttribute("error","Vous n'avez aucune classe avec ce nom dans cet établissement");
        }
        model.addAttribute("lists",cours);
        model.addAttribute("classe",salle);
        return "parent/courses";
    }

    @GetMapping("/bibliotheque/lists/{id}")
    public String bibliotheques(@PathVariable Long id, Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
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


        model.addAttribute("classe",salle);
        if (compte.getStatus() == false || compte.getStatus() == null){
            model.addAttribute("classe",salle);
            model.addAttribute("error","Veuillez contacter l'équipe YesB pour avoir votre code d'activation");
            return "parent/activation";
        }else {
            model.addAttribute("lists", alls);

            return "parent/bibliotheques";
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
            return "redirect:/parent/bibliotheque/lists/"+salle.getId();
        }else {
            model.addAttribute("classe",salle);
            model.addAttribute("error","Votre code n'est pas valide, veuillez contacter l'équipe YesB pour tout besoin d'assistance");
            return "parent/activation";
        }
    }

    @GetMapping("/devoirs/lists/{id}")
    public String devoirs(@PathVariable Long id, Model model, HttpServletRequest request){

        Salle salle = salleRepository.getOne(id);
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Cours> devoirs = new ArrayList<>();
        if (salles.contains(salle)) {
            devoirs = coursRepository.findAllBySalleAndType(salle.getNiveau()+""+salle.getId(), ECours.DEVOIRS.toString());
        }else {
            model.addAttribute("error","Vous n'avez aucune classe avec ce nom dans cet établissement");
        }
        model.addAttribute("lists",devoirs);
        model.addAttribute("classe",salle);
        request.getSession().setAttribute("salleId", salle.getId());
        return "parent/devoirs";
    }

    @GetMapping("/reponses/lists/{id}/{username}")
    public String reponse(@PathVariable Long id,@PathVariable String username, Model model, HttpServletRequest request){
        Compte compte = compteService.findByUsername(username);
        Salle salle = salleRepository.getOne(id);
        Collection<Salle> salles = salleRepository.findAll();

        Collection<Response> reponses = new ArrayList<>();
        if (salles.contains(salle)) {
            reponses = responseRepository.findAllByCompte_IdAndSalle(compte.getId(),salle.getNiveau()+""+salle.getId());
        }else {
            model.addAttribute("error","Vous n'avez aucune classe avec ce nom dans cet établissement");
        }
        model.addAttribute("lists",reponses);
        model.addAttribute("classe",salle);
        return "parent/reponses";
    }

    @GetMapping("/reponse/delete/{id}/{salleId}")
    public String reponseDelete(Long id, Long salleId,HttpServletRequest request){
        responseRepository.deleteById(id);
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(salleId);
        return "redirect:/parent/reponses/lists/"+salle.getId()+"/"+compte.getUsername();
    }

    @GetMapping("/reponses/add/{id}")
    public String reponsesForm(@PathVariable Long id, Model model){
        Cours devoir = coursRepository.getOne(id);
        Response reponse = new Response();
        model.addAttribute("devoir",devoir);
        model.addAttribute("reponse",reponse);
        return "parent/reponse";
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
        return "redirect:/parent/reponses/lists/"+(Long)request.getSession().getAttribute("salleId")+"/"+compte.getUsername()+"/"+(Long)request.getSession().getAttribute("ecoleId");
    }

    @GetMapping("/examens/lists/{id}")
    public String examens(@PathVariable Long id, Model model){
        Salle salle = salleRepository.getOne(id);
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Examen> examens = new ArrayList<>();
        if (salles.contains(salle)) {
            examens = examenRepository.findAllBySalle(salle.getNiveau()+""+salle.getId());
        }else {
            model.addAttribute("error","Vous n'avez aucune classe avec ce nom dans cet établissement");
        }
        model.addAttribute("lists",examens);
        model.addAttribute("classe",salle);
        return "parent/examens";
    }

    @Autowired
    private HebdoRepository hebdoRepository;

    @GetMapping("/eleves/lists/{id}")
    public String presences(@PathVariable Long id,  Model model, HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Parent parent = compte.getParent();
        Salle salle = salleRepository.getOne(id);

        Collection<Hebdo> hebdos = hebdoRepository.findAllBySalle_Id(salle.getId(),Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("classe",salle);
        model.addAttribute("lists",hebdos);
        return "parent/hebdos";
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
        return "parent/hebdo";
    }

    @GetMapping("/presence/detail/{id}")
    public String presenceNew(Model model, @PathVariable Long id, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Parent parent = compte.getParent();
        Hebdo hebdo = hebdoRepository.getOne(id);
        Collection<Eleve> eleves = eleveRepository.findAllBySalle_IdAndParent_Id(hebdo.getSalle().getId(),parent.getId());


        model.addAttribute("lists",eleves);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("classe",hebdo.getSalle());
        return "parent/presence";

    }

    @GetMapping("/presence/eleve/detail/{eleveId}/{id}")
    public String presenceDetail(Model model, @PathVariable Long id,@PathVariable Long eleveId, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Parent parent = compte.getParent();
        Hebdo hebdo = hebdoRepository.getOne(id);
        Eleve eleve = eleveRepository.getOne(eleveId);
        Collection<Presence> presences = presenceRepository.findAllByEleve_IdAndHebdo_Id(eleve.getId(),hebdo.getId());

        model.addAttribute("lists",presences);
        model.addAttribute("eleve",eleve);
        model.addAttribute("hebdo",hebdo);
        model.addAttribute("classe",hebdo.getSalle());
        return "parent/presenceDetail";

    }

    @GetMapping("/activate/planning/{id}")
    public String activatePlan(@PathVariable Long id){
        Planning planning = planningRepository.getOne(id);
        planning.getValidations().add(true);
        planningRepository.save(planning);
        Hebdo hebdo = planning.getHebdo();
        Salle salle = hebdo.getSalle();
        return "redirect:/parent/hebdo/detail/"+hebdo.getId();
    }


    @GetMapping("/access-denied")
    public String access_denied(){
        return "parent/access-denied";
    }

    //----- messages methods -----/

    @Autowired
    private MessageRepository messageRepository;

    @PostMapping("/message/save/{id}")
    public String saveMessage(Message message, @RequestParam("file") MultipartFile file, @PathVariable Long id, HttpServletRequest request){

        Principal principal = request.getUserPrincipal();
        Compte compte = compteService.findByUsername(principal.getName());
        Salle salle = salleRepository.getOne(id);
        message.setCompte(compte);
        System.out.println(compte.getUsername());
        message.setSender(compte.getUsername());
        message.setSalle(salle.getNiveau()+""+salle.getId());
        message.setDate(new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        System.out.println(message.getVisibilite().toString());
        message.setVisibilite(message.getVisibilite().toString());
       multipart.store(file);
       message.setFichier("/upload-dir/"+file.getOriginalFilename());


        messageRepository.save(message);
        Mail sender = new Mail();

        sender.sender(
                compte.getEmail(),
                "Envoi d'un message",
                "Message de  ---> "+message.getContent()+", envoye le "+message.getDate()+", fichier associe(s) "+message.getFichier()+"avec un visibilite ----> "+message.getVisibilite());

        return "redirect:/parent/classe/detail/"+salle.getId();

    }

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @GetMapping("/account/detail/{id}")
    public String getAccount(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        model.addAttribute("compteDto", new CompteRegistrationDto());
        return "parent/account";
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
                return "redirect:/parent/account/detail/"+compte.getId();
            }
        }

        compte.setEncode(compteRegistrationDto.getPassword());
        compteRepository.save(compte);

        return "redirect:/parent/account/detail/"+compte.getId();

    }



}
