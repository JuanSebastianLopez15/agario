package org.juanse.logic.dto;

public class PelletDTO {
    public String id;
    public double x;
    public double y;

    public PelletDTO(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public PelletDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
