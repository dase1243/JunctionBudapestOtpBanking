package com.junction.otpbanking;

import com.google.firebase.database.IgnoreExtraProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@IgnoreExtraProperties
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    public String id;
    public String username;
    public String chosenAtm;
    public long howFarSec;
}