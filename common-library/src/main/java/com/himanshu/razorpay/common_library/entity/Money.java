package com.himanshu.razorpay.common_library.entity;


import jakarta.persistence.Embeddable;
import lombok.*;


@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Money {

    private long amountUnits;
    private String currency;

    public static Money of(long amountUnits, String currency){
        return new Money(amountUnits, currency);
    }

    public static Money inr(long amountUnits){
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies do not match");
        }
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies do not match");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }
}
