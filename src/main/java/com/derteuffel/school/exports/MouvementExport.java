package com.derteuffel.school.exports;

import com.derteuffel.school.entities.Mouvement;

import java.util.List;

public class MouvementExport {

    List<Mouvement> mouvements;
    Mouvement mouvement;

    public MouvementExport(List<Mouvement> mouvements) {
        this.mouvements = mouvements;
    }

    public MouvementExport(Mouvement mouvement) {
        this.mouvement = mouvement;
    }

   /* private void writeTableHeader(PdfTable table){
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new Phrase("User ID", font));

        table.addCell(cell);

        cell.setPhrase(new Phrase("E-mail", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Full Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Roles", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Enabled", font));
        table.addCell(cell);
    }

    public void export(HttpServletResponse response, Mouvement mouvement) throws DocumentException, IOException {
        Document document = new Document(PageSize.A5);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.BLUE);

        Paragraph titleParagraph = new Paragraph("Lycee CIREZI ");
        Paragraph p = new Paragraph("Bon de caisse "+ mouvement.getType(), font);
        p.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(titleParagraph);
        document.add(p);

        PdfTable pdfTable;
        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Phrase("Intitule"));
        pdfTable.a


        document.close();

    }*/
}
