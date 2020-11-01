package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.Compte;
import com.derteuffel.school.entities.Encadreur;
import com.derteuffel.school.enums.ECategory;
import com.derteuffel.school.helpers.EcoleFormHelper;
import com.derteuffel.school.repositories.CompteRepository;
import com.derteuffel.school.repositories.EcoleRepository;
import com.derteuffel.school.repositories.EncadreurRepository;
import com.derteuffel.school.services.MailService;
import com.derteuffel.school.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


/**
 * Created by user on 22/03/2020.
 */

@Controller
public class HomeController {

    public static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }

    @Autowired
    private EcoleRepository ecoleRepository;
    @Autowired
    private CompteRepository compteRepository;
    @Autowired
    private EncadreurRepository encadreurRepository;

    @Autowired
    private StorageService storageService;


    @GetMapping("/home/direction")
    public String home(){
        return "index1";
    }


    @GetMapping("/login/admin")
    public String admin(){
        return "login/admin";
    }

    @GetMapping("/getConferenceid/{userId}")
    @ResponseBody
    public HashMap<String, String> getConferenceId(@PathVariable String userId){
        HashMap<String,String> conferenceId = new HashMap<String,String>();
       Compte compte= compteRepository.getOne(Long.parseLong(userId));

       conferenceId.put("conferenceId",compte.getConferenceId());
       return conferenceId;
    }
    @GetMapping("/subscriberId/{subscriberId}")
    public String setSubscriberId(@PathVariable String subscriberId, HttpServletRequest request){
        Compte compte = (Compte)request.getSession().getAttribute("compte");
        compte.setSubscriberId(subscriberId);
        compteRepository.save(compte);
        return "index1";
    }
    @GetMapping("/sendMail/{sender}/{conferenceId}")
    public String sendMail(@PathVariable String sender,@PathVariable String conferenceId,HttpServletRequest request) throws IOException {
        Compte compte = compteRepository.findByEnseignant_Id(Long.parseLong(sender));
        System.out.println(compte);
        if(compte==null)
        {
             compte = compteRepository.findByParent_Id(Long.parseLong(sender));
             if(compte==null){

                     compte = compteRepository.findByEnfant_Id(Long.parseLong(sender));
                 if(compte==null){

                 compte = compteRepository.getOne(Long.parseLong(sender));
                 }

             }
        }
        Compte session = (Compte) request.getSession().getAttribute("compte");
        URL url = null;
        try {
            url = new URL("https://api.pushalert.co/rest/v1/send");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "api_key=4221ef49256f52a64cfb265b6cba4031");
            Map<String, String> parameters = new HashMap<>();
            parameters.put("title", "Yesbanana School");
            parameters.put("url","https://ecoles.yesbanana.org");
            parameters.put("message","Appel de " + session.getUsername() + " Connectez vous pour répondre.");
            parameters.put("icon","https://ecoles.yesbanana.org/images/logo1.png");
            parameters.put("subscriber",compte.getSubscriberId());
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
           System.out.println(con.getResponseCode());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        compte.setConferenceId(conferenceId);
        compteRepository.save(compte);
        MailService mailService = new MailService();
        mailService.sendSimpleMessage(
                compte.getEmail(),
                "YesBanana School: VideoCall live",
                "Go to your profile at https://ecoles.yesbanana.org, to join the call");
        return "index1";
    }
    @GetMapping("/planning/{sender}")
    public String planning(@PathVariable String sender, @RequestParam String date){
        MailService mailService = new MailService();
        mailService.sendSimpleMessage(
                sender,
                "YesBanana School: Reunion du " + date ,
                "Allez a votre profil, a l'adresse:  https://ecoles.yesbanana.org, pour participer a la " +
                        "reunion du " + date);
        return "index1";
    }


    @GetMapping("/login/parent")
    public String parent(){
        return "login/parent";
    }

    @GetMapping("/login/enseignant")
    public String enseignant(){
        return "login/enseignant";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    @GetMapping("/experts/ecoles")
    public String getExpert1(Model model){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Expert_YesB_primaire.toString(),Sort.by(Sort.Direction.DESC,"id"));
        encadreurs.addAll(encadreurRepository.findAllByCategory(ECategory.Expert_YesB_secondaire.toString(),Sort.by(Sort.Direction.DESC,"id")));
        List<String> matieres = new ArrayList<>();
        for (Encadreur encadreur : encadreurs){
            matieres.addAll(encadreur.getCour_enseigner());
        }
        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("lists",encadreurs);
        model.addAttribute("name","ecoles");
        return "expertsProfiles";
    }
    @GetMapping("/experts/stages")
    public String getExpert4(Model model){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Expert_YesB_en_stage_professionnel.toString(),Sort.by(Sort.Direction.DESC,"id"));
        List<String> matieres = new ArrayList<>();
        for (Encadreur encadreur : encadreurs){
            matieres.addAll(encadreur.getCour_enseigner());
        }
        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("lists",encadreurs);
        model.addAttribute("name","stages");
        return "expertsProfiles";
    }

    @GetMapping("/experts/ecoles/{matiere}")
    public String getExpert1Matieres(Model model,@PathVariable String matiere){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Expert_YesB_primaire.toString(),Sort.by(Sort.Direction.DESC,"id"));
        encadreurs.addAll(encadreurRepository.findAllByCategory(ECategory.Expert_YesB_secondaire.toString(),Sort.by(Sort.Direction.DESC,"id")));
        Collection<Encadreur> lists = new ArrayList<>();
        List<String> matieres = new ArrayList<>();
        for (Encadreur encadreur : encadreurs){
            if (encadreur.getCour_enseigner().contains(matiere.toString())){
                lists.add(encadreur);
            }
            matieres.addAll(encadreur.getCour_enseigner());
        }
        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("name","ecoles");
        model.addAttribute("lists",lists);
        return "expertsProfiles";
    }

    @GetMapping("/experts/universites")
    public String getExpert2(Model model){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Appui_redaction_travail_de_fin_de_cycle.toString(),Sort.by(Sort.Direction.DESC,"id"));
        List<String> matieres = new ArrayList<>();
        for (Encadreur encadreur : encadreurs){
            matieres.addAll(encadreur.getCour_enseigner());
        }
        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("lists",encadreurs);
        model.addAttribute("name","universite");
        return "expertsProfiles";
    }

    @GetMapping("/experts/universites/{matiere}")
    public String getExpert2Matieres(Model model,@PathVariable String matiere){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Appui_redaction_travail_de_fin_de_cycle.toString(),Sort.by(Sort.Direction.DESC,"id"));
        Collection<Encadreur> lists = new ArrayList<>();
        List<String> matieres = new ArrayList<>();

        for (Encadreur encadreur : encadreurs){
            if (encadreur.getCour_enseigner().contains(matiere.toString())){
                lists.add(encadreur);
            }
            matieres.addAll(encadreur.getCour_enseigner());
        }
        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("name","universite");
        model.addAttribute("lists",lists);
        return "expertsProfiles";
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

    @GetMapping("/experts/professionnels")
    public String getExpert3(Model model){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Expert_YesB_en_formation_professionnelle.toString(),Sort.by(Sort.Direction.DESC,"id"));
        List<String> matieres = new ArrayList<>();
        for (Encadreur encadreur : encadreurs){
            matieres.addAll(encadreur.getCour_enseigner());
        }
        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("lists",encadreurs);
        model.addAttribute("name","professionnelle");
        return "expertsProfiles";
    }

    @GetMapping("/experts/professionnels/{matiere}")
    public String getExpert3Matieres(Model model,@PathVariable String matiere){

        Collection<Encadreur> encadreurs = encadreurRepository.findAllByCategory(ECategory.Expert_YesB_en_formation_professionnelle.toString(),Sort.by(Sort.Direction.DESC,"id"));
        Collection<Encadreur> lists = new ArrayList<>();
        List<String> matieres = new ArrayList<>();
        for (Encadreur encadreur : encadreurs){
            if (encadreur.getCour_enseigner().contains(matiere)){
                lists.add(encadreur);
            }
            matieres.addAll(encadreur.getCour_enseigner());
        }

        model.addAttribute("matieres",removeDuplicates(matieres));
        model.addAttribute("name","professionnelle");
        model.addAttribute("lists",encadreurs);
        return "expertsProfiles";
    }

    @GetMapping("/prices")
    public String priceDescription(){
        return "prices";
    }
}
