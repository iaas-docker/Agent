package models;

public class Instance extends IdObject {

    private String imageId, userId, baseImageId, state, stateMessage, ipAddressId, physicalMachineId, containerId;
    private Integer cores, ram, memory;


    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBaseImageId() {
        return baseImageId;
    }

    public void setBaseImageId(String baseImageId) {
        this.baseImageId = baseImageId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public void setStateMessage(String stateMessage) {
        this.stateMessage = stateMessage;
    }

    public String getIpAddressId() {
        return ipAddressId;
    }

    public void setIpAddressId(String ipAddressId) {
        this.ipAddressId = ipAddressId;
    }

    public String getPhysicalMachineId() {
        return physicalMachineId;
    }

    public void setPhysicalMachineId(String physicalMachineId) {
        this.physicalMachineId = physicalMachineId;
    }

    public Integer getCores() {
        return cores;
    }

    public void setCores(Integer cores) {
        this.cores = cores;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
