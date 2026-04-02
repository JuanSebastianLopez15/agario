package org.juanse.logic.dto;

public class PlayerDTO {
    public String id;
    public String owner;
    public double x;
    public double y;
    public double mass;

    public PlayerDTO(String id, String owner, double x, double y, double mass) {
        this.id = id;
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.mass = mass;
    }

    public PlayerDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }
}
