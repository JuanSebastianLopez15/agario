package org.juanse.logic.dto;

public class HazardDTO {
    public String id;
    public double x;
    public double y;

    public HazardDTO(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public HazardDTO() {
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
