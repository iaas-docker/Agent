package models;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

public class IdObject {

    @MongoId
    @MongoObjectId
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof IdObject))
            return false;
        IdObject idObj = (IdObject) obj;
        if (id == null || idObj.id == null)
            return false;
        return id.equals(idObj.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}