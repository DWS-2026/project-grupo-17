package es.codeurjc.board.dto;

public class SignupRequestDTO {

    private String name;
    private String email;
    private String password;
    private String birthDate;

    public SignupRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
}