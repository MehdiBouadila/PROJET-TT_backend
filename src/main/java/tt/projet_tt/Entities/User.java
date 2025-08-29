package tt.projet_tt.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Matricule;
    @Column(nullable = false)
    @NotBlank(message = "Nom est obligatoire")
    private String nom;
    @Column(nullable = false)
    @NotBlank(message = "Prenom est obligatoire")
    private String prenom;
    @Enumerated(EnumType.STRING)
    private Profile profile;
    @Column(nullable = false)
    @NotBlank(message = "Mot de passe est obligatoire")
    private String mdp;
    @Email(message = "Email invalide")
    @NotBlank(message = "Email est obligatoire")
    private String email;

    public User() {
    }

    public Long getMatricule() {
        return Matricule;
    }

    public void setMatricule(Long matricule) {
        Matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
//    @Enumerated(EnumType.STRING)
//    private Role role;
//    private Boolean is_verified;
//    @Column(unique = true)
//    private String resetToken;

}
