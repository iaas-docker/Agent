package models;

import java.util.List;

public class PhysicalMachine extends IdObject {

    private List<Integer> assignedRanges;

    private Integer freeCores, freeRam, freeMemory;

    public Integer getFreeCores() {
        return freeCores;
    }

    public void setFreeCores(Integer freeCores) {
        this.freeCores = freeCores;
    }

    public Integer getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(Integer freeRam) {
        this.freeRam = freeRam;
    }

    public Integer getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(Integer freeMemory) {
        this.freeMemory = freeMemory;
    }

    public List<Integer> getAssignedRanges() {
        return assignedRanges;
    }

    public void setAssignedRanges(List<Integer> assignedRanges) {
        this.assignedRanges = assignedRanges;
    }
}
