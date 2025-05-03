package com.example.demo.Domain;

import com.example.demo.Domain.Validators.Annotations.SafeString;
import com.example.demo.Structures.Sanitizer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.hibernate.validator.constraints.URL;

import java.util.Objects;


//TODO:Add profile pic
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
public class Employer {

    @JsonIgnore
    @Id
    @GeneratedValue
    long ID;

    //Have a way to check employer names are distinct
    @NotBlank
    @NonNull
    @SafeString
    String companyName;

    @NotBlank
    @NonNull
    @Column(columnDefinition = "TEXT")
    String companyDescription;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    EmployerImageFile companyImage;

//    @URL
//    String companyURL=null;


    public void copyFixedTraitsAndSanitizeFields(Employer employer){
        this.ID=employer.ID;
        companyImage =employer.companyImage;
        this.companyName=employer.companyName;
        sanitizeStringFields();
    }
    public void initializeNewEntity(){
        ID=0;
        companyImage =null;
        sanitizeStringFields();
    }

    public void sanitizeStringFields(){
        companyDescription= Sanitizer.sanitize(companyDescription,"companyDescription");
//        if (!new UrlValidator().isValid(companyURL)){
//            companyURL=null;
//        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Employer employer = (Employer) o;

        if (ID != employer.ID) return false;
        if (!companyName.equals(employer.companyName)) return false;
        if (!companyDescription.equals(employer.companyDescription)) return false;
        return Objects.equals(companyImage, employer.companyImage);
    }

    @Override
    public int hashCode() {
        int result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + companyName.hashCode();
        result = 31 * result + companyDescription.hashCode();
        result = 31 * result + (companyImage != null ? companyImage.hashCode() : 0);
        return result;
    }
}
