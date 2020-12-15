package com.derteuffel.school.exports;

import com.derteuffel.school.entities.Eleve;
import com.derteuffel.school.entities.Matiere;
import com.derteuffel.school.entities.Note;
import com.derteuffel.school.entities.Salle;
import com.derteuffel.school.repositories.EleveRepository;
import com.derteuffel.school.repositories.MatiereRepository;
import com.derteuffel.school.repositories.NoteRepository;
import com.derteuffel.school.repositories.SalleRepository;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class MouvementExport {

    @Autowired
    private EleveRepository eleveRepository;

    @Autowired
    private SalleRepository salleRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private MatiereRepository matiereRepository;

    @Value("${file.upload-dir}")
    private String location ;

    public void printBulletin(Long id, Long salleId) throws IOException, BadElementException {
        Float totalPremierePeriode = 0f;
        Float totalTroisiemePeriode = 0f;
        Float totalPremierExamen = 0f;
        Float totalPremierSemestre = 0f;
        Float totalDeuxiemeSemestre = 0f;
        Float totalDeuxiemePeriode = 0f;
        Float totalQuatriemePeriode = 0f;
        Float totalDeuxiemeExamen = 0f;
        Float totalGenerale = 0f;
        int totalMaxPeriode = 0;
        int totalMaxExamen = 0;
        int totalMaxGenerale = 0;
        Eleve eleve = eleveRepository.getOne(id);
        Salle salle = salleRepository.getOne(salleId);
        List<Note> notes = noteRepository.findAllByEleve_Id(eleve.getId());
        List<Matiere> matieres = matiereRepository.findAllBySalle_Id(salle.getId());
        for (Matiere matiere : matieres) {
            Note tmpNote = noteRepository.findByMatiere_IdAndEleve_Id(matiere.getId(), eleve.getId());
            if (tmpNote != null) {
                System.out.println("je contient deja des valeurs");
            } else {
                Note newNote = new Note();
                newNote.setTotalGeneral(0f);
                newNote.setTotalSemestrePremier(0f);
                newNote.setTotalSemestreSecond(0f);
                newNote.setExameSecond(0f);
                newNote.setExamePremier(0f);
                newNote.setQuatriemePeriode(0f);
                newNote.setTroisiemePeriode(0f);
                newNote.setDeuxiemePeriode(0f);
                newNote.setPremierePeriode(0f);
                newNote.setExamen(0);
                newNote.setMaxPeriode(0);
                newNote.setEleve(eleve);
                newNote.setMatiere(matiere);
                noteRepository.save(newNote);
                System.out.println("J'etais vide et maintenant je suis composer");
            }
        }


        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        String image = location+"drapeau-rdc.png";
        String image2 = location+"armoirie-rdc.png";

        Image image1 = Image.getInstance(image);
        Image image3 = Image.getInstance(image2);

        image1.scaleToFit(50,50);
        image3.scaleToFit(50,50);
        String filePath = location+eleve.getName().toUpperCase()+eleve.getPrenom().toUpperCase()+eleve.getAge()+".pdf";

        Document document = new Document(PageSize.A4,10,10,10,10);

        try{

            PdfWriter.getInstance(document,new FileOutputStream(new File(filePath).toString()));
            document.open();
            FontSelector fontSelector = new FontSelector();
            FontSelector fontSelector2 = new FontSelector();
            Font f1 = FontFactory.getFont(FontFactory.TIMES_ROMAN,12);
            Font f2 = FontFactory.getFont(FontFactory.TIMES_ROMAN,8);
            System.out.println("j'ai reduit la taille");
            fontSelector.addFont(f1);
            fontSelector2.addFont(f2);
            PdfPTable header = new PdfPTable(3);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setWidthPercentage(100);

            header.setWidths(new float[] {15f, 70f,15f});
            header.addCell(image1);
            String headerText = "REPUBLIQUE DEMOCRATIQUE DU CONGO \n\nMINISTERE DE L'ENSEIGNEMENT, PRIMAIRE, SECONDAIRE ET\n\n PROFESSIONNEL";
            Phrase headerPhrase = fontSelector.process(headerText);
            Paragraph paragraph = new Paragraph(headerPhrase);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            header.addCell(paragraph);
            header.addCell(image3);

            document.add(header);


            PdfPTable Nid = new PdfPTable(1);
            Nid.setWidthPercentage(100);
            String nidText =  "       N ID";
            Phrase nidPhrase = fontSelector.process(nidText);
            Nid.addCell(nidPhrase);
            document.add(Nid);


            PdfPTable province = new PdfPTable(1);
            province.setWidthPercentage(100);
            String provinceText = "       PROVINCE : SUD KIVU";
            Phrase provincePhrase = fontSelector.process(provinceText);
            province.addCell(provincePhrase);
            document.add(province);

            PdfPTable ecoleDetail = new PdfPTable(2);
            ecoleDetail.setWidthPercentage(100);
            ecoleDetail.setWidths(new int[] {50, 50});
            String ecoleDetailText1 = "  VILLE : BUKAVU\n\n  COMMUNE/TER(1) : IBANDA\n\n  ECOLE : LYCEE CIREZI\n\n  CODE : 2 - 630031\n\n";
            String ecoleDetailText2 = "  ELEVE : "+eleve.getName().toUpperCase()+" "+eleve.getPrenom().toUpperCase()+"\n\n  NE(E) A : "+eleve.getLocalisation()+"     LE : "+eleve.getAge()+" ans\n\n  CLASSE : "+eleve.getSalle().getNiveau()+"\n\n  N PERM : 2 - 630031\n\n";
            Phrase ecoleDetailPhrase1 = fontSelector2.process(ecoleDetailText1);
            Phrase ecoleDetailPhrase2 = fontSelector2.process(ecoleDetailText2);
            ecoleDetail.addCell(ecoleDetailPhrase1);
            ecoleDetail.addCell(ecoleDetailPhrase2);
            document.add(ecoleDetail);



            PdfPTable title = new PdfPTable(1);
            title.setWidthPercentage(100);
            String titleText = "       BULLETIN DE LA "+salle.getNiveau().toUpperCase()+"        ANNEE SCOLAIRE 2019 - 2020\n";
            Phrase titlePhrase = fontSelector2.process(titleText);
            title.addCell(titlePhrase);
            document.add(title);

            PdfPTable noteHeader = new PdfPTable(5);
            noteHeader.setWidthPercentage(100);
            noteHeader.setWidths(new int[] {25,25,25, 10,15});
            String branchesText = "  BRANCHES";
            Phrase branchesPhrase = fontSelector2.process(branchesText);
            noteHeader.addCell(branchesPhrase);
            PdfPCell cell = new PdfPCell();
            PdfPTable semestresUn = new PdfPTable(3);
            semestresUn.setWidthPercentage(102);
            semestresUn.setWidths(new int[] {50,25,25});
            PdfPCell travauxCell = new PdfPCell();
            PdfPTable travauxTable = new PdfPTable(2);
            travauxTable.setWidthPercentage(104);
            travauxTable.setWidths(new int[] {50,50});
            String premiereTex = "1ere P";
            String deuxiemeTex = "2eme P";
            String travauxTex = "TRAVAUX JOURNAL";
            Phrase premierePhrase = fontSelector2.process(premiereTex);
            Phrase deuxiemePhrase = fontSelector2.process(deuxiemeTex);
            Phrase travauxPhrase1 = fontSelector2.process(travauxTex);
            travauxTable.addCell(premierePhrase);
            travauxTable.addCell(deuxiemePhrase);
            travauxCell.addElement(travauxPhrase1);
            travauxCell.addElement(travauxTable);
            semestresUn.addCell(travauxCell);
            String exam = "EXAM";
            String tot1 = "TOT";
            Phrase examPhrase = fontSelector2.process(exam);
            Phrase totPhrase = fontSelector2.process(tot1);
            semestresUn.addCell(examPhrase);
            semestresUn.addCell(totPhrase);
            String semestres1Text = "PREMIER SEMESTRE";
            Phrase semestre1Phrase = fontSelector2.process(semestres1Text);
            cell.addElement(semestre1Phrase);
            cell.addElement(semestresUn);
            noteHeader.addCell(cell);
            PdfPCell cell2 = new PdfPCell();
            PdfPTable semestresDeux = new PdfPTable(3);
            semestresDeux.setWidthPercentage(102);
            semestresDeux.setWidths(new int[] {50,25,25});
            PdfPCell travauxCell1 = new PdfPCell();
            PdfPTable travauxTable1 = new PdfPTable(2);
            travauxTable1.setWidthPercentage(104);
            travauxTable1.setWidths(new int[] {50,50});
            String troisiemeTex = "3eme P";
            String quatriemeTex = "4eme P";
            Phrase troisiemePhrase = fontSelector2.process(troisiemeTex);
            Phrase quatriemePhrase = fontSelector2.process(quatriemeTex);
            travauxTable1.addCell(troisiemePhrase);
            travauxTable1.addCell(quatriemePhrase);
            travauxCell1.addElement(travauxPhrase1);
            travauxCell1.addElement(travauxTable1);
            semestresDeux.addCell(travauxCell1);
            semestresDeux.addCell(examPhrase);
            semestresDeux.addCell(totPhrase);
            String semestres2Text = "SECOND SEMESTRE";
            Phrase semestre2Phrase = fontSelector2.process(semestres2Text);
            cell2.addElement(semestre2Phrase);
            cell2.addElement(semestresDeux);
            noteHeader.addCell(cell2);
            String totQ = "T.Q.";
            Phrase totQPhrase = fontSelector2.process(totQ);
            String repechageText = "EXAMEN DE REPECHAGE";
            Phrase repechagePhrase = fontSelector2.process(repechageText);
            noteHeader.addCell(totQPhrase);
            noteHeader.addCell(repechagePhrase);

            document.add(noteHeader);

            PdfPTable noteTable = new PdfPTable(12);
            noteTable.setWidthPercentage(100);
            noteTable.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});

            for (Note note : notes){
                totalPremierePeriode +=note.getPremierePeriode();
                totalDeuxiemePeriode +=note.getDeuxiemePeriode();
                totalTroisiemePeriode +=note.getTroisiemePeriode();
                totalQuatriemePeriode +=note.getQuatriemePeriode();
                totalDeuxiemeExamen +=note.getExameSecond();
                totalPremierExamen +=note.getExamePremier();
                totalPremierSemestre +=note.getTotalSemestrePremier();
                totalDeuxiemeSemestre +=note.getTotalSemestreSecond();
                totalMaxPeriode +=note.getMaxPeriode();
                totalMaxExamen +=note.getExamen();
                totalGenerale += note.getTotalGeneral();
                String nameText = "     "+note.getMatiere().getName().toUpperCase();
                Phrase namePhrase = fontSelector2.process(nameText);
                noteTable.addCell(namePhrase);
                noteTable.addCell(note.getPremierePeriode().toString());
                noteTable.addCell(note.getDeuxiemePeriode().toString());
                noteTable.addCell(note.getExamePremier().toString());
                noteTable.addCell(note.getTotalSemestrePremier().toString());
                noteTable.addCell(note.getTroisiemePeriode().toString());
                noteTable.addCell(note.getQuatriemePeriode().toString());
                noteTable.addCell(note.getExameSecond().toString());
                noteTable.addCell(note.getTotalSemestreSecond().toString());
                noteTable.addCell(note.getTotalGeneral().toString());
                noteTable.addCell("");
                noteTable.addCell("");
            }
            document.add(noteTable);



            PdfPTable statisticTable = new PdfPTable(12);
            statisticTable.setWidthPercentage(100);
            statisticTable.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String maximaText = "     MAXIMA GENERAUX";
            Phrase maximaPhrase = fontSelector2.process(maximaText);
            statisticTable.addCell(maximaPhrase);
            statisticTable.addCell(totalMaxPeriode+"");
            statisticTable.addCell(totalMaxPeriode+"");
            statisticTable.addCell(totalMaxExamen+"");
            statisticTable.addCell((totalMaxPeriode * 2) + totalMaxExamen+"");
            statisticTable.addCell(totalMaxPeriode+"");
            statisticTable.addCell(totalMaxPeriode+"");
            statisticTable.addCell(totalMaxExamen+"");
            statisticTable.addCell((totalMaxPeriode * 2) + totalMaxExamen+"");
            statisticTable.addCell((((totalMaxPeriode * 2) + totalMaxExamen) * 2) +"");
            statisticTable.addCell("");
            statisticTable.addCell("");
            document.add(statisticTable);

            PdfPTable totaux = new PdfPTable(12);
            totaux.setWidthPercentage(100);
            totaux.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String totauxText = "     TOTAUX";
            Phrase totauxPhrase = fontSelector2.process(totauxText);

            totaux.addCell(totauxPhrase);
            totaux.addCell(totalPremierePeriode+"");
            totaux.addCell(totalDeuxiemePeriode+"");
            totaux.addCell(totalPremierExamen+"");
            totaux.addCell(totalPremierSemestre+"");
            totaux.addCell(totalTroisiemePeriode+"");
            totaux.addCell(totalQuatriemePeriode+"");
            totaux.addCell(totalDeuxiemeExamen+"");
            totaux.addCell(totalDeuxiemeSemestre+"");
            totaux.addCell(totalDeuxiemeSemestre + totalPremierSemestre +"");
            totaux.addCell("");
            totaux.addCell("");
            document.add(totaux);

            PdfPTable pourcentage = new PdfPTable(12);
            pourcentage.setWidthPercentage(100);
            pourcentage.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String pourcentageText = "     POURCENTAGE";
            Phrase pourcentagePhrase = fontSelector2.process(pourcentageText);
            pourcentage.addCell(pourcentagePhrase);
            pourcentage.addCell(((totalPremierePeriode * 100) / totalMaxPeriode)+" %");
            pourcentage.addCell(((totalDeuxiemePeriode * 100) / totalMaxPeriode)+" %");
            pourcentage.addCell(((totalPremierExamen * 100) / totalMaxExamen) +" %");
            pourcentage.addCell((totalPremierSemestre * 100) / ((totalMaxPeriode * 2) + totalMaxExamen) +" %");
            pourcentage.addCell(((totalTroisiemePeriode * 100) / totalMaxPeriode)+" %");
            pourcentage.addCell(((totalQuatriemePeriode * 100) / totalMaxPeriode)+" %");
            pourcentage.addCell(((totalDeuxiemeExamen * 100) / totalMaxExamen)+" %");
            pourcentage.addCell((totalDeuxiemeSemestre * 100) / ((totalMaxPeriode * 2) + totalMaxExamen)+" %");
            pourcentage.addCell((((totalPremierSemestre * 100) / ((totalMaxPeriode * 2) + totalMaxExamen) + (totalDeuxiemeSemestre * 100) / ((totalMaxPeriode * 2) + totalMaxExamen)) / 2) +" %");
            pourcentage.addCell("");
            pourcentage.addCell("");
            document.add(pourcentage);

            PdfPTable rangTable = new PdfPTable(12);
            rangTable.setWidthPercentage(100);
            rangTable.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String rangText = "     PLACE/Nbre D'ELEVES";
            Phrase rangPhrase = fontSelector2.process(rangText);
            rangTable.addCell(rangPhrase);
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell(" /"+eleveRepository.findAllBySalle_Id(salle.getId()).size());
            rangTable.addCell("");
            rangTable.addCell("");
            document.add(rangTable);


            PdfPTable application = new PdfPTable(12);
            application.setWidthPercentage(100);
            application.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String applicationText = "     APPLICATION";
            Phrase applicationPhrase = fontSelector2.process(applicationText);
            application.addCell(applicationPhrase);
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            application.addCell("");
            document.add(application);


            PdfPTable conduite = new PdfPTable(12);
            conduite.setWidthPercentage(100);
            conduite.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String conduiteText = "     CONDUITE";
            Phrase conduitePhrase = fontSelector2.process(conduiteText);
            conduite.addCell(conduitePhrase);
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            conduite.addCell("");
            document.add(conduite);

            PdfPTable signature = new PdfPTable(12);
            signature.setWidthPercentage(100);
            signature.setWidths(new float[] {25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,6.25f,10f,5f,10f});
            String signatureResp = "     SIGN DU RESPONSABLE";
            Phrase signaturePhrase = fontSelector2.process(signatureResp);
            signature.addCell(signaturePhrase);
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            signature.addCell("");
            document.add(signature);


            PdfPTable footer = new PdfPTable(1);
            footer.setWidthPercentage(100);
            String footerText = "       - L'eleve ne pourra passer dans la classe superieure s'il(elle) n'a subit avec succes un examen de\n"+"    repechage en:______________________________________________________\n\n"+"     _______________________________________________(f)\n\n"+
                    "     - L'eleve passe dans la classe superieure(f)\n\n"+
                    "     - L'eleve redouble la classe(f)                                        Fait a Bukavu, le ..../...../2020\n\n"+
                    "     - L'eleve a echoue et est ???????? vers _________________ (f)                Le chef d'Etablissement\n\n"+
                    "                                                                                    (Sceau et Signature)\n\n"+
                    "     Signature de l'eleve                 Sceau de l'ecole                      Soeur MUKEBE MIKOKA Godelieve\n\n"+
                    "     (f) ??????? la mention inutile\n\n"+
                    "     Note importante : le bulletin est sans valeur s'il est sature ou surcharge";
            Phrase footerPhrase = fontSelector2.process(footerText);
            Paragraph footerParagraph = new Paragraph(footerPhrase);
            footerParagraph.setSpacingAfter(5);
            footerParagraph.setSpacingBefore(5);
            footer.addCell(footerParagraph);
            document.add(footer);


            Paragraph line = new Paragraph("--------------------------------------------------------------------------------");
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);

            document.close();
            System.out.println("the job is done!!!");

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        eleve.setBulletinPath("/upload-dir/"+eleve.getName().toUpperCase()+eleve.getPrenom().toUpperCase()+eleve.getAge()+".pdf");
        eleveRepository.save(eleve);
    }
}
