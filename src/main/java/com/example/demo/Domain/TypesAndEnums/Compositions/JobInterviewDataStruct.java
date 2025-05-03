package com.example.demo.Domain.TypesAndEnums.Compositions;

import com.example.demo.Domain.Match;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobInterviewDataStruct {
    Match.CompositeKey key;
    String complimentaryToken;
}
