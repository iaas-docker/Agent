package models;

public class User extends IdObject {

    private String email, portusToken;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPortusToken() {
        return portusToken;
    }

    public void setPortusToken(String portusToken) {
        this.portusToken = portusToken;
    }
}
