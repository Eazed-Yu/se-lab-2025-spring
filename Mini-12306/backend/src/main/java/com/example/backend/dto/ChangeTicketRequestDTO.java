package com.example.backend.dto;

public class ChangeTicketRequestDTO {
    private String userId;
    private String ticketId;
    private String newScheduleId;
    private String seatType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getNewScheduleId() {
        return newScheduleId;
    }

    public void setNewScheduleId(String newScheduleId) {
        this.newScheduleId = newScheduleId;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }
}
