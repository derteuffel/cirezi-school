package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

/**
 * Created by user on 23/03/2020.
 */
@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private EcoleRepository ecoleRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private EleveRepository eleveRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private CompteService compteService;

    @Autowired
    private Multipart multipart;
    /*@Value("${file.upload-dir}")
    private  String fileStorage ;*///=System.getProperty("user.dir")+"/src/main/resources/static/downloadFile/";

    @GetMapping("/login")
    public String director(){
        return "admin/login";
    }

    @GetMapping("/registration")
    public String registrationForm(Model model){
        return "admin/registration";
    }


   /* @PostMapping("/registration")
    public String registrationDirectionSave(@ModelAttribute("compte") @Valid CompteRegistrationDto compteDto,
                                            BindingResult result, RedirectAttributes redirectAttributes, Model model, String ecole){

        Compte existAccount = compteService.findByUsername(compteDto.getUsername());
        if (existAccount != null){
            result.rejectValue("username", null, "Il existe deja un compte avec ce nom d'utilisateur vueillez choisir un autre");
            model.addAttribute("error","Il existe deja un compte avec ce nom d'utilisateur vueillez choisir un autre");
        }

        if (result.hasErrors()) {
            return "admin/registration";
        }

        Ecole ecole1 = ecoleRepository.getOne(Long.parseLong(ecole));

        compteService.save(compteDto,"/images/icon/avatar-01.jpg",ecole1.getId());
        redirectAttributes.addFlashAttribute("success", "Votre enregistrement a ete effectuer avec succes");
        return "redirect:/admin/login";
    }*/

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        System.out.println("je suis deconnectee");
        return "redirect:/admin/login";
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
        model.addAttribute("livre",new Livre());
        model.addAttribute("lists",alls);

        return "admin/bibliotheques";
    }

    @PostMapping("/livre/save")
    public String saveBook(@Valid Livre livre, @RequestParam("file") MultipartFile file, @RequestParam("cover") MultipartFile cover){
        multipart.store(file);
        livre.setFichier("/upload-dir/"+file.getOriginalFilename());
        multipart.store(cover);
        livre.setCouverture("/upload-dir/"+cover.getOriginalFilename());

        livreRepository.save(livre);
        return "redirect:/admin/bibliotheque/lists";
    }

    @GetMapping("/livre/edit/{id}")
    public String updateLivre(@PathVariable Long id, Model model){
        Livre livre = livreRepository.getOne(id);
        model.addAttribute("livre",livre);
        return "admin/updateBibliotheque";
    }

    @GetMapping("/livre/delete/{id}")
    public String deleteLivre(@PathVariable Long id){
        livreRepository.deleteById(id);
        return "redirect:/admin/bibliotheque/lists";
    }


    @GetMapping("/ecoles/lists")
    public String getAllSchools(Model model){

        List<Ecole> lists = ecoleRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("lists",lists);
        return "admin/ecoles";
    }

    @GetMapping("/ecole/enseignants")
    public String getAllEnseignants(Model model){
        List<Enseignant> lists = new ArrayList<>();
        Collection<Salle> salles = salleRepository.findAll();
        for (Salle salle : salles){
            lists.addAll(enseignantRepository.findAllBySalles_Id(salle.getId()));
        }

        model.addAttribute("lists",lists);
        return "admin/enseignants";

    }
    @GetMapping("/ecole/classes")
    public String getAllSalles( Model model){
        Collection<Salle> lists = salleRepository.findAll();
        model.addAttribute("lists",lists);
        return "admin/classes";

    }
    @GetMapping("/ecole/eleves")
    public String getAllEleves( Model model){
        List<Eleve> lists = new ArrayList<>();
        Collection<Salle> salles = salleRepository.findAll();
        for (Salle salle : salles){
            lists.addAll(eleveRepository.findAllBySalle_Id(salle.getId()));
        }

        model.addAttribute("lists",lists);
        return "admin/eleves";

    }

    @GetMapping("/ecole/parents")
    public String getAllParents( Model model){
        List<Eleve> lists = new ArrayList<>();
        List<Parent> parents = new ArrayList<>();
        Collection<Salle> salles = salleRepository.findAll();
        for (Salle salle : salles){
            lists.addAll(eleveRepository.findAllBySalle_Id(salle.getId()));
        }

        for (Eleve eleve : lists){
            parents.add(eleve.getParent());
        }

        model.addAttribute("lists",parents);
        return "admin/parents";



    }

    @GetMapping("/ecole/accounts/parents")
    public String getAllAccounts(@PathVariable Long id, Model model) {
        List<Eleve> lists = new ArrayList<>();
        List<Parent> parents = new ArrayList<>();
        List<Compte> accounts = new ArrayList<>();
        Collection<Salle> salles = salleRepository.findAll();
        for (Salle salle : salles) {
            lists.addAll(eleveRepository.findAllBySalle_Id(salle.getId()));
        }

        for (Eleve eleve : lists) {
            parents.add(eleve.getParent());
        }

        for (Parent parent : parents){
            for (Compte compte : compteRepository.findAll()){
                if (compte.getParent() != null) {
                    if (compte.getParent().getId() == parent.getId()) {
                        accounts.add(compte);
                    }
                }
            }
        }

        model.addAttribute("lists", accounts);
        return "admin/accounts";
    }

    @GetMapping("/generate/activation/{id}")
    public String generateBibliothequeCode(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        model.addAttribute("compte",compte);
        return "admin/generate";
    }


    @GetMapping("/active/{id}")
    public String active(@PathVariable Long id, @RequestParam("expireDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expireDate){

        System.out.println(expireDate);
        Compte compte = compteRepository.getOne(id);
        String randomCode = "" + UUID.randomUUID().toString();
        compte.setBibliothequeCode(randomCode);
        compteRepository.save(compte);
        TimerTask deactivate = new TimerTask() {
            @Override
            public void run() {
                compte.setBibliothequeCode(null);
                compte.setStatus(false);
                compteRepository.save(compte);
                System.out.println("job is done");
            }
        };

        Timer timer = new Timer();
        timer.schedule(deactivate,expireDate);

        return "redirect:/admin/bibliotheque/lists";
    }
}
