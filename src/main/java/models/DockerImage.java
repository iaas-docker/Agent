package models;

public class DockerImage extends IdObject {

    private String tag, name, dockerImageId;

    public DockerImage() {}

    public DockerImage(String tag, String name, String dockerImageId) {
        this.tag = tag;
        this.name = name;
        this.dockerImageId = dockerImageId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDockerImageId() {
        return dockerImageId;
    }

    public void setDockerImageId(String dockerImageId) {
        this.dockerImageId = dockerImageId;
    }
}
