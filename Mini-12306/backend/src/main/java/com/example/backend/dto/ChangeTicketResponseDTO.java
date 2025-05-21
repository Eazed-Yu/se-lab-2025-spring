package com.example.backend.dto;

public class ChangeTicketResponseDTO {
    private String oldTicketId;
    private String newTicketId;
    private String message;
    private TicketDTO newTicket;
    
    public ChangeTicketResponseDTO() {
    }
    
    public ChangeTicketResponseDTO(String oldTicketId, String newTicketId, String message, TicketDTO newTicket) {
        this.oldTicketId = oldTicketId;
        this.newTicketId = newTicketId;
        this.message = message;
        this.newTicket = newTicket;
    }

    public String getOldTicketId() {
        return oldTicketId;
    }

    public void setOldTicketId(String oldTicketId) {
        this.oldTicketId = oldTicketId;
    }

    public String getNewTicketId() {
        return newTicketId;
    }

    public void setNewTicketId(String newTicketId) {
        this.newTicketId = newTicketId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TicketDTO getNewTicket() {
        return newTicket;
    }

    public void setNewTicket(TicketDTO newTicket) {
        this.newTicket = newTicket;
    }
}
