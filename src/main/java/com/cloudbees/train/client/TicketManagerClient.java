package com.cloudbees.train.client;


public class TicketManagerClient {
    public static void main(String[] args) {
        TicketManagerClientImpl  ticketManagerClient = new TicketManagerClientImpl();
        ticketManagerClient.selectionYourOption();
    }
}
