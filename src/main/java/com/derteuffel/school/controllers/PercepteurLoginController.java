package com.derteuffel.school.controllers;

import com.derteuffel.school.entities.*;
import com.derteuffel.school.enums.EMois;
import com.derteuffel.school.enums.ERole;
import com.derteuffel.school.enums.EType;
import com.derteuffel.school.enums.EVisibilite;
import com.derteuffel.school.helpers.CompteRegistrationDto;
import com.derteuffel.school.helpers.SalaireHelper;
import com.derteuffel.school.repositories.*;
import com.derteuffel.school.services.CompteService;
import com.derteuffel.school.services.Mail;
import com.derteuffel.school.services.Multipart;
import com.derteuffel.school.services.Printer;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

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
    private CaisseRepository caisseRepository;

    @Autowired
    private MouvementRepository mouvementRepository;

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

        Collection<Eleve> eleves = eleveRepository.findAllBySalle_Id(salle.getId());
        Collection<Paiement> paiements = new ArrayList<>();
        for (Eleve eleve : eleves){
            System.out.println(eleve.getName());
            paiements.add(paiementRepository.findByEleve_Id(eleve.getId()));
        }
        model.addAttribute("classe", salle);
        model.addAttribute("lists", paiements);
        return "percepteur/classes/paiements";
    }


    @GetMapping("/add/montant")
    public String addMontant(String montant, String niveau, String categorie){
        Collection<Salle> salles = salleRepository.findAll();
        Collection<Eleve> eleves = new ArrayList<>();
        for (Salle salle : salles){
            System.out.println("je suis la!");
            if (salle.getNiveau().contains(niveau)){
                System.out.println("je suis ici dedans");
                for (Eleve eleve : eleveRepository.findAllByCategorieAndSalle_Id(categorie, salle.getId())){
                    System.out.println(eleveRepository.findAllByCategorieAndSalle_Id(categorie, salle.getId()).size());
                    Paiement paiement = paiementRepository.findByEleve_Id(eleve.getId());
                    paiement.setCoutTotal(Double.parseDouble(montant));
                    paiementRepository.save(paiement);
                    System.out.println("et la cest la fin ");
                }
            }
        }
        return "redirect:/percepteur/home";
    }

    @GetMapping("/classe/eleves/add/account/{id}")
    public String addPaiement(String motif, @PathVariable Long id, String montant){
        Paiement paiement = paiementRepository.getOne(id);
        if (motif.equals("premier")){
            paiement.setAccountTrimestrePremier(Double.parseDouble(montant));
        }else if (motif.equals("deuxieme")){
            paiement.setAccountTrimestreSecond(Double.parseDouble(montant));
        }else if (motif.equals("troisieme")){
            paiement.setAccountTrimestreTroisieme(Double.parseDouble(montant));
        }else if (motif.equals("sport")){
            paiement.setAccountSport(Double.parseDouble(montant));
        }else {
            paiement.setAccountBibliotheque(Double.parseDouble(montant));
        }
        if (paiement.getTotalPayer() != null) {
            paiement.setTotalPayer(paiement.getTotalPayer() + Double.parseDouble(montant));
            paiement.setSolde(paiement.getCoutTotal() - paiement.getTotalPayer());
        }else {
            paiement.setTotalPayer(Double.parseDouble(montant));
            paiement.setSolde(paiement.getCoutTotal()-Double.parseDouble(montant));
        }
        paiementRepository.save(paiement);
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        SimpleDateFormat formatMois = new SimpleDateFormat("MM");
        String annee = format.format(new Date());
        String mois = formatMois.format(new Date());
        Caisse caisse = caisseRepository.findByStatus(true);
        Mouvement mouvement = new Mouvement();
        if(caisse != null){

            mouvement.setType("ENTREE");
            mouvement.setNumMouvement((mouvementRepository.findAllByCaisse_Id(caisse.getId()).size()+1)+"");
            mouvement.setCaisse(caisse);
            mouvement.setLibelle("Versement Montant : "+motif);
            mouvement.setMontant(Double.parseDouble(montant));
            mouvement.setSoldeFin(caisse.getSoldeFinMois() + mouvement.getMontant());
            mouvementRepository.save(mouvement);
            caisse.setMouvementMensuel(caisse.getMouvementMensuel() + mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement.getSoldeFin());
            caisseRepository.save(caisse);
        }else {
            Caisse caisse1 = new Caisse();
            caisse1.setMouvementMensuel(Double.parseDouble(montant));
            caisse1.setSoldeFinMois(Double.parseDouble(montant));
            caisse1.setSoldeDebutmois(Double.parseDouble(montant));
            caisse1.setStatus(true);
            if (Integer.parseInt(mois) == 01){
                caisse1.setMois(EMois.Janvier.toString());
            }else if (Integer.parseInt(mois) == 02){
                caisse1.setMois(EMois.Fevrier.toString());
            }else if (Integer.parseInt(mois) == 03){
                caisse1.setMois(EMois.Mars.toString());
            }else if (Integer.parseInt(mois) == 04){
                caisse1.setMois(EMois.Avril.toString());
            }else if (Integer.parseInt(mois) == 05){
                caisse1.setMois(EMois.Mai.toString());
            }else if (Integer.parseInt(mois) == 06){
                caisse1.setMois(EMois.Juin.toString());
            }else if (Integer.parseInt(mois) == 07){
                caisse1.setMois(EMois.Juillet.toString());
            }else if (Integer.parseInt(mois) == 8){
                caisse1.setMois(EMois.Aout.toString());
            }else if (Integer.parseInt(mois) == 9){
                caisse1.setMois(EMois.Septembre.toString());
            }else if (Integer.parseInt(mois) == 10){
                caisse1.setMois(EMois.Octobre.toString());
            }else if (Integer.parseInt(mois) == 11){
                caisse1.setMois(EMois.Novembre.toString());
            }else {
                caisse1.setMois(EMois.Decembre.toString());
            }
            caisse1.setAnnee(Integer.parseInt(annee));
            caisseRepository.save(caisse1);
            mouvement.setSoldeFin(caisse1.getSoldeFinMois());
            mouvement.setMontant(Double.parseDouble(montant));
            mouvement.setLibelle("Versement Montant : "+motif);
            mouvement.setCaisse(caisse1);
            mouvement.setNumMouvement((mouvementRepository.findAllByCaisse_Id(caisse1.getId()).size()+1)+"");
            mouvement.setType("ENTREE");
            mouvementRepository.save(mouvement);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
        String printerName = "Canon iR-ADV C5535/5540 UFR II";

        String filePath = location+mouvement.getNumMouvement()+"_"+sdf.format(mouvement.getCreatedDate())+".pdf";
        Document document = new Document(PageSize.A5, 5, 5, 5, 5);
        try{
            PdfWriter.getInstance(document,new FileOutputStream(new File((filePath).toString())));
            document.open();
            Paragraph para1 = new Paragraph("LYCEE CIREZI");
            para1.setAlignment(Paragraph.ALIGN_CENTER);
            para1.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 4, Font.BOLD,
                    BaseColor.GREEN));
            para1.setSpacingAfter(10);
            document.add(para1);

            Paragraph paragraph = new Paragraph("Ecole Conventionnelle Catholique. \n");
            paragraph.setSpacingAfter(10);
            Paragraph paragraph1 = new Paragraph("B.P.  2276      Bukavu\n");
            paragraph1.setSpacingAfter(10);
            Paragraph paragraph2 = new Paragraph("SECOPE  :   6001299 \n");
            paragraph2.setSpacingAfter(10);
            Paragraph paragraph3 = new Paragraph("ARRETE MINISTERIEL N MINEPSP/CABMIN/0614/2005 DU 21/02/2005");
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph1.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph2.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph3.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph1.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph2.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph3.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            document.add(paragraph);
            document.add(paragraph1);
            document.add(paragraph2);
            document.add(paragraph3);
            Paragraph line = new Paragraph("----------------------------------------------------------------");
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);
            document.add(new Paragraph("Bon de caisse :   "+mouvement.getType().toUpperCase()));
            document.add(new Paragraph("Numero  :  "+mouvement.getNumMouvement()));
            document.add(new Paragraph("Nom et Prenom  :  "+paiement.getEleve().getName()+" "+paiement.getEleve().getPrenom()));
            document.add(new Paragraph("Classe  :  "+paiement.getEleve().getSalle().getNiveau()));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100.0f);
            table.setWidths(new float[] {3.0f, 2.0f,2.0f,2.0f});
            table.setSpacingBefore(10);
            table.setSpacingAfter(20f);
            table.setSpacingBefore(20f);
            // define font for table header row
            Font font = FontFactory.getFont(FontFactory.HELVETICA);
            font.setColor(BaseColor.WHITE);
            addTableHeader(table);
            table.addCell("1");
            table.addCell(mouvement.getLibelle());
            table.addCell("1");
            table.addCell(mouvement.getMontant().toString()+" $");
            System.out.println("inside the table");

            document.add(table);
            document.add(new Paragraph("Fait le  :  "+sdf1.format(new Date())));
            document.add(new Paragraph("Signature Caisse  "));
            document.close();
            System.out.println("the job is done!!!");

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Printer printer = new Printer();
        printer.print(filePath,printerName);
        return "redirect:/percepteur/classe/eleves/payment/lists/"+paiement.getEleve().getSalle().getId();
    }

    @GetMapping("/enseignants/lists")
    public String getEnseignants(Model model){
        Collection<Enseignant> enseignants = enseignantRepository.findAll();
        model.addAttribute("lists", enseignants);
        return "percepteur/enseignants";
    }

    @GetMapping("/staffs/lists")
    public String getStaffs(Model model){
        Collection<Compte> staffs = compteRepository.findAllByType(EType.PREFET.toString());
        staffs.addAll(compteRepository.findAllByType(EType.SECRETAIRE.toString()));
        staffs.addAll(compteRepository.findAllByType(EType.DIRECTEUR_ETUDE.toString()));
        staffs.addAll(compteRepository.findAllByType(EType.DIRECTEUR_DISCIPLINE.toString()));
        staffs.addAll(compteRepository.findAllByType(EType.PERCEPTEUR.toString()));
        model.addAttribute("lists", staffs);
        return "percepteur/staffs";
    }

    @Autowired
    private SalaireRepository salaireRepository;

    @GetMapping("/enseignants/salaires/lists/{id}")
    public String getEnseignantSalaire(@PathVariable Long id, Model model){
        Enseignant enseignant = enseignantRepository.getOne(id);
        Collection<Salaire> salaires = salaireRepository.findAllByEnseignant_Id(enseignant.getId());

        model.addAttribute("lists", salaires);
        model.addAttribute("enseignant",enseignant);
        model.addAttribute("salaire", new SalaireHelper());
        return "percepteur/salairesEnseignant";
    }

    @GetMapping("/staffs/salaires/lists/{id}")
    public String getStaffSalaire(@PathVariable Long id, Model model){
        Compte compte = compteRepository.getOne(id);
        Collection<Salaire> salaires = salaireRepository.findAllByCompte_Id(compte.getId());

        model.addAttribute("lists", salaires);
        model.addAttribute("compte",compte);
        model.addAttribute("salaire", new SalaireHelper());
        return "percepteur/salairesStaff";
    }


    @PostMapping("/enseignants/salaire/save/{id}")
    public String saveBulletin(@PathVariable Long id, SalaireHelper salaireHelper, RedirectAttributes redirectAttributes,
                               String montant, String housing, String allocFamille, String allocTransport,
                               String avanceSalaire){

        Enseignant enseignant = enseignantRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        salaireHelper.setSalaireBase(Double.parseDouble(montant));
        salaireHelper.setHousing(Double.parseDouble(housing));
        salaireHelper.setAllocationFamilliale(Double.parseDouble(allocFamille));
        salaireHelper.setAllocationTransport(Double.parseDouble(allocTransport));
        salaireHelper.setAvanceSalaire(Double.parseDouble(avanceSalaire));
        Salaire salaire = new Salaire();
        salaire.setAllocationFamilliale(salaireHelper.getAllocationFamilliale());
        salaire.setAllocationTransport(salaireHelper.getAllocationTransport());
        salaire.setAvanceSalaire(salaireHelper.getAvanceSalaire());
        salaire.setDatePaiement(sdf.format(salaireHelper.getDatePaiement()));
        salaire.setMois(salaireHelper.getMois());
        salaire.setHousing(salaireHelper.getHousing());
        salaire.setEnseignant(enseignant);
        salaire.setSalaireBase(salaireHelper.getSalaireBase());
        salaire.setNumBuletin((salaireRepository.findAllByEnseignant_Id(enseignant.getId()).size()+1));
        salaire.setSalaireBrut(salaire.getAllocationFamilliale()+salaire.getAllocationTransport()+salaire.getHousing()+salaire.getSalaireBase());
        salaire.setCafeteriat(5.0);
        salaire.setRemboursementMensuelle(0.0);
        salaire.setTaxe(salaire.getSalaireBase()*0.1);
        salaire.setCnss(salaire.getSalaireBrut()*0.035);
        salaire.setMutuelleSante(salaire.getSalaireBrut()*0.05);
        salaire.setNetPaie(salaire.getSalaireBrut() - (salaire.getCnss()+salaire.getMutuelleSante()+salaire.getCafeteriat()+salaire.getRemboursementMensuelle()));
        salaire.setSalaireNet(salaire.getNetPaie() - salaire.getAvanceSalaire());
        salaireRepository.save(salaire);
        String annee = format.format(new Date());
        Caisse caisse = caisseRepository.findByAnneeAndMois(Integer.parseInt(annee),salaireHelper.getMois());
        if(caisse != null){
            Mouvement mouvement = new Mouvement();
            mouvement.setType("SORTIE");
            mouvement.setNumMouvement((mouvementRepository.findAllByCaisse_Id(caisse.getId()).size()+1)+"");
            mouvement.setCaisse(caisse);
            mouvement.setLibelle("Paiement Salaire d'un staff");
            mouvement.setMontant(salaire.getSalaireNet());
            mouvement.setSoldeFin(caisse.getSoldeFinMois() - mouvement.getMontant());
            mouvementRepository.save(mouvement);
            caisse.setMouvementMensuel(caisse.getMouvementMensuel() - mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement.getSoldeFin());
            caisseRepository.save(caisse);
        }else {
            Caisse caisse1 = new Caisse();
            caisse1.setMouvementMensuel(0.0 - salaire.getSalaireNet());
            caisse1.setSoldeFinMois(0.0 - salaire.getSalaireNet());
            caisse1.setSoldeDebutmois(0.0 - salaire.getSalaireNet());
            caisse1.setStatus(true);
            caisse1.setMois(salaireHelper.getMois());
            caisse1.setAnnee(Integer.parseInt(annee));
            caisseRepository.save(caisse1);
            Mouvement mouvement = new Mouvement();
            mouvement.setSoldeFin(caisse1.getSoldeFinMois());
            mouvement.setMontant(salaire.getSalaireNet());
            mouvement.setLibelle("Paiement Salaire d'un staff");
            mouvement.setCaisse(caisse1);
            mouvement.setNumMouvement((mouvementRepository.findAllByCaisse_Id(caisse1.getId()).size()+1)+"");
            mouvement.setType("SORTIE");
            mouvementRepository.save(mouvement);
        }
        redirectAttributes.addFlashAttribute("message","Vous avez ajouter avec succes un nouveau bulletin de paie");
        return "redirect:/percepteur/enseignants/salaires/lists/"+enseignant.getId();
    }

    @PostMapping("/staffs/salaire/save/{id}")
    public String saveBulletinStaff(@PathVariable Long id, SalaireHelper salaireHelper, RedirectAttributes redirectAttributes,
                                    String montant, String housing, String allocFamille, String allocTransport,
                                    String avanceSalaire){

        Compte compte = compteRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        salaireHelper.setSalaireBase(Double.parseDouble(montant));
        salaireHelper.setHousing(Double.parseDouble(housing));
        salaireHelper.setAllocationFamilliale(Double.parseDouble(allocFamille));
        salaireHelper.setAllocationTransport(Double.parseDouble(allocTransport));
        salaireHelper.setAvanceSalaire(Double.parseDouble(avanceSalaire));
        Salaire salaire = new Salaire();
        salaire.setAllocationFamilliale(salaireHelper.getAllocationFamilliale());
        salaire.setAllocationTransport(salaireHelper.getAllocationTransport());
        salaire.setAvanceSalaire(salaireHelper.getAvanceSalaire());
        salaire.setDatePaiement(sdf.format(salaireHelper.getDatePaiement()));
        salaire.setMois(salaireHelper.getMois());
        salaire.setHousing(salaireHelper.getHousing());
        salaire.setCompte(compte);
        salaire.setSalaireBase(salaireHelper.getSalaireBase());
        salaire.setNumBuletin((salaireRepository.findAllByCompte_Id(compte.getId()).size()+1));
        salaire.setSalaireBrut(salaire.getAllocationFamilliale()+salaire.getAllocationTransport()+salaire.getHousing()+salaire.getSalaireBase());
        salaire.setCafeteriat(5.0);
        salaire.setRemboursementMensuelle(0.0);
        salaire.setTaxe(salaire.getSalaireBase()*0.1);
        salaire.setCnss(salaire.getSalaireBrut()*0.035);
        salaire.setMutuelleSante(salaire.getSalaireBrut()*0.05);
        salaire.setNetPaie(salaire.getSalaireBrut() - (salaire.getCnss()+salaire.getMutuelleSante()+salaire.getCafeteriat()+salaire.getRemboursementMensuelle()));
        salaire.setSalaireNet(salaire.getNetPaie() - salaire.getAvanceSalaire());
        salaireRepository.save(salaire);
        String annee = format.format(new Date());
        Caisse caisse = caisseRepository.findByAnneeAndMois(Integer.parseInt(annee),salaireHelper.getMois());
        if(caisse != null){
            Mouvement mouvement = new Mouvement();
            mouvement.setType("SORTIE");
            mouvement.setNumMouvement((mouvementRepository.findAllByCaisse_Id(caisse.getId()).size()+1)+"");
            mouvement.setCaisse(caisse);
            mouvement.setLibelle("Paiement Salaire d'un staff");
            mouvement.setMontant(salaire.getSalaireNet());
            mouvement.setSoldeFin(caisse.getSoldeFinMois() - mouvement.getMontant());
            mouvementRepository.save(mouvement);
            caisse.setMouvementMensuel(caisse.getMouvementMensuel() - mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement.getSoldeFin());
            caisseRepository.save(caisse);
        }else {
            Caisse caisse1 = new Caisse();
            caisse1.setMouvementMensuel(0.0 - salaire.getSalaireNet());
            caisse1.setSoldeFinMois(0.0 - salaire.getSalaireNet());
            caisse1.setSoldeDebutmois(0.0 - salaire.getSalaireNet());
            caisse1.setStatus(true);
            caisse1.setMois(salaireHelper.getMois());
            caisse1.setAnnee(Integer.parseInt(annee));
            caisseRepository.save(caisse1);
            Mouvement mouvement = new Mouvement();
            mouvement.setSoldeFin(caisse1.getSoldeFinMois());
            mouvement.setMontant(salaire.getSalaireNet());
            mouvement.setLibelle("Paiement Salaire d'un staff");
            mouvement.setCaisse(caisse1);
            mouvement.setNumMouvement((mouvementRepository.findAllByCaisse_Id(caisse1.getId()).size()+1)+"");
            mouvement.setType("SORTIE");
            mouvementRepository.save(mouvement);
        }
        redirectAttributes.addFlashAttribute("message","Vous avez ajouter avec succes un nouveau bulletin de paie");
        return "redirect:/percepteur/staffs/salaires/lists/"+compte.getId();
    }

    @GetMapping("/enseignants/salaire/detail/{id}")
    public String detailSalaire(@PathVariable Long id, Model model){
        Salaire salaire = salaireRepository.getOne(id);
        Enseignant enseignant = salaire.getEnseignant();
        model.addAttribute("salaire", salaire);
        model.addAttribute("enseignant",enseignant);
        return "percepteur/salairesDetail";
    }




    @GetMapping("/mouvements/lists")
    public String caisses(Model model){
        List<Caisse> caisses = caisseRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        if (caisses.size()== 0){
            Caisse caisse = new Caisse();
            caisse.setAnnee(2020);
            caisse.setMois(EMois.Novembre.toString());
            caisse.setMouvementMensuel(0.0);
            caisse.setSoldeDebutmois(0.0);
            caisse.setSoldeFinMois(0.0);
            caisse.setStatus(true);
            caisseRepository.save(caisse);
        }
        model.addAttribute("lists", caisses);
        return "percepteur/caisses";
    }

    @GetMapping("/mouvements/detail/{id}")
    public String caisseDetail(@PathVariable Long id, Model model){
        Caisse caisse = caisseRepository.getOne(id);
        model.addAttribute("caisse", caisse);
        Collection<Mouvement> mouvements = mouvementRepository.findAllByCaisse_Id(caisse.getId());
        model.addAttribute("lists",mouvements);
        model.addAttribute("mouvement", new Mouvement());
        return "percepteur/caisse";

    }

    @GetMapping("/mouvements/terminer/{id}")
    public String closeMonth(@PathVariable Long id,RedirectAttributes redirectAttributes){
        Caisse caisse = caisseRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String annee = sdf.format(new Date());
        caisse.setStatus(false);
        Caisse newCaisse = new Caisse();
        newCaisse.setStatus(true);
        newCaisse.setAnnee(Integer.parseInt(annee));
        newCaisse.setSoldeDebutmois(caisse.getSoldeFinMois());
        newCaisse.setSoldeFinMois(caisse.getSoldeFinMois());
        newCaisse.setMouvementMensuel(0.0);
        if (caisse.getMois().equals(EMois.Janvier.toString())){
            newCaisse.setMois(EMois.Fevrier.toString());
        }else if (caisse.getMois().equals(EMois.Fevrier.toString())){
            newCaisse.setMois(EMois.Mars.toString());
        }else if (caisse.getMois().equals(EMois.Mars.toString())){
            newCaisse.setMois(EMois.Avril.toString());
        }else if (caisse.getMois().equals(EMois.Avril.toString())){
            newCaisse.setMois(EMois.Mai.toString());
        }else if (caisse.getMois().equals(EMois.Mai.toString())){
            newCaisse.setMois(EMois.Juin.toString());
        }else if (caisse.getMois().equals(EMois.Juin.toString())){
            newCaisse.setMois(EMois.Juillet.toString());
        }else if (caisse.getMois().equals(EMois.Juillet.toString())){
            newCaisse.setMois(EMois.Aout.toString());
        }else if (caisse.getMois().equals(EMois.Aout.toString())){
            newCaisse.setMois(EMois.Septembre.toString());
        }else if (caisse.getMois().equals(EMois.Septembre.toString())){
            newCaisse.setMois(EMois.Octobre.toString());
        }else if (caisse.getMois().equals(EMois.Octobre.toString())){
            newCaisse.setMois(EMois.Novembre.toString());
        }else if (caisse.getMois().equals(EMois.Novembre.toString())){
            newCaisse.setMois(EMois.Decembre.toString());
        }
        caisseRepository.save(newCaisse);
        redirectAttributes.addFlashAttribute("message","Vous venez de cloturer le mois de : "+caisse.getMois()+" et vous etes au debut du mois de : "+newCaisse.getMois());
        return "redirect:/percepteur/mouvements/detail/"+newCaisse.getId();
    }

    @PostMapping("/mouvements/save/{type}/{id}")
    public String saveMouvement(Mouvement mouvement, @PathVariable Long id,@PathVariable String type, RedirectAttributes redirectAttributes,
                                String montant){
        Caisse caisse = caisseRepository.getOne(id);
        mouvement.setCaisse(caisse);
        mouvement.setMontant(Double.parseDouble(montant));
        mouvement.setNumMouvement(""+(mouvementRepository.findAll().size()+1));
        if (type.equals("entree")){
            mouvement.setSoldeFin(caisse.getSoldeFinMois()+mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement.getSoldeFin());
            caisse.setMouvementMensuel(caisse.getMouvementMensuel()+mouvement.getMontant());
        }else {
            mouvement.setSoldeFin(caisse.getSoldeFinMois() - mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement.getSoldeFin());
            caisse.setMouvementMensuel(caisse.getMouvementMensuel()-mouvement.getMontant());
        }
        mouvement.setType(type.toUpperCase());

        caisseRepository.save(caisse);
        mouvementRepository.save(mouvement);
        redirectAttributes.addFlashAttribute("message", "Vous avez ajouter votre mouvement de caisse avec success!");

        return "redirect:/percepteur/mouvements/detail/"+caisse.getId();
    }


    @GetMapping("/mouvements/update/{id}")
    public String updateMouvement(@PathVariable Long id, Model model){
        Mouvement mouvement = mouvementRepository.getOne(id);
        model.addAttribute("mouvement", mouvement);
        return "percepteur/mouvementU";
    }

    @PostMapping("/mouvements/update/{id}")
    public String saveUpdateMouvement(Mouvement mouvement, @PathVariable Long id, RedirectAttributes redirectAttributes,
                                      String montant){
        Mouvement mouvement1 = mouvementRepository.getOne(id);
        mouvement.setMontant(Double.parseDouble(montant));
        Caisse caisse = mouvement1.getCaisse();
        mouvement1.setType(mouvement.getType());

        if (mouvement1.getType().equals("ENTREE")){
            mouvement1.setSoldeFin(caisse.getSoldeFinMois()-mouvement1.getMontant()+mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement1.getSoldeFin());
            caisse.setMouvementMensuel(caisse.getMouvementMensuel()-mouvement1.getMontant()+mouvement.getMontant());
            mouvement1.setMontant(mouvement.getMontant());

        }else {
            mouvement1.setSoldeFin(caisse.getSoldeFinMois()+mouvement1.getMontant()-mouvement.getMontant());
            caisse.setSoldeFinMois(mouvement1.getSoldeFin());
            caisse.setMouvementMensuel(caisse.getMouvementMensuel()+mouvement1.getMontant()-mouvement.getMontant());
            mouvement1.setMontant(mouvement.getMontant());
        }

        caisseRepository.save(caisse);
        mouvementRepository.save(mouvement1);
        redirectAttributes.addFlashAttribute("message", "Vous avez modifier votre mouvement de caisse avec success!");

        return "redirect:/percepteur/mouvements/detail/"+caisse.getId();

    }

    @GetMapping("/mouvements/print/{id}")
    public String producePrinter(Model model, @PathVariable Long id, String printerName){

        Mouvement mouvement = mouvementRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");

        String filePath = location+mouvement.getNumMouvement()+"_"+sdf.format(mouvement.getCreatedDate())+".pdf";
        Document document = new Document(PageSize.A5, 5, 5, 5, 5);
        try{
            PdfWriter.getInstance(document,new FileOutputStream(new File((filePath).toString())));
            document.open();
            Paragraph para1 = new Paragraph("LYCEE CIREZI");
            para1.setAlignment(Paragraph.ALIGN_CENTER);
            para1.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 4, Font.BOLD,
                    BaseColor.GREEN));
            para1.setSpacingAfter(10);
            document.add(para1);

            Paragraph paragraph = new Paragraph("Ecole Conventionnelle Catholique. \n");
            paragraph.setSpacingAfter(10);
            Paragraph paragraph1 = new Paragraph("B.P.  2276      Bukavu\n");
            paragraph1.setSpacingAfter(10);
            Paragraph paragraph2 = new Paragraph("SECOPE  :   6001299 \n");
            paragraph2.setSpacingAfter(10);
            Paragraph paragraph3 = new Paragraph("ARRETE MINISTERIEL N MINEPSP/CABMIN/0614/2005 DU 21/02/2005");
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph1.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph2.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph3.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph1.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph2.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph3.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            document.add(paragraph);
            document.add(paragraph1);
            document.add(paragraph2);
            document.add(paragraph3);
            Paragraph line = new Paragraph("----------------------------------------------------------------");
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);
            document.add(new Paragraph("Bon de caisse :   "+mouvement.getType().toUpperCase()));
            document.add(new Paragraph("Numero  :  "+mouvement.getNumMouvement()));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100.0f);
            table.setWidths(new float[] {3.0f, 2.0f,2.0f,2.0f});
            table.setSpacingBefore(10);
            table.setSpacingAfter(20f);
            table.setSpacingBefore(20f);
            // define font for table header row
            Font font = FontFactory.getFont(FontFactory.HELVETICA);
            font.setColor(BaseColor.WHITE);
            addTableHeader(table);
                table.addCell("1");
                table.addCell(mouvement.getLibelle());
                table.addCell("1");
                table.addCell(mouvement.getMontant().toString()+" $");
                System.out.println("inside the table");

            document.add(table);
            document.close();
            System.out.println("the job is done!!!");

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Printer printer = new Printer();
        printer.print(filePath,printerName);

        return "redirect:/percepteur/mouvements/detail/"+mouvement.getCaisse().getId();
    }

    static void addTableHeader(PdfPTable table) {
        Stream.of("Index", "Libelle", "Quantite","Montant")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPadding(5);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    static void addTableHeaderSalaire(PdfPTable table) {
        Stream.of( "Libelle","Montant")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPadding(5);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }


    @GetMapping("/staffs/salaire/detail/{id}")
    public String detailSalaireStaff(@PathVariable Long id, Model model){
        Salaire salaire = salaireRepository.getOne(id);
        Compte compte = salaire.getCompte();
        model.addAttribute("salaire", salaire);
        model.addAttribute("compte",compte);
        return "percepteur/salairesDetailStaff";
    }

    @GetMapping("/salaire/detail/print/{id}")
    public String producePrinterSalaire(Model model, @PathVariable Long id, String printerName){

        Salaire salaire = salaireRepository.getOne(id);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String filePath = location+salaire.getNumBuletin()+"_"+salaire.getDatePaiement()+".pdf";

        Document document = new Document(PageSize.A5, 5, 5, 10, 10);
        try{
            PdfWriter.getInstance(document,new FileOutputStream(new File((filePath).toString())));
            document.open();
            Paragraph para1 = new Paragraph("LYCEE CIREZI");
            para1.setAlignment(Paragraph.ALIGN_CENTER);
            para1.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 4, Font.BOLD,
                    BaseColor.GREEN));
            para1.setSpacingAfter(10);
            document.add(para1);

            Paragraph paragraph = new Paragraph("Ecole Conventionnelle Catholique. \n");
            paragraph.setSpacingAfter(10);
            Paragraph paragraph1 = new Paragraph("B.P.  2276      Bukavu\n");
            paragraph1.setSpacingAfter(10);
            Paragraph paragraph2 = new Paragraph("SECOPE  :   6001299 \n");
            paragraph2.setSpacingAfter(10);
            Paragraph paragraph3 = new Paragraph("ARRETE MINISTERIEL N MINEPSP/CABMIN/0614/2005 DU 21/02/2005");
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph1.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph2.setAlignment(Paragraph.ALIGN_LEFT);
            paragraph3.setAlignment(Paragraph.ALIGN_CENTER);
            paragraph.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph1.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph2.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            paragraph3.setFont(new Font(Font.FontFamily.TIMES_ROMAN,4,Font.BOLD));
            document.add(paragraph);
            document.add(paragraph1);
            document.add(paragraph2);
            document.add(paragraph3);
            Paragraph line = new Paragraph("----------------------------------------------------------------");
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);
            document.add(new Paragraph("Bulletin de paie Numero :   "+salaire.getNumBuletin()));
            if (salaire.getCompte()!= null) {
                document.add(new Paragraph("Nom et prenom  :  " + salaire.getCompte().getUsername()));
            }else {
                document.add(new Paragraph("Nom et prenom  :  " + salaire.getEnseignant().getName()+" "+salaire.getEnseignant().getPrenom()));
            }
            document.add(new Paragraph("Paiement du Mois de  :  "+salaire.getMois()));
            document.add(new Paragraph("Date  :  "+salaire.getDatePaiement()));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100.0f);
            table.setWidths(new float[] {3.0f, 2.0f});
            table.setSpacingBefore(10);
            table.setSpacingAfter(20f);
            table.setSpacingBefore(20f);
            // define font for table header row
            Font font = FontFactory.getFont(FontFactory.HELVETICA);
            font.setColor(BaseColor.WHITE);
            addTableHeaderSalaire(table);
            table.addCell("Salaire de base");
            table.addCell(salaire.getSalaireBase().toString()+" $");
            table.addCell("Housing");
            table.addCell(salaire.getHousing().toString()+" $");
            table.addCell("Allocation familliale");
            table.addCell(salaire.getAllocationFamilliale().toString()+" $");
            table.addCell("Allocation transport");
            table.addCell(salaire.getAllocationTransport().toString()+" $");
            table.addCell("Salaire brut");
            table.addCell(salaire.getSalaireBrut().toString()+" $");
            table.addCell("Taxe IPR(10% salaire de base)");
            table.addCell(salaire.getTaxe().toString()+" $");
            table.addCell("Mutuelle de sante(5% de salaire brut)");
            table.addCell(salaire.getMutuelleSante().toString()+" $");
            table.addCell("Contribution CNSS(3.5% du salaire brut)");
            table.addCell(salaire.getCnss().toString()+" $");
            table.addCell("Cafeteriat");
            table.addCell(salaire.getCafeteriat().toString()+" $");
            table.addCell("Remboursement mensuel");
            table.addCell(salaire.getRemboursementMensuelle().toString()+" $");
            table.addCell("Net a payer");
            table.addCell(salaire.getNetPaie().toString()+" $");
            table.addCell("Avance salaire");
            table.addCell(salaire.getAvanceSalaire().toString()+" $");
            table.addCell("Salaire net");
            table.addCell(salaire.getSalaireNet().toString()+" $");
            System.out.println("inside the table");

            document.add(table);
            document.add(new Paragraph("Fait a Bukavu le : "+sdf.format(new Date())));
            document.add(new Paragraph("Recu par :                         Signature du staff"));
            document.add(new Paragraph(""));
            document.add(new Paragraph("Signature de la caisse"));
            document.close();
            System.out.println("the job is done!!!");

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Printer printer = new Printer();
        printer.print(filePath,printerName);

        if (salaire.getCompte()!=null) {
            return "redirect:/percepteur/staffs/salaire/detail/"+salaire.getId();
        }else {
            return "redirect:/percepteur/enseignants/salaire/detail/"+salaire.getId();
        }
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
