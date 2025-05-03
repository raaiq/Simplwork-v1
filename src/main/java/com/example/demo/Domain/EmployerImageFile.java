package com.example.demo.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="employer_image_file")
public class EmployerImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "image_id", nullable = false)
    private UUID imageID;

    @Getter
    @Setter
    private String fileName;

    @Getter
    @Setter
    boolean isPNG;

    public UUID getImageID() {
        return imageID;
    }

    public void setImageID(UUID imageID) {
        this.imageID = imageID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployerImageFile that = (EmployerImageFile) o;

        if (isPNG != that.isPNG) return false;
        if (!Objects.equals(imageID, that.imageID)) return false;
        return Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        int result = imageID != null ? imageID.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (isPNG ? 1 : 0);
        return result;
    }
}
