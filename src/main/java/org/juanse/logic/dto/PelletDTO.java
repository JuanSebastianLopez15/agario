package org.juanse.logic.dto;

import java.io.Serializable;//Combierte un objeto a bytes, los sockets UDP solo entienden arreglos de bytes

public class PelletDTO implements Serializable{
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
