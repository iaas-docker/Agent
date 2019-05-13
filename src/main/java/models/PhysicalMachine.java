package models;

import java.util.List;

public class PhysicalMachine extends IdObject {

    private List<Integer> assignedRanges;

    public List<Integer> getAssignedRanges() {
        return assignedRanges;
    }

    public void setAssignedRanges(List<Integer> assignedRanges) {
        this.assignedRanges = assignedRanges;
    }
}
