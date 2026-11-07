package com.scc.model;

public record ActivityLog(int id, int userId, String actionType, String description, String timestamp) {
    @Override
    public String toString() {
        return String.format("%-5d | %-8d | %-18s | %-35s | %-20s", id, userId, actionType, description, timestamp);
    }
}