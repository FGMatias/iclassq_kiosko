package org.iclassq.printer;


import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import java.util.logging.Logger;

public class PrinterManager {
    private static final Logger logger = Logger.getLogger(PrinterManager.class.getName());
    private static PrinterManager instance;
    private PrintService selectedPrinter;

    private PrinterManager() {
        detectPrinter();
    }

    public static synchronized PrinterManager getInstance() {
        if (instance == null) {
            instance = new PrinterManager();
        }

        return instance;
    }

    public void detectPrinter() {
        logger.info("Detectando impresora...");

        try {
            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

            if (defaultService != null && isPrinterAvailable(defaultService)) {
                selectedPrinter = defaultService;
                logger.info("Impresora por defecto detectada: " + defaultService.getName());
                return;
            }

            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

            if (printServices.length == 0) {
                logger.warning("No se encontraron impresoras en el sistema");
                selectedPrinter = null;
                return;
            }

            for (PrintService service : printServices) {
                if (isPrinterAvailable(service)) {
                    selectedPrinter = service;
                    logger.info("Impresora detectada: " + service.getName());
                    return;
                }
            }

            selectedPrinter = printServices[0];
            logger.warning("Usando impresora aunque no esté completamente disponible: " + printServices[0].getName());
        } catch (Exception e) {
            logger.severe("Error al detectar impresora: " + e.getMessage());
            e.printStackTrace();
            selectedPrinter = null;
        }
    }

    private boolean isPrinterAvailable(PrintService service) {
        try {
            AttributeSet attributes = service.getAttributes();
            PrinterIsAcceptingJobs acceptingJobs = (PrinterIsAcceptingJobs) attributes.get(PrinterIsAcceptingJobs.class);

            return acceptingJobs == null || acceptingJobs.equals(PrinterIsAcceptingJobs.ACCEPTING_JOBS);
        } catch (Exception e) {
            return true;
        }
    }

    public PrintService getSelectedPrinter() {
        return selectedPrinter;
    }

    public boolean hasAvailablePrinter() {
        return selectedPrinter != null;
    }

    public String getSelectedPrinterName() {
        return selectedPrinter != null ? selectedPrinter.getName() : "Ninguna";
    }

    public void refresh() {
        logger.info("Refrescando detección de impresoras...");
        detectPrinter();
    }
}
