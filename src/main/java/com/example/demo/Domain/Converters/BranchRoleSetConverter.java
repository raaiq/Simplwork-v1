package com.example.demo.Domain.Converters;

import com.example.demo.Domain.Converters.Helpers.EnumSetConverter;
import com.example.demo.Domain.RolesAndAuthorities.BranchRole;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Set;

@Converter(autoApply = true)
public class BranchRoleSetConverter implements AttributeConverter<Set<BranchRole>,String> {

    private EnumSetConverter<BranchRole> converter=new EnumSetConverter<>(BranchRole.class);
    @Override
    public String convertToDatabaseColumn(Set<BranchRole> attribute) {
        return converter.convertToDatabaseColumn(attribute);
    }

    @Override
    public Set<BranchRole> convertToEntityAttribute(String dbData) {
        return converter.convertToEntityAttribute(dbData);
    }
}
