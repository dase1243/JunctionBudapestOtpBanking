package com.junction.otpbanking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class Atm {
    private String id;

    private double latitude;
    private double longitude;
    private int[] lineCount;
    private double moneyPresence;
    private String address;
    private boolean ableToPut;
    private boolean ableToTakeOff;
    private long routeTiming;
}
