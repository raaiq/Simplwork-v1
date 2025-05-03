package com.example.demo.Domain.Converters.Helpers;


import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

//TODO: Maybe remove converter
public class EnumSetConverter<T extends Enum<T>> {

    private final Class<T> type;

    public EnumSetConverter(Class<T> type){
        this.type=type;
    }
    public String convertToDatabaseColumn(Set<T> attribute) {
        String rolesString="";

        Iterator<T> employerRolesIterator=attribute.iterator();
        while (employerRolesIterator.hasNext()){
            rolesString+=employerRolesIterator.next();
            if(employerRolesIterator.hasNext()){
                rolesString+="; ";
            }
        }
        return rolesString;
    }

    public Set<T> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split("; ")).map(e-> T.valueOf(type,e)).collect(Collectors.toSet());
    }

}
