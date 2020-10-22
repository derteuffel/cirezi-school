package com.derteuffel.school.controllers;

import com.derteuffel.school.repositories.EcoleRepository;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by user on 23/03/2020.
 */
@Controller
@RequestMapping("/ecole")
public class EcoleController {

    @Autowired
    private EcoleRepository ecoleRepository;

    @Autowired
    private CompteService compteService;

    /*@Value("${file.upload-dir}")
    private  String fileStorage ;*/ //=System.getProperty("user.dir")+"/src/main/resources/static/downloadFile/";

    @Autowired
    private Multipart multipart;


    /*@PostMapping("/save")
    public String save(EcoleFormHelper formHelper, Model model, HttpServletRequest request, @RequestParam("file") MultipartFile file){
        Ecole ecole = new Ecole();
        Compte compte = new Compte();
        ecole.setAvenue(formHelper.getAvenue().toUpperCase());
        ecole.setCommune(formHelper.getCommune().toUpperCase());
        ecole.setCountry(formHelper.getCountry().toUpperCase());
        ecole.setCycle(formHelper.getCycle().toUpperCase());
        ecole.setCategory(formHelper.getCategory());
        ecole.setMatricule(formHelper.getMatricule().toUpperCase());
        ecole.setName(formHelper.getName().toUpperCase());
        ecole.setNumParcelle(formHelper.getNumParcelle().toUpperCase());
        ecole.setProvince(formHelper.getProvince().toUpperCase());
        ecole.setQuartier(formHelper.getQuartier().toUpperCase());
        ecole.setStatus(false);
        ecole.setCode(UUID.randomUUID().toString());
        ecole.setTerms(formHelper.getTerms());
        multipart.store(file);
        ecole.setLogo("/upload-dir/"+file.getOriginalFilename());
        ecoleRepository.save(ecole);
        System.out.println(formHelper.getPassword()+" "+formHelper.getConfirmPassword()+" "+formHelper.getTerms());
        if (!(formHelper.getPassword().equals(formHelper.getConfirmPassword())) || formHelper.getTerms() == false){
            model.addAttribute("error","Le mot de passe et la confirmation du mot de passe ne correspondent pas");
            return "index1";
        }else {
            compteService.save(formHelper.getEmail(), formHelper.getPassword(), formHelper.getUsername(), file.getOriginalFilename(), ecole.getId());


            Mail sender = new Mail();
            sender.sender(
                    "confirmation@yesbanana.org",
                    "Ecole Yesbanana: Notification Creation d'une ecole par:"+formHelper.getUsername(),
                    "Nom : " + ecole.getName() + ", Province : " + ecole.getProvince() + ", Commune : " + ecole.getCommune() + ", Cycle : " + ecole.getCycle() + ", Matricule : " + ecole.getMatricule() + ", et le code généré que vous allez communiquer au directeur pour valider son compte ---> " + ecole.getCode() +
                            " sur la plateforme ecoles.yesbanana.org. Veuillez vous connectez pour l'envoyer son code de confirmation. bien vouloir communiquer le code suivant a l'adresse mail: " + formHelper.getEmail() + " code: " + ecole.getCode()
            );

            request.getSession().setAttribute("item", ecole);
            model.addAttribute("ecole", ecole);
            model.addAttribute("success", "Votre école a été ajouté avec succès, veuillez contacter les administrateurs du site pour vous fournir le code de validation de votre école pour continuer à l'adresse");
            return "index";
        }
    }*/


}
