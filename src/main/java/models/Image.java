package models;

public class Image extends IdObject {

    private String type, backedById;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBackedById() {
        return backedById;
    }

    public void setBackedById(String backedById) {
        this.backedById = backedById;
    }
}
