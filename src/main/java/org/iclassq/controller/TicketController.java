package org.iclassq.controller;

import org.iclassq.navigation.Navigator;
import org.iclassq.view.TicketView;

public class TicketController {
    private final TicketView view;

    public TicketController(TicketView view){
        this.view=view;
        view.setOnClose(this::handleClose);
    }

    private void handleClose() {
        Navigator.navigateToIdentification();
    }
}
