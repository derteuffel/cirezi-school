package com.derteuffel.school.services;


import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Printer implements Printable {

    public List<String> getPrinters(){

        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

        PrintService printServices[] = PrintServiceLookup.lookupPrintServices(
                flavor, pras);

        List<String> printerList = new ArrayList<String>();
        for(PrintService printerService: printServices){
            printerList.add( printerService.getName());
        }

        return printerList;
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page)
            throws PrinterException {
        if (page > 0) { /* We have only one page, and 'page' is zero-based */
            return NO_SUCH_PAGE;
        }

        /*
         * User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        /* Now we perform our rendering */

        g.setFont(new Font("Roman", 0, 8));
        g.drawString("Hello world !", 0, 10);

        return PAGE_EXISTS;
    }

    public void printString(String printerName, String text) {

        // find the printService of name printerName
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

        PrintService printService[] = PrintServiceLookup.lookupPrintServices(
                flavor, pras);
        PrintService service = findPrintService(printerName);
        System.out.println(printerName);

        DocPrintJob job = service.createPrintJob();

        try {

            byte[] bytes;

            // important for umlaut chars
            bytes = text.getBytes("CP437");

            Doc doc = new SimpleDoc(bytes, flavor, null);


            job.print(doc, null);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void printBytes(String printerName, byte[] bytes) {

        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

        PrintService printService[] = PrintServiceLookup.lookupPrintServices(
                flavor, pras);
        PrintService service = findPrintService(printerName);

        DocPrintJob job = service.createPrintJob();

        try {

            Doc doc = new SimpleDoc(bytes, flavor, null);

            job.print(doc, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private PrintService findPrintService(String printerName,
                                          PrintService[] services) {
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }

        return null;
    }*/
    /**
     * Retrieve the specified Print Service; will return null if not found.
     * @return
     */
    public static PrintService findPrintService(String printerName) {

        PrintService service = null;

        // Get array of all print services - sort order NOT GUARANTEED!
        PrintService[] services = PrinterJob.lookupPrintServices();

        // Retrieve specified print service from the array
        for (int index = 0; service == null && index < services.length; index++) {

            if (services[index].getName().equalsIgnoreCase(printerName)) {

                service = services[index];
            }
        }

        // Return the print service
        return service;
    }



    public void print(String filePath, String printerName) {
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
        FileInputStream psStream = null;
        try {
            psStream = new FileInputStream(filePath);
        } catch (FileNotFoundException ffne) {
            ffne.printStackTrace();
        }
        if (psStream == null) {
            return;
        }
        if (services.length > 0)
        {
            PrintService myService = null;
            for(PrintService service : services) {
                System.out.println(service.getName());
                if(service.getName().contains(printerName)) {
                    myService = service;
                    break;
                }
            }
            DocPrintJob printJob = myService.createPrintJob();
            Doc document = new SimpleDoc(psStream, flavor, null);
            try {
                printJob.print(document, null);
            } catch (PrintException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("No PDF printer available.");
        }
    }
}
